package Server.Client;

import Server.DB.Persister;
import Server.Message;
import Server.User;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

enum ClientAction {LOGIN, REGISTER, SEND_MESSAGE, GET_MESSAGES};

public class ClientCommunication implements Runnable {

    private final Socket socket;
    private final Scanner input;
    private final PrintStream outputStream;
    private User user;
    private final Persister persister;

    public ClientCommunication(Socket socket, Persister persister) throws IOException {
        this.socket = socket;
        this.input = new Scanner(socket.getInputStream());
        this.outputStream = new PrintStream(socket.getOutputStream());
        this.persister = persister;
    }

    private ClientAction getAction() {
        String action = input.nextLine();
        if (action.equals("login")) {
            this.outputStream.println("OK");
            return ClientAction.LOGIN;
        }
        if (action.equals("register")) {
            this.outputStream.println("OK");
            return ClientAction.REGISTER;
        }
        if (action.equals("send_message")) {
            this.outputStream.println("OK");
            return ClientAction.SEND_MESSAGE;
        }
        if (action.equals("get_messages")) {
            this.outputStream.println("OK");
            return ClientAction.GET_MESSAGES;
        }
        this.outputStream.println("invalid action");
        return null;
    }

    private void runRegister() {
        String username = input.nextLine();
        String password = input.nextLine();
        String publicKeyBase64 = input.nextLine();
        byte[] publicKey = Base64.getDecoder().decode(publicKeyBase64);
        User user = this.persister.getUser(username);
        if (user != null) {
            outputStream.println("user already exists");
        } else {
            try {
                this.persister.insertUser(new User(username, password, publicKey));
                outputStream.println("OK");
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                outputStream.println("internal error");
            }
        }
    }

    private User runLogin() {
        String username = input.nextLine();
        String password = input.nextLine();
        User user = this.persister.getUser(username);
        if (user == null) {
            outputStream.println("user doesn't exist");
        } else {
            if (user.getPassword().equals(password)) {
                outputStream.println("OK");
                return user;
            } else {
                outputStream.println("wrong password");
            }
        }
        return null;
    }

    private void runSendMessage() {
        String username = input.nextLine();
        User user = this.persister.getUser(username);
        if (user == null) {
            outputStream.println("user doesn't exist");
        } else {
            outputStream.println("OK");
            outputStream.println(new String(Base64.getEncoder().encode(user.getPublicKey().getEncoded())));
            byte[] message = Base64.getDecoder().decode(input.nextLine());
            String id = UUID.randomUUID().toString();
            this.persister.insertMessage(new Message(id, this.user, user, new Date(), message));
            outputStream.println("OK");
            outputStream.println(id);
        }
    }

    private void runGetMessages() {
        String username = input.nextLine();
        User user = this.persister.getUser(username);
        if (user == null) {
            outputStream.println("user doesn't exist");
            return;
        }
        outputStream.println("OK");
        ArrayList<Message> messages = this.persister.getMessagesOfUsers(this.user, user);
        outputStream.println(messages.size());
        for (Message m: messages) {
            outputStream.println(m.getId());
            outputStream.println(m.getSender().getUsername());
            outputStream.println(new String(Base64.getEncoder().encode(m.getMessage())));
        }
        outputStream.println("OK");
    }

    public void run() {
        try {
            while(true) {
                ClientAction action = this.getAction();
                if (action == null) {
                    return;
                }
                if (action == ClientAction.REGISTER) {
                    this.runRegister();
                }
                if (action == ClientAction.LOGIN) {
                    this.user = this.runLogin();
                }
                if (action == ClientAction.SEND_MESSAGE) {
                    this.runSendMessage();
                }
                if (action == ClientAction.GET_MESSAGES) {
                    this.runGetMessages();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}