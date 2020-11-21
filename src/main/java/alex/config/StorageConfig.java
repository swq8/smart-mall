package alex.config;

import alex.cache.SystemCache;
import alex.storage.LocalStorage;
import alex.storage.OssStorage;
import alex.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Set;

@Configuration
public class StorageConfig {
    private String storageType;

    @Autowired
    private void getContext(ConfigurableApplicationContext context) {
        storageType = SystemCache.getStorageType();
        if (!Set.of("local", "oss").contains(storageType)) {
            storageType = "local";
        }
    }

    @Bean
    @Scope("prototype")
    Storage getStorage() {
        if (storageType.equals("oss")) {
            return new OssStorage();
        } else {
            return new LocalStorage();
        }
    }
}
