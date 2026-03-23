import org.example.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.io.File;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseManagementTest {
    private DatabaseManager db;
    private String tempDbPath;

    @BeforeEach
    void setUp() throws Exception{
        File tempFile = File.createTempFile("test_db",".sqlite3");
        tempFile.deleteOnExit();
        tempDbPath = tempFile.getAbsolutePath();
        db = new DatabaseManager(tempDbPath);
        createSchema();
    }

    @AfterEach
    void tearDown() throws Exception{
        new File(tempDbPath).delete();
    }

    private void createSchema() throws SQLException{
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + tempDbPath);
             Statement statement = connection.createStatement()){

            statement.execute("CREATE TABLE IF NOT EXISTS booking_meetingroom("+
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "room_name TEXT NOT NULL," +
                    "capacity INTEGER NOT NULL," +
                    "is_active BOOLEAN NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS member_customuser (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS booking_bookinghistory (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "meeting_room_id INTEGER REFERENCES booking_meetingroom(id)," +
                    "booked_by_id INTEGER REFERENCES member_customuser(id)," +
                    "start_time TEXT," +
                    "end_time TEXT)");
        }
    }
    private void insertRoom(int id, String name, int capacity) throws SQLException {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ tempDbPath);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO booking_meetingroom(id,room_name, capacity, is_active) VALUES (?,?,?,?)"
            )) {
            preparedStatement.setInt(1,id);
            preparedStatement.setString(2,name);
            preparedStatement.setInt(3,capacity);
            preparedStatement.setBoolean(4,true);
            preparedStatement.executeUpdate();

        }
    }

    private void insertUser(int id, String email) throws SQLException{
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ tempDbPath);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO member_customuser(id,email) VALUES (?,?)"
            )) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        }
    }

    private void insertBooking(int roomId, int userId) throws SQLException{
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ tempDbPath);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO booking_bookinghistory(meeting_room_id,booked_by_id,start_time,end_time) VALUES (?,?,?,?)"
            )) {
            preparedStatement.setInt(1, roomId);
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, "2026-03-01 11:00");
            preparedStatement.setString(4,"2026-03-02 11:00");
            preparedStatement.executeUpdate();
        }
    }

    private int countRows(String table) throws SQLException{

        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ tempDbPath);
           Statement statement = connection.createStatement();
           ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM "+ table)){
            return resultSet.getInt(1);
        }
    }


    @Test
    void addRoomInsertsCorrectly() throws Exception{
        db.addRoom("Con A",96);
        List<DatabaseManager.Room> rooms = db.getAllRooms();
        assertEquals(1,rooms.size(), "should have 1 room");
        assertEquals(96,rooms.get(0).capacity());
    }

    @Test
    void removeRoomDeletesRoom() throws Exception{
        insertRoom(1,"room delete", 5);
        insertUser(1, "saurav@gmail.com");
        insertBooking(1,1);
        assertEquals(1,countRows("booking_meetingroom"));
        assertEquals(1,countRows("booking_bookinghistory"));
        db.removeRoom(1);
        assertEquals(0,countRows("booking_meetingroom"),"Room should be deletyed");
        assertEquals(0,countRows("booking_bookinghistory"), "booking deleted");
    }

    @Test
    void updateCapacity() throws Exception{
        insertRoom(1,"ROOm CHANGE", 5);
        db.updateCapacity(1,20);
        List<DatabaseManager.Room> rooms = db.getAllRooms();
        assertEquals(1,rooms.size());
        assertEquals(20,rooms.get(0).capacity(),"capacity changed");
    }

    @Test
    void removeRoomCancelledUser() throws Exception{
        insertRoom(1, "ROOm cancelled", 5);
        insertUser(1,"saurav@gmail.com");
        insertUser(2,"niroula@gmail.com");
        insertBooking(1,2);
        insertBooking(1,1);
        List<String> cancel = db.removeRoom(1);
        assertEquals(2,cancel.size(), " should return 2 users");
        assertTrue(cancel.contains("saurav@gmail.com"));
        assertTrue(cancel.contains("niroula@gmail.com"));
    }

    @Test
    void removeRoomWithNoBooking() throws Exception{
        insertRoom(1,"empty",4);
        List <String> cancel = db.removeRoom(1);
        assertTrue(cancel.isEmpty(), "should return empty list");
        assertEquals(0,countRows("booking_meetingroom"),"room still deleted");
    }
}
