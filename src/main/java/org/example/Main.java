package org.example;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        String modelPath = "models";
        String outputFile = "transcription_" + System.currentTimeMillis() + ".txt";

        System.out.println("model = " + modelPath);
        System.out.println("Output file = " + outputFile);

        MicrophoneListener.listen(Paths.get(modelPath), Paths.get(outputFile));
    }
}