package org.example;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.file.Path;

import org.vosk.Model;
import org.vosk.Recognizer;

public class MicrophoneListener {
    private static final double SilenceThreshold = 500.0;
    private static final long SilenceDuration = 1500;
    private static final long maxListen = 30000;

    public static String listen(Path modelPath) throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        StringBuilder transcript = new StringBuilder();


        try(Model model = new Model(modelPath.toString());
            Recognizer recognizer = new Recognizer(model, 16000)){
            byte[] buffer = new byte[4096];

            System.out.println("Listening speak");
            boolean speechStarted = false;
            long silenceAt = 0;
            long deadSound = System.currentTimeMillis() + maxListen;
            int bytesRead = 0;
            while (System.currentTimeMillis() < deadSound) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                double rms = calculateRms(buffer, bytesRead);
                boolean isSilent = rms < SilenceThreshold;
                if (!isSilent) {
                    speechStarted = true;
                    silenceAt = 0;
                } else if (speechStarted) {
                    if (silenceAt == 0) {
                        silenceAt = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - silenceAt >= SilenceDuration) {
                        System.out.println("silence detected");
                        break;
                    }
                }

                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String text = extractText(recognizer.getResult());
                    if (!text.isEmpty()) {
                        System.out.println("u said:" + text);
                        transcript.append(text).append(" ");
                    }
                }
            }


            String tail = extractText(recognizer.getFinalResult());
            if (!tail.isEmpty()) {
                System.out.println(tail);
                transcript.append(tail);
            }
        }
        finally{
                microphone.stop();
                microphone.close();
            }

            return transcript.toString().trim();
        }



    //this idea was conceived by AI
   public static double calculateRms(byte[] buffer, int bytesRead){
        long sumSquares = 0;
        int samples = bytesRead / 2;
        for(int i = 0 ; i < bytesRead - 1; i+=2){
            short sample = (short) ((buffer[i+1] << 8 ) | (buffer[i] & 0xFF));
            sumSquares += (long) sample * sample;
        }
        return samples > 0 ? Math.sqrt((double) sumSquares / samples ): 0;
   }

    public static String extractText(String json) {
        if (json == null || json.isBlank()) return "";
        return json.replaceAll(".*\"text\"\\s*:\\s*\"([^\"]*)\".*", "$1").trim();
    }

}

