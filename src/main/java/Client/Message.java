package Client;

public class Message {
    private String sender;
    private String receiver;
    private String message;
    private String externalId;

    public Message(String sender, String receiver, String message, String externalId) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.externalId = externalId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
