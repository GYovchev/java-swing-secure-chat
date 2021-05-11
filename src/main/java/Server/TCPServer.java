package Server;

import Server.Client.ClientCommunication;
import Server.DB.Persister;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class TCPServer {

    private final int mainPort;
    private final Persister persister;

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        new TCPServer(1337).run();
    }

    public TCPServer(int mainPort) throws SQLException {
        this.mainPort = mainPort;
        this.persister = new Persister("persister_db.sqlite");
        persister.init();
    }

    public void run() throws IOException {
        ServerSocket mainServer = new ServerSocket(mainPort) {
            protected void finalize() throws IOException {
                this.close();
            }
        };
        System.out.println("Ports " + this.mainPort);

        while (true) {
            Socket clientSocket = mainServer.accept();
            new Thread(new ClientCommunication(clientSocket, persister)).start();
        }
    }
}

