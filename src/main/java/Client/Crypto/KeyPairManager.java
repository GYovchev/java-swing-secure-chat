package Client.Crypto;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairManager {
    public static byte[] readRSAKeyPairFromFile(String fileName) throws IOException {
        byte[] privateKey;
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fileName));
            privateKey = is.readAllBytes();
        } finally {
            if (is != null)
                is.close();

        }
        return privateKey;
    }

    public static PublicKey genRSAKeyPairAndSaveToFile(String fileName) {
        System.out.println("generating secure key pairs");
        KeyPair keyPair = genRSAKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        System.out.println("Saving key pairs");
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(fileName));
            dos.write(privateKey.getEncoded());
            dos.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (dos != null)
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return publicKey;
    }

    private static KeyPair genRSAKeyPair() {
        return genRSAKeyPair(2048);
    }

    private static KeyPair genRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance("RSA");
            keyPairGenerator.initialize(keyLength);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
