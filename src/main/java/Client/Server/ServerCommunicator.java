package Client.Server;

import Client.ClientError;
import Client.DB.Persister;
import Client.Crypto.KeyPairManager;
import Client.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class ServerCommunicator {
    private final String host;
    private final int port;

    private PrintStream output;
    private Scanner input;

    private Persister persister;

    public ServerCommunicator(String host, int port, Persister persister) {
        this.host = host;
        this.port = port;
        this.persister = persister;
    }

    public void connect() throws ServerError {
        try {
            Socket client = new Socket(host, port);
            this.input = new Scanner(client.getInputStream());
            this.output = new PrintStream(client.getOutputStream());
        } catch (Exception e) {
            throw new ServerError("couldn't connect to server");
        }
    }

    private void sendActionToServer(String action) throws ServerError {
        output.println(action);
        String response = input.nextLine();
        if (!response.equals("OK")) {
            throw new ServerError(response);
        }
    }

    public byte[] login(String username, String password, String privateKeyFileName) throws ServerError, ClientError {
        synchronized (input) {
            byte[] privateKey;
            this.sendActionToServer("login");
            try {
                privateKey = KeyPairManager.readRSAKeyPairFromFile(privateKeyFileName);
            } catch (IOException e) {
                throw new ClientError("couldn't open primary key file");
            }
            output.println(username);
            output.println(password);

            String result = input.nextLine();
            if (!result.equals("OK")) {
                throw new ServerError("error while logging in: " + result);
            }
            return privateKey;
        }
    }

    public void register(String username, String password, String privateKeyFileName) throws ServerError {
        synchronized (input) {
            this.sendActionToServer("register");

            PublicKey pk = KeyPairManager.genRSAKeyPairAndSaveToFile(privateKeyFileName);

            output.println(username);
            output.println(password);
            output.println(new String(Base64.getEncoder().encode(pk.getEncoded())));

            String result = input.nextLine();
            if (!result.equals("OK")) {
                throw new ServerError("error while registering: " + result);
            }
        }
    }

    public void sendMessage(String sender, String message, String receiver) throws ServerError, ClientError {
        synchronized (input) {
            this.sendActionToServer("send_message");
            output.println(receiver);

            String result = input.nextLine();
            if (result.equals("OK")) {
                // TODO: get this from public blockchain in the future
                String recipientPublicKeyBase64 = input.nextLine();
                PublicKey recipientPublicKey = null;
                try {
                    recipientPublicKey = KeyFactory.getInstance("RSA").generatePublic(
                            new X509EncodedKeySpec(Base64.getDecoder().decode(recipientPublicKeyBase64)));
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                    throw new ClientError("internal error");
                }
                Cipher cipher = null;
                byte[] encryptedMessage;
                try {
                    cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
                    encryptedMessage = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
                    e.printStackTrace();
                    throw new ClientError("internal error");
                }
                output.println(new String(Base64.getEncoder().encode(encryptedMessage)));

                result = input.nextLine();
                if (!result.equals("OK")) {
                    throw new ServerError("error while sending a message: " + result);
                }
                this.persister.insertMessage(new Message(sender, receiver, message, input.nextLine()));
            } else {
                throw new ServerError("error while sending a message: " + result);
            }
        }
    }

    private Map<String, String> getLocalMessages(String username) {
        ArrayList<Message> msgs = this.persister.getMessagesOfUser(username);
        Map<String, String> res = new HashMap<>();
        for (Message m :
                msgs) {
            res.put(m.getExternalId(), m.getMessage());
        }
        return res;
    }

    public ArrayList<Message> getMessages(String impersonatedUser, String otherUser, byte[] impersonatedUserPrivateKey) throws ServerError, ClientError {
        synchronized (input) {
            this.sendActionToServer("get_messages");
            output.println(otherUser);
            String result = input.nextLine();
            if (!result.equals("OK")) {
                throw new ServerError("error while sending a message: " + result);
            }
            String size = input.nextLine();
            int s = Integer.parseInt(size);
            ArrayList<Message> msgs = new ArrayList<>();
            Map<String, String> localMsgs = getLocalMessages(otherUser);

            for (int i = 0; i < s; i++) {
                String externalId = input.nextLine();
                String sender = input.nextLine();
                byte[] message = Base64.getDecoder().decode(input.nextLine());
                if (sender.equals(impersonatedUser)) {
                    msgs.add(new Message(impersonatedUser, otherUser, localMsgs.get(externalId), externalId));
                } else {
                    String decryptedMessage;
                    try {
                        PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(
                                new PKCS8EncodedKeySpec(impersonatedUserPrivateKey));
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.DECRYPT_MODE, pk);
                        decryptedMessage = new String(cipher.doFinal(message));
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
                        e.printStackTrace();
                        throw new ClientError("internal error");
                    }
                    msgs.add(new Message(otherUser, impersonatedUser, decryptedMessage, externalId));
                }
            }
            result = input.nextLine();
            if (!result.equals("OK")) {
                throw new ServerError("error while sending a message: " + result);
            }
            return msgs;
        }
    }
}
