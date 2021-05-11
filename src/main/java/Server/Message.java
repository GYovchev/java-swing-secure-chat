package Server;

import java.util.Date;

public class Message {

    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    private User sender;
    private User receiver;
    private Date timestamp;
    private byte[] message;

    public Message(String id, User sender, User receiver, Date ts, byte[] msg) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = ts;
        this.message = msg;
    }
}
