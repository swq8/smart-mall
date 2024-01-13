package smart.util;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import smart.config.AppProperties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static smart.util.Helper.bytesToHex;

/**
 * 安全助手函数
 */
@Component
public class Security {


    private static final String AES_KEY_ALGORITHM = "AES";
    // AES加密算法
    private static final String AES_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static SecretKeySpec secretKeySpec;

    /**
     * 初始化密钥
     */
    public Security(ConfigurableApplicationContext context) {
        AppProperties appProperties = context.getBean(AppProperties.class);
        String key = appProperties.getKey();
        if (key == null) {
            LogUtils.warn(getClass(), "need config: app.key");
            key = "";
        }
        KeyGenerator kg = null;
        SecureRandom secureRandom = null;
        try {
            kg = KeyGenerator.getInstance(AES_KEY_ALGORITHM);
            // 初始化密钥生成器，AES要求密钥长度为128位、192位、256位
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            LogUtils.error(getClass(), "", ex);
            return;
        }
        secureRandom.setSeed(key.getBytes());
        kg.init(128, secureRandom);
        SecretKey secretKey = kg.generateKey();
        // AES密钥
        secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), AES_KEY_ALGORITHM);
    }

    /**
     * AES 解密
     *
     * @param base64Content 待解密的base64字符串
     * @return 解密后的原文
     */
    public static String aesDecrypt(String base64Content) {
        try {
            byte[] content = Base64.getDecoder().decode(base64Content);
            if (content.length < 28) {
                return null;
            }
            GCMParameterSpec params = new GCMParameterSpec(128, content, 0, 12);
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, params);
            byte[] decryptData = cipher.doFinal(content, 12, content.length - 12);
            return new String(decryptData, CHARSET);
        } catch (BadPaddingException | IllegalArgumentException e) {
            return null;
        } catch (Exception ex) {
            LogUtils.error(Security.class, "", ex);
            return null;
        }

    }

    /**
     * AES 加密
     *
     * @param content 待加密内容
     * @return 返回base64转码后的加密数据
     */
    public static String aesEncrypt(String content) {
        try {
            byte[] iv = new byte[12];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            byte[] contentBytes = content.getBytes(CHARSET);
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            GCMParameterSpec params = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, params);
            byte[] encryptData = cipher.doFinal(contentBytes);
            assert encryptData.length == contentBytes.length + 16;
            byte[] message = new byte[12 + contentBytes.length + 16];
            System.arraycopy(iv, 0, message, 0, 12);
            System.arraycopy(encryptData, 0, message, 12, encryptData.length);
            return Base64.getEncoder().encodeToString(message);
        } catch (Exception ex) {
            LogUtils.error(Security.class, "", ex);
        }
        return null;
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
