package smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.annotation.Resource;

@Configuration
public class ThymeleafConfig {

    @Resource
    private AppProperties appProperties;

    @Bean
    public CustomDialect customDialect() {
        return new CustomDialect();
    }

    @Bean
    @Description("Thymeleaf template resolver")
    public ClassLoaderTemplateResolver templateResolver() {
        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        // 开发者模式下不使用模板缓存
        if (appProperties.isDevMode()) {
            templateResolver.setCacheable(false);
        }
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }
}