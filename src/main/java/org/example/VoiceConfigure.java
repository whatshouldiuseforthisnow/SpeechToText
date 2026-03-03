package org.example;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class VoiceConfigure {

    public static void main(String[] args) throws Exception{
        var secrets = SecretsLoader.load();
        String baseUrl = secrets.getProperty("api.base_Url");
        String email = secrets.getProperty("api.email");
        String password = secrets.getProperty("api.password");
        String modelPath = secrets.getProperty("vosk.model_path");
        String ollamaUrl = secrets.getProperty("ollama.base_Url", "http://localhost:11434");
        String ollamaModel = secrets.getProperty("ollama.model","llama3.1");

        RoomBooking roomBooking = new RoomBooking();
        boolean loggedIn = roomBooking.login(email,password,baseUrl);
        if(!loggedIn){
            System.err.println("login failed big dawg");
            System.exit(1);
        }
        System.out.println("logged in big dawg 2");

        OllamaChatModel llm = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(ollamaModel)
                .timeout(Duration.ofSeconds(60))
                .build();

        System.out.println("ready to ask about your reservations");
        System.out.println("press CTRL + C to quit");

        while(true){
            String question = MicrophoneListener.listen(Path.of(modelPath));
            if(question.isBlank()){
                System.out.println("cant hear nothing say again");
                continue;
            }
            System.out.println("you asked:"+question);
            String now = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String myBookings = roomBooking.listMyBooking(baseUrl);
            String availableRooms = roomBooking.listAvailableRooms(baseUrl,null,null);
            String prompt = """
                    you are a meeting room assistant act accordingly.
                    Current date and time: %s
                    User's bookings:%s
                    Available rooms: %s
                    User's question: %s
                    """.formatted(now,myBookings, availableRooms, question);
            String answer = llm.generate(prompt);
            System.out.println("AGENT:"+answer);
        }
    }

}
