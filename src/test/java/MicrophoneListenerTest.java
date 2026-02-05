import org.junit.jupiter.api.Test;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.example.MicrophoneListener.extractText;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MicrophoneListenerTest
{
    public static String listeningTest(File audioFile, Path modelPath ) throws Exception {
        Model model = new Model(modelPath.toString());
        Recognizer recognizer = new Recognizer(model,16000);

        try(javax.sound.sampled.AudioInputStream ais =
                javax.sound.sampled.AudioSystem.getAudioInputStream(audioFile)){
            byte[] buffer = new byte[4096];
            int byteRead;
            StringBuilder resultText = new StringBuilder();

            while((byteRead = ais.read(buffer)) >= 0 ){
                if(recognizer.acceptWaveForm(buffer, byteRead)){
                    resultText.append(extractText(recognizer.getResult())).append(" ");
                }
            }
            resultText.append(extractText(recognizer.getFinalResult()));
            return  resultText.toString().trim();
        }
        finally {
            recognizer.close();
            model.close();
        }
    }


    @Test
    void testSpeechToTextFromWavFile() throws Exception{
        File audioFile = new File("src/test/resources/test.wav");
        Path modelPath = Path.of("models");

        assertTrue(audioFile.exists(), " test.wav not found");
        assertTrue(modelPath.toFile().exists(),"vosk folder is missing");

        String transcs = listeningTest(audioFile,modelPath);

        System.out.println("result: " + transcs);

        assertFalse(transcs.isEmpty(),"wack");

        assertTrue(
                transcs.toLowerCase().contains("you"),"word not found"
        );


    }

}

