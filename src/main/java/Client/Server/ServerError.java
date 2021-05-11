package Client.Server;

public class ServerError extends Exception {
    public ServerError(String message) {
        super("Server error: " + message);
    }
}
