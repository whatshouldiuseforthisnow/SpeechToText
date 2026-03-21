package org.example;

import java.sql.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class DatabaseManager {
    private final String dbPath;
    public DatabaseManager(String dbPath){
        this.dbPath = dbPath;
    }

    private Connection connect() throws SQLException{
        return DriverManager.getConnection("jdbc:sqlite:"+dbPath);
    }

    public List<Room> getAllRooms() throws SQLException{
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, room_name, capacity " +
                "FROM booking_meetingroom";
        try(
                Connection conn = connect();
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
                ){
            while(resultSet.next()) {
                rooms.add(
                        new Room(resultSet.getInt("id"), resultSet.getString("room_name"),
                                resultSet.getInt("capacity"))
                );
            }
            }
        return rooms;
        }


    public void addRoom(String name, int capacity) throws SQLException {
        String sql = "INSERT INTO booking_meetingroom (room_name,capacity,is_active) VALUES (?,?,?)";
        try (
                Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, capacity);
            preparedStatement.setBoolean(3,true);
            preparedStatement.executeUpdate();
        }
    }

    public void updateCapacity(int roomId,int newCapacity) throws SQLException{
        String sql = "UPDATE booking_meetingroom SET capacity = ? WHERE id = ?";
        try (Connection connection = connect();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1,newCapacity);
            preparedStatement.setInt(2,roomId);
            preparedStatement.executeUpdate();
        }
    }

    public List<String> removeRoom(int roomId) throws SQLException{
        List<String> cancelledUsers = new ArrayList<>();

        try(Connection connection = connect())
        {
            connection.setAutoCommit(false);
            try {
                String findUsers = "SELECT DISTINCT member_customuser.email "
                        + "FROM booking_bookinghistory " +
                        "JOIN member_customuser ON booking_bookinghistory.booked_by_id = member_customuser.id " +
                        "WHERE booking_bookinghistory.meeting_room_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(findUsers)) {
                    preparedStatement.setInt(1, roomId);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            cancelledUsers.add(resultSet.getString("email"));
                        }
                    }
                }

                String deleteBookings = "DELETE FROM booking_bookinghistory WHERE meeting_room_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteBookings)) {
                    preparedStatement.setInt(1, roomId);
                    preparedStatement.executeUpdate();
                }
                String deleteRoom = "DELETE FROM booking_meetingroom WHERE id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteRoom)) {
                    preparedStatement.setInt(1, roomId);
                    preparedStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e){
                connection.rollback();
                throw e;
            }
        }
        return cancelledUsers;
    }

    public record Room(int id, String name, int capacity){
        @Override
        public String toString(){
            return name + "(capacity: " + capacity + ")";
        }
    }
}


