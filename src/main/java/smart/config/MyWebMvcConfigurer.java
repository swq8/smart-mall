package smart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import smart.storage.LocalStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    private static final String IMG_PATH_PATTERN = "/img/**";
    private static final String MANAGE_PATH_PATTERN = "/manage/**";
    private static final String CLASSPATH_RESOURCES_LOCATION = "classpath:/static/";
    private static final String FILE_RESOURCES_LOCATION = "file:";

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SessionArgumentResolver());
        resolvers.add(new UserTokenArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(CLASSPATH_RESOURCES_LOCATION)
                .setCacheControl(createCacheControl(30, TimeUnit.MINUTES))
                .resourceChain(true);

        // upload directory mapping
        Path pathUploadDir = Paths.get(LocalStorage.UPLOAD_DIR);
        createDirectoryIfNotExists(pathUploadDir);

        registry.addResourceHandler(IMG_PATH_PATTERN)
                .addResourceLocations(FILE_RESOURCES_LOCATION + LocalStorage.UPLOAD_DIR + File.separator)
                .setCacheControl(createCacheControl(10, TimeUnit.HOURS))
                .resourceChain(true);

        // Map the administrative directory
        registry.addResourceHandler(MANAGE_PATH_PATTERN)
                .addResourceLocations(FILE_RESOURCES_LOCATION + Paths.get(AppConfig.getAppDir(), "manage") + File.separator)
                .setCacheControl(createCacheControl(10, TimeUnit.DAYS))
                .resourceChain(true);
    }

    private CacheControl createCacheControl(int duration, TimeUnit unit) {
        return CacheControl.maxAge(duration, unit).cachePublic();
    }

    private void createDirectoryIfNotExists(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException("Could not create directory: " + path, e);
            }
        }
    }
}

