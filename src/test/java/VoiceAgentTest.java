import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.MicrophoneListener;
import org.example.RoomBooking;
import org.example.SecretsLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentTest {
    private static final int testRoomId = 1;
    private RoomBooking roomBooking;
    private OllamaChatModel llm;
    private String baseUrl;
    private String modelPath;
    private int createdBookingId = -1;

    @BeforeEach
    void setUp() throws Exception {
        Properties secrets = SecretsLoader.load();
        baseUrl = secrets.getProperty("api.base_Url");
        modelPath = secrets.getProperty("vosk.model_path");

        roomBooking = new RoomBooking();
        boolean ok = roomBooking.login(
                secrets.getProperty("api.email"),
                secrets.getProperty("api.password"),
                baseUrl
        );
        assertTrue(ok, "login must succeed");

        LocalDateTime start = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        String bookingResponse = roomBooking.bookRoom(baseUrl, testRoomId, start, end, 2);
        System.out.println("booking response:" + bookingResponse);

        if (bookingResponse.contains("\"id\"")) {
            createdBookingId = Integer.parseInt(
                    bookingResponse.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1").trim());
        }
        String ollamaUrl = secrets.getProperty("ollama.base_Url", "http://127.0.0.1:11434");
        String ollamaModel = secrets.getProperty("ollama.model", "llama3.2");

        llm = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(ollamaModel)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (createdBookingId > 0) {
            String cancelResp = roomBooking.cancelBooking(baseUrl, createdBookingId);
            System.out.println("Cancelled booking " + createdBookingId + ": " + cancelResp);
        }
    }

    @Test
    void agentAnswers() throws Exception {
        String question = transcribeWavFile(
                Path.of(modelPath),
                new File("src/test/resources/test.wav")
        );
        System.out.println("transcribed: " + question);
        assertFalse(question.isBlank(), "WAV file is empty");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String myBookings = roomBooking.listMyBooking(baseUrl);
        String available = roomBooking.listAvailableRooms(baseUrl, null, null);

        String prompt = """
                you are a meeting assistant
                behave accordingly
                Current date and time: %s
                User's booking: %s
                available rooms: %s
                user's qusetion: %s
                """.formatted(now, myBookings, available, question);

        String answer = llm.generate(prompt);
        System.out.println(answer);
        assertFalse(answer.isBlank(), "agent has returned blank");

        assertTrue(
                answer.toLowerCase().contains("room 2") ||
                        answer.toLowerCase().contains("room2") ||
                        answer.toLowerCase().contains("meeting rooms"),
                "answer must reference the booked room but: " + answer
        );

    }

    private static String transcribeWavFile(Path modelPath, File wavFile) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        try (Model model = new Model(modelPath.toString());
             Recognizer recognizer = new Recognizer(model, 16000);
             AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = ais.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String text = MicrophoneListener.extractText(recognizer.getResult());
                    if (!text.isEmpty()) {
                        stringBuilder.append(text).append(" ");
                    }
                }
            }

            String tail = MicrophoneListener.extractText(recognizer.getFinalResult());
            if (!tail.isEmpty()) {
                stringBuilder.append(tail);
            }
        }

        return stringBuilder.toString().trim();
    }
}
