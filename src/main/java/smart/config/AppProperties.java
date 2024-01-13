package smart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "app"
)
public class AppProperties {

    // 是否为开发者模式
    private boolean devMode = false;

    // AES密钥
    private String key;

    /**
     * 是否为开发者模式
     *
     * @return boolean
     */
    public boolean isDevMode() {
        return devMode;
    }

    /**
     * 是否为开发者模式
     *
     * @param devMode boolean
     */
    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    /**
     * Secret Key
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * 密钥，用于加/解密数据,请勿随意更改
     *
     * @param key key
     */
    public void setKey(String key) {
        this.key = key;
    }
}
