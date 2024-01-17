package smart.lib.session;

import smart.config.RedisConfig;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * api session
 * 相关配置等在 GlobalAuth SessionConfig 中实现
 */
public class ApiSession implements BaseSession {
    // 请求时的参数名称
    // /api/test?token=012345678ab
    public static String REQUEST_NAME = "token";

    //redis prefix
    public static String REDIS_PREFIX = "api:";

    // session id
    private String id;

    public ApiSession(String sessionId) {
        id = sessionId;
    }

    /**
     * create session object
     *
     * @param request request
     */
    public ApiSession(HttpServletRequest request) {

        id = request.getParameter(REQUEST_NAME);
        if (id != null && !flush()) {
            id = null;
        }
        request.setAttribute("API_SESSION", this);
    }

    public static ApiSession from(HttpServletRequest request) {
        var obj = request.getAttribute("API_SESSION");
        return (ApiSession) obj;
    }

    /**
     * delete
     */
    @Override
    public void delete(String... keys) {
        if (id != null) {
            RedisConfig.getStringObjectRedisTemplate().opsForHash().delete(REDIS_PREFIX + id, (Object[]) keys);
        }

    }

    @Override
    public void destroy() {
        if (id != null) {
            RedisConfig.getStringObjectRedisTemplate().delete(REDIS_PREFIX + id);
        }
    }

    /**
     * 刷新过期时间
     */
    protected boolean flush() {
        if (id != null) {
            Boolean b = RedisConfig.getStringObjectRedisTemplate().expire(REDIS_PREFIX + id, TIMEOUT);
            if (b != null) {
                return b;
            }
        }
        return false;
    }

    @Override
    public Object get(String key) {
        return id == null ? null : RedisConfig.getStringObjectRedisTemplate().opsForHash().get(REDIS_PREFIX + id, key);
    }

    /**
     * 返回所有键值对
     *
     * @return map
     */
    @Override
    public Map<String, Object> getAll() {
        Map<String, Object> map = new HashMap<>();
        if (id != null) {
            RedisConfig.getStringObjectRedisTemplate().opsForHash().entries(ApiSession.REDIS_PREFIX + id).
                    forEach((key, val) -> {
                        if (key instanceof String) {
                            map.put((String) key, val);
                        }
                    });
        }
        return map;
    }

    /**
     * 获取session id
     *
     * @param canCreate 不存在则新建
     * @return session id
     */
    @Override
    public String getId(boolean canCreate) {
        if (id == null && canCreate) {
            id = BaseSession.generalId();
            RedisConfig.getStringObjectRedisTemplate().opsForHash().put(REDIS_PREFIX + id, "sessionId", id);
            flush();
        }
        return id;
    }

    /**
     * 设置键值对
     *
     * @param key   string
     * @param value object
     */
    @Override
    public void set(String key, Object value) {
        RedisConfig.getStringObjectRedisTemplate().opsForHash().put(REDIS_PREFIX + getId(true), key, value);
    }
}
