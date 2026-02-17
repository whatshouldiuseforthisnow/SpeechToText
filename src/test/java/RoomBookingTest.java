import okhttp3.Response;
import org.example.RoomBooking;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class RoomBookingTest {
    private static final String BASE_URL = System.getenv("BASE_URL");
    private static final String EMAIL = System.getenv("TEST_EMAIL");
    private static final String PASSWORD = System.getenv("TEST_PASSWORD");
    private static final Logger logger = LoggerFactory.getLogger(RoomBookingTest.class);

    private static RoomBooking client;


    @BeforeAll
    static void setup(){
        assertNotNull(BASE_URL,"baseurl not set");
        assertNotNull(EMAIL,"eMail buddy");
        assertNotNull(PASSWORD,"pw buddy");

        client = new RoomBooking();
    }

    @Test
    void testLogin() throws Exception{
        boolean success = client.login(EMAIL,PASSWORD,BASE_URL);
        assertTrue(success,"login should succed");
        logger.info(()-> "login success"+success);
    }

    @Test
    void testListAvailableRooms() throws Exception{
      assertTrue(client.login(EMAIL,PASSWORD,BASE_URL));

      LocalDateTime start = LocalDateTime.now().plusMinutes(10);
      LocalDateTime end = start.plusMinutes(15);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


      String response = client.listAvailableRooms(BASE_URL,start.toString(),end.toString());

      assertNotNull(response,"should not be null");
      assertFalse(response.isEmpty(),"response not em[ty");
      System.out.println("available rooms " + response);


    }

    @Test
    void testBook() throws Exception{
        assertTrue(client.login(EMAIL,PASSWORD,BASE_URL));

        LocalDateTime start = LocalDateTime.now().plusMinutes(20);
        LocalDateTime end = start.plusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:a");

        String bookingResponse = client.bookRoom(BASE_URL,1,start,end,4);
        assertNotNull(bookingResponse);
        assertFalse(bookingResponse.isEmpty());
        System.out.println("first booking " + bookingResponse);

        String bookingResponse2 = client.bookRoom(BASE_URL,1,start,end,4);
        System.out.println("second booking "+bookingResponse2);
        assertTrue(bookingResponse2.toLowerCase().contains("error") ||
                bookingResponse2.toLowerCase().contains("cannot"),
                "Second booking has to fail");
    }
}