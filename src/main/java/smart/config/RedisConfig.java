package smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@Order(0)
public class RedisConfig {
    private static StringRedisTemplate stringRedisTemplate;
    private static RedisTemplate<String, Object> stringObjectRedisTemplate;

    public static StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }

    public static RedisTemplate<String, Object> getStringObjectRedisTemplate() {
        return stringObjectRedisTemplate;
    }

    @Autowired
    private void stringRedisTemplate(StringRedisTemplate redisTemplate) {
        stringRedisTemplate = redisTemplate;
    }

    /**
     * 注册RedisTemplate<String, Object>
     *
     * @param factory RedisConnectionFactory
     * @return RedisTemplate<String, Object>
     */
    @Bean
    RedisTemplate<String, Object> getStringObjectRedisTemplate(@Autowired RedisConnectionFactory factory) {
        stringObjectRedisTemplate = new RedisTemplate<>();
        stringObjectRedisTemplate.setConnectionFactory(factory);
        stringObjectRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringObjectRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return stringObjectRedisTemplate;
    }
}
