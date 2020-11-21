package alex.lib;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static alex.lib.Helper.bytesToHex;

public class Crypto {
    public static String CIPHER = "AES-256-CBC";

    public static String hash(String algorithm, String data) {
        try {
            byte[] bytes = MessageDigest.getInstance(algorithm).digest(data.getBytes());
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    public static String hash(String algorithm, InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            DigestInputStream dis = new DigestInputStream(inputStream, md);
            byte[] buffer = new byte[10240];
            while (dis.read(buffer) > -1) {
            }
            String hash = bytesToHex(dis.getMessageDigest().digest());
            dis.close();
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String sha256(String data) {
        return hash("SHA-256", data);
    }
    public static String sha256(InputStream inputStream) {
        return hash("SHA-256", inputStream);
    }
    public static String sha3_256(String data) {
        return hash("SHA3-256", data);
    }
    public static String sha3_256(InputStream inputStream) {
        return hash("SHA3-256", inputStream);
    }
}
