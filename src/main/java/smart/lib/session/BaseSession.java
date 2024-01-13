package smart.lib.session;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;

/**
 * session interface
 */
public interface BaseSession {
    // 过期时间
    public static Duration TIMEOUT = Duration.ofHours(48);

    /**
     * 生成 session id
     *
     * @return session id
     */
    static String generalId() {
        return new BigInteger(SecureRandom.getSeed(64)).abs().toString(36);
    }

    /**
     * 销毁会话信息
     */
    void destroy();

    /**
     * get value by ke
     *
     * @param key session key
     * @return session value
     */
    Object get(String key);

    /**
     * get all data
     *
     * @return all data
     */
    Map<String, Object> getAll();

    /**
     * 获取session id
     *
     * @param canCreate create if not exist
     * @return session
     */
    String getId(boolean canCreate);

    /**
     * delete key
     *
     * @param keys keys
     */
    void delete(String... keys);

    /**
     * set key value
     *
     * @param key   session key
     * @param value session value
     */
    void set(String key, Object value);

}
