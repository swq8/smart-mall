package smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;


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

    public RedisConfig(StringRedisTemplate redisTemplate) {
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
        stringObjectRedisTemplate.setDefaultSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        stringObjectRedisTemplate.setKeySerializer(RedisSerializer.string());
        stringObjectRedisTemplate.setValueSerializer(RedisSerializer.string());
        stringObjectRedisTemplate.setHashKeySerializer(RedisSerializer.string());
        stringObjectRedisTemplate.setHashValueSerializer(RedisSerializer.string());
        return stringObjectRedisTemplate;
    }
}
