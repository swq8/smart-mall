package alex.config;

import alex.lib.CustomDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafConfig {

    @Bean
    public CustomDialect customDialect() {
        return new CustomDialect();
    }
}