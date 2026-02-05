package org.example;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.FileWriter;
import java.nio.file.Path;
import org.vosk.Model;
import org.vosk.Recognizer;

public class MicrophoneListener {
    public static void listen(Path modelPath, Path outputFile) throws Exception{
        AudioFormat format = new AudioFormat(16000,16,1,true,false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        Model model = new Model(modelPath.toString());
        Recognizer recognizer = new Recognizer(model,16000);

        try(FileWriter writer = new FileWriter(outputFile.toFile(),true)){
            byte[] buffer = new byte[4096];

            System.out.println("Listening speak");

            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis()- startTime < 10000){
                int bytesRead = microphone.read(buffer,0,buffer.length);
                if(recognizer.acceptWaveForm(buffer,bytesRead)){
                    String text = extractText(recognizer.getResult());
                    if(!text.isEmpty()){
                        System.out.println("Text Transcribed: "+ text);
                        writer.write(text + System.lineSeparator());
                        writer.flush();
                    }
                }
            }
        }
        microphone.close();
        recognizer.close();
        model.close();

        System.out.println("Text saved: " + outputFile);

    }

    public static String extractText(String json) {
        return json.replaceAll(".*\"text\"\\s*:\\s*\"([^\"]*)\".*", "$1").trim();
    }

}

