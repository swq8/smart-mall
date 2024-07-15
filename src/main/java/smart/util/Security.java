package smart.util;

import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static smart.util.Helper.bytesToHex;

/**
 * 安全助手函数
 */
@Component
public class Security {

    static AesBytesEncryptor AES_BYTES_ENCRYPTOR;

    public Security(AesBytesEncryptor aesBytesEncryptor) {
        AES_BYTES_ENCRYPTOR = aesBytesEncryptor;
    }

    public static String aesDecrypt(String encryptorBase64) {
        try {
            return new String(AES_BYTES_ENCRYPTOR.decrypt(Base64.getDecoder().decode(encryptorBase64)));
        } catch (Exception e) {
            return null;
        }
    }

    public static String aesEncrypt(String str) {
        return Base64.getEncoder().encodeToString(AES_BYTES_ENCRYPTOR.encrypt(str.getBytes()));
    }

    /**
     * 计算字符串的hash
     *
     * @param algorithm hash算法,如：MD5 SHA-256
     * @param data      待计算hash值的数据
     * @return hash值
     */
    public static String hash(String algorithm, String data) {
        try {
            byte[] bytes = MessageDigest.getInstance(algorithm).digest(data.getBytes());
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            LogUtils.error(Security.class, "", ex);
            return null;
        }
    }

    /**
     * 计算流的hash
     *
     * @param algorithm   hash算法,如：MD5 SHA-256
     * @param inputStream 待计算hash值的流
     * @return hash值
     */
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
        } catch (Exception ex) {
            LogUtils.error(Security.class, "", ex);
            return null;
        }
    }

    public static String rsaDecrypt(String base64Key, String base64Content) {
        try {
            var base64Decoder = Base64.getDecoder();
            var rsaPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(base64Decoder.decode(base64Key)));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
            var decryptBuffer = cipher.doFinal(base64Decoder.decode(base64Content));
            return new String(decryptBuffer);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 字符串的sha256哈希
     *
     * @param data 待计算hash值的字符串
     * @return hash
     */
    public static String sha256(String data) {
        return hash("SHA-256", data);
    }

    /**
     * 计算流的sha256哈希值
     *
     * @param inputStream 待计算hash值的流
     * @return hash
     */
    public static String sha256(InputStream inputStream) {
        return hash("SHA-256", inputStream);
    }

    /**
     * 字符串的sha3-256哈希
     *
     * @param data 待计算hash值的字符串
     * @return hash
     */
    public static String sha3_256(String data) {
        return hash("SHA3-256", data);
    }

    /**
     * 计算流的sha3-256哈希值
     *
     * @param inputStream 待计算hash值的流
     * @return hash
     */
    public static String sha3_256(InputStream inputStream) {
        return hash("SHA3-256", inputStream);
    }


}
