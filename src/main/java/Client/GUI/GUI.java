package Client.GUI;

import Client.*;
import Client.GUI.Views.LoginRegisterForm;
import Client.GUI.Views.MainForm;
import Client.GUI.Views.StartForm;
import Client.Server.ServerCommunicator;
import Client.Server.ServerError;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;

public class GUI {

    private JFrame frame;

    private ServerCommunicator sc;
    private MessagesUpdater mu;

    private byte[] privateKey;
    private String loggedInUser;
    private String chattingWithUsername = "";

    private Thread messagePoller;

    public GUI(ServerCommunicator sc) {
        this.sc = sc;
        this.frame = new JFrame("Secure chat client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void openInitialForm() {
        frame.setContentPane(new StartForm(this).$$$getRootComponent$$$());
        frame.pack();
    }

    public void alert(String message) {
        showMessageDialog(null, message);
    }

    public void goToLogin() {
        this.frame.setContentPane(new LoginRegisterForm(this, true).$$$getRootComponent$$$());
        this.frame.pack();
    }

    public void goToRegister() {
        this.frame.setContentPane(new LoginRegisterForm(this, false).$$$getRootComponent$$$());
        this.frame.pack();
    }

    public void login(String username, String password, String privateKeyFileName) {
        try {
            this.privateKey = this.sc.login(username, password, privateKeyFileName);
        } catch (ServerError | ClientError e) {
            this.alert(e.getMessage());
            return;
        }
        this.loggedInUser = username;
        MainForm mf = new MainForm(this);
        this.mu = mf;
        this.frame.setContentPane(mf.$$$getRootComponent$$$());
        this.frame.pack();
    }

    public void register(String username, String password, String privateKeyFileName) {
        try {
            this.sc.register(username, password, privateKeyFileName);
        } catch (ServerError e) {
            this.alert(e.getMessage());
            return;
        }
        this.frame.setContentPane(new LoginRegisterForm(this, true).$$$getRootComponent$$$());
        this.frame.pack();
    }

    public void sendMessage(String message) {
        if (chattingWithUsername.equals("")) {
            this.alert("choose a user you want to chat with");
            return;
        }
        if (privateKey == null) {
            this.alert("you are not logged in");
        }
        try {
            this.sc.sendMessage(this.loggedInUser, message, chattingWithUsername);
        } catch (ServerError | ClientError e) {
            this.alert(e.getMessage());
            return;
        }
        this.updateMessages(chattingWithUsername);
    }

    public void updateMessages(String username) {
        ArrayList<Message> messages;
        try {
            messages = sc.getMessages(this.loggedInUser, username, this.privateKey);
        } catch (ServerError | ClientError serverError) {
            this.alert(serverError.getMessage());
            return;
        }
        updateMessageBox(messages);
        if (!chattingWithUsername.equals(username)) {
            if (this.messagePoller != null) {
                this.messagePoller.interrupt();
            }

            ServerCommunicator sc = this.sc;
            GUI gui = this;
            String loggedInUser = this.loggedInUser;
            byte[] privateKey = this.privateKey;
            chattingWithUsername = username;
            this.messagePoller = new Thread(){
                boolean exit = false;

                public void run()
                {
                    String username = chattingWithUsername;
                    while(!exit) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) { }
                        ArrayList<Message> messages;
                        try {
                            messages = sc.getMessages(loggedInUser, username, privateKey);;
                            updateMessageBox(messages);
                        } catch (ServerError | ClientError serverError) {
                            gui.alert(serverError.getMessage());
                        }
                    }
                }
            };
            this.messagePoller.start();
        }
    }

    private void updateMessageBox(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message m :
                messages) {
            sb.append("[" + m.getSender() + "]: ").append(m.getMessage()).append("\n");
        }
        this.mu.updateMessages(sb.toString());
    }
}
