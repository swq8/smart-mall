package smart.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import smart.cache.SystemCache;
import smart.storage.LocalStorage;
import smart.storage.OssStorage;
import smart.storage.Storage;

@Component
public class StorageConfig {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Storage getStorage() {
        if ("oss".equals(SystemCache.getStorageType())) {
            return new OssStorage();
        } else {
            return new LocalStorage();
        }
    }
}
