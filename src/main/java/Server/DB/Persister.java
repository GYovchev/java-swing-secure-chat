package Server.DB;

import Server.Message;
import Server.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

        // TODO: inefficient messages to users relation
        String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + "	username text PRIMARY KEY NOT NULL,\n"
                + "	password text NOT NULL,\n"
                + "	public_key blob NOT NULL\n"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "CREATE TABLE IF NOT EXISTS messages(\n" +
                " id text NOT NULL,\n" +
                " sender_username text NOT NULL,\n" +
                " receiver_username text NOT NULL,\n" +
                " timestamp timestamp NUT NULL,\n" +
                " message blob NUT NULL\n" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users(username,password,public_key) VALUES(?,?,?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        final byte[] pubKey = user.getPublicKey().getEncoded();
        pstmt.setBytes(3, pubKey);
        pstmt.executeUpdate();
    }

    public User getUser(String username) {
        String sql = "SELECT username, password, public_key FROM users WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                return new User(resultSet.getString(1), resultSet.getString(2), resultSet.getBytes(3));
            } else {
                return null;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertMessage(Message msg) {
        String sql = "INSERT INTO messages(id,sender_username,receiver_username,timestamp,message) VALUES(?,?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, msg.getId());
            pstmt.setString(2, msg.getSender().getUsername());
            pstmt.setString(3, msg.getReceiver().getUsername());
            pstmt.setDate(4, new Date(msg.getTimestamp().getTime()));
            pstmt.setBytes(5, msg.getMessage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Message> getMessagesOfUsers(User user1, User user2) {
        ArrayList<Message> msgs = new ArrayList<>();
        String sql = "SELECT sender_username, receiver_username, timestamp, message, id FROM messages " +
                "WHERE (sender_username = ? AND receiver_username = ?) OR (sender_username = ? AND receiver_username = ?) ORDER BY timestamp asc";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1.getUsername());
            pstmt.setString(2, user2.getUsername());
            pstmt.setString(3, user2.getUsername());
            pstmt.setString(4, user1.getUsername());
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                String sender_username = resultSet.getString(1);
                String receiver_username = resultSet.getString(2);
                User sender = getUser(sender_username);
                if (sender == null) {
                    continue; // just skip messages with corrupted sender
                }
                User receiver = getUser(receiver_username);
                if (receiver == null) {
                    continue; // just skip messages with corrupted receiver
                }
                msgs.add(new Message(resultSet.getString(5), sender, receiver, resultSet.getDate(3), resultSet.getBytes(4)));
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