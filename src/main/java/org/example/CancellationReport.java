package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CancellationReport {
    public static void save(String filePath, String roomName, List<String> cancelledUsers) throws IOException {
        try(
                PrintWriter writer = new PrintWriter( new FileWriter(filePath,true))){
                    String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    writer.println("--- cancellation report---");
                    writer.println("Timestamp : "+ timeStamp);
                    writer.println("room :" + roomName);
                    writer.println("cancelled reservaitons: "+ cancelledUsers.size());

                    if(cancelledUsers.isEmpty()){
                        writer.println("no reservations were affected");
                    }
                    else {
                        for(String username : cancelledUsers){
                            writer.println("-"+username);
                        }
                    }
                    writer.println();
        }
    }
}
