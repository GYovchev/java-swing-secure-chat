package Client;

import Client.DB.Persister;
import Client.GUI.GUI;
import Client.Server.ServerCommunicator;
import Client.Server.ServerError;

import java.sql.SQLException;

public class TCPClient {
    public static void main(String[] args) throws SQLException {
        Persister p = new Persister("persister_db_client.db");
        p.init();
        ServerCommunicator sc = new ServerCommunicator("127.0.0.1", 1337, p);
        GUI gui = new GUI(sc);
        try {
            sc.connect();
        } catch (ServerError serverError) {
            serverError.printStackTrace();
            gui.alert("couldn't connect to server");
        }
        gui.openInitialForm();
    }
}
