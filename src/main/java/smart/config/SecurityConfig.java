package smart.config;

import jakarta.annotation.Resource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import smart.util.LogUtils;
import smart.util.Security;


@Configuration
public class SecurityConfig {
    @Resource
    AppProperties appProperties;

    @Bean
    public AesBytesEncryptor AesBytesEncryptor() {
        if (Strings.isEmpty(appProperties.getKey())) {
            LogUtils.error(this.getClass(), "need config: app.key");
        }
        return new AesBytesEncryptor(appProperties.getKey(), Security.sha3_256(appProperties.getKey()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
