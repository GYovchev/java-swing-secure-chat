package Server;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class User {
    private final String username;
    private final String password;

    PublicKey publicKey;

    public User(String email, String password) {
        this.username = email;
        this.password = password;
    }

    public User(String email, String password, byte[] publicKey) throws IOException {
        this.username = email;
        this.password = password;
        try {
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            this.publicKey = null;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
