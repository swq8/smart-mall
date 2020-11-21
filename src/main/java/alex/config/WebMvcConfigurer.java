package alex.config;

import alex.storage.LocalStorage;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfigurer extends WebMvcConfigurationSupport {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl
                        .maxAge(10, TimeUnit.MINUTES)
                        .cachePublic())
                .resourceChain(true);

        // upload directory mapping
        Path pathUploadDir = Paths.get(LocalStorage.UPLOAD_DIR);
        if (Files.notExists(pathUploadDir)) {
            try {
                Files.createDirectory(pathUploadDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + LocalStorage.UPLOAD_DIR)
                .setCacheControl(CacheControl
                        .maxAge(10, TimeUnit.MINUTES)
                        .cachePublic())
                .resourceChain(true);

    }
}

