package Client.DB;

import Client.Message;

import java.sql.*;
import java.util.ArrayList;

public class Persister {

    private String fileName;
    private Connection conn;

    public Persister(String fileName) {
        this.fileName = fileName;
    }

    private void connect() throws SQLException {
        String url = "jdbc:sqlite:" + this.fileName;
        this.conn = DriverManager.getConnection(url);

        System.out.println("Connection to SQLite has been established.");
    }

    public void init() throws SQLException {
        this.connect();

        String sql = "CREATE TABLE IF NOT EXISTS messages(\n" +
                " external_id text NOT NULL,\n" +
                " username text NOT NULL,\n" +
                " message text NUT NULL\n" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertMessage(Message msg) {
        String sql = "INSERT INTO messages(external_id,username,message) VALUES(?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, msg.getExternalId());
            pstmt.setString(2, msg.getReceiver());
            pstmt.setString(3, msg.getMessage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Message> getMessagesOfUser(String username) {
        ArrayList<Message> msgs = new ArrayList<>();
        String sql = "SELECT external_id,username,message FROM messages " +
                "WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                String external_id = resultSet.getString(1);
                String msgUsername = resultSet.getString(2);
                String message = resultSet.getString(3);
                msgs.add(new Message("", msgUsername, message, external_id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgs;
    }

    public void close() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}