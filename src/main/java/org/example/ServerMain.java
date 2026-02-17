//saurav niroula
//comp 490


package org.example;

import java.time.LocalDateTime;

public class ServerMain {
    public static void main(String[] args) throws Exception{
        String baseUrl = System.getenv("SERVER_URL");
        String email = System.getenv("USER_EMAIL");
        String password  = System.getenv("USER_PASSWORD");
        System.out.println(baseUrl);

        RoomBooking client = new RoomBooking();

        if(client.login(email, password, baseUrl)){
            System.out.println("login successful");
            LocalDateTime start = LocalDateTime.now().plusMinutes(5);
            LocalDateTime end = start.plusMinutes(15);

            String rooms = client.listAvailableRooms(baseUrl, start.toString() ,end.toString());
            System.out.println("Available Rooms: " + rooms);


            String booking = client.bookRoom(baseUrl,1,start,end,1);
            System.out.println("booking response: "+ booking);

            String myBooking = client.listMyBooking(baseUrl);
            System.out.println("my bookings" + myBooking);

            String cancel = client.cancelBooking(baseUrl,1);
            System.out.println("cancel " + cancel);
        }
        else {
            System.out.println("wack");
        }

    }
}
