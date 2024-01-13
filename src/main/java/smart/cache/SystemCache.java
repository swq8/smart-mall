package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import smart.dto.other.CarouselDto;
import smart.dto.other.StaticResDto;
import smart.repository.SystemRepository;
import smart.util.Helper;
import smart.util.Json;
import smart.util.Security;

import java.util.ArrayList;
import java.util.List;

@Component
public class SystemCache {
    //备案号
    private static String beian;

    // 首页轮播
    private static CarouselDto carousel;
    private static List<String> keywords;
    private static String keywordsStr;
    private static long maxBuyNum;

    private static String ossAk;
    private static String ossAks;
    private static String ossBucket;
    private static String ossBucketUrl;
    private static String ossEndpoint;

    private static String rsaPrivateKey;
    private static String rsaPublicKey;

    //site name
    private static String siteName;
    private static StaticResDto staticRes;
    private static String storageType;

    private static String themePc;
    private static String themeMobile;
    //site url
    private static String url;
    private static SystemRepository systemRepository;

    @PostConstruct
    public synchronized static void update() {
        List<String> keywords1 = new ArrayList<>();
        systemRepository.findAll().forEach(entity -> {
            if (entity.getEntity().equals("other")) {
                switch (entity.getAttribute()) {
                    case "carousel" -> setCarousel(entity.getValue());
                    case "staticRes" -> setStaticRes(entity.getValue());
                }
                return;
            }
            if (entity.getEntity().equals("sys")) {
                switch (entity.getAttribute()) {
                    case "beian" -> beian = entity.getValue();
                    case "keywords" -> {
                        keywordsStr = entity.getValue();
                        for (var str : keywordsStr.split(",")) {
                            str = str.trim();
                            if (!str.isEmpty()) {
                                keywords1.add(str);
                            }
                        }
                        keywords = keywords1;
                    }
                    case "maxBuyNum" -> maxBuyNum = Helper.parseNumber(entity.getValue(), Long.class);
                    case "rsaPrivateKey" -> rsaPrivateKey = entity.getValue();
                    case "rsaPublicKey" -> rsaPublicKey = entity.getValue();
                    case "siteName" -> siteName = entity.getValue();
                    case "url" -> url = entity.getValue();
                }
                return;
            }
            if (entity.getEntity().equals("storage")) {
                switch (entity.getAttribute()) {
                    case "type" -> storageType = entity.getValue();
                    case "ossAk" -> ossAk = Security.aesDecrypt(entity.getValue());
                    case "ossAks" -> ossAks = Security.aesDecrypt(entity.getValue());
                    case "ossBucket" -> ossBucket = entity.getValue();
                    case "ossBucketUrl" -> ossBucketUrl = entity.getValue();
                    case "ossEndpoint" -> ossEndpoint = entity.getValue();
                }
                return;
            }
            if (entity.getEntity().equals("theme")) {
                switch (entity.getAttribute()) {
                    case "mobile" -> themeMobile = entity.getValue();
                    case "pc" -> themePc = entity.getValue();
                }
            }
        });
    }

    /**
     * 获取备案号
     *
     * @return 备案号
     */
    public static String getBeian() {
        return beian;
    }

    /**
     * 获取首页轮播JSON
     *
     * @return json string
     */
    public static CarouselDto getCarousel() {
        return carousel;
    }

    /**
     * 更新首页轮播JSON
     *
     * @param json 首页轮播JSON
     */
    public static void setCarousel(String json) {
        if (!StringUtils.hasText(json)) json = "{}";
        SystemCache.carousel = Json.parse(json, CarouselDto.class);
    }

    public static List<String> getKeywords() {
        return keywords;
    }

    public static String getKeywordsStr() {
        return keywordsStr;
    }

    public static long getMaxBuyNum() {
        return maxBuyNum;
    }

    public static String getOssAk() {
        return ossAk;
    }

    public static String getOssAks() {
        return ossAks;
    }

    public static String getOssBucket() {
        return ossBucket;
    }

    public static String getOssBucketUrl() {
        return ossBucketUrl;
    }

    public static String getOssEndpoint() {
        return ossEndpoint;
    }

    public static String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public static String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public static String getSiteName() {
        return siteName;
    }

    public static StaticResDto getStaticRes() {
        return staticRes;
    }

    public static void setStaticRes(String json) {
        if (!StringUtils.hasText(json)) json = "{}";
        SystemCache.staticRes = Json.parse(json, StaticResDto.class);
    }

    public static String getStorageType() {
        return storageType;
    }

    public static String getThemeMobile() {
        return themeMobile;
    }

    public static String getThemePc() {
        return themePc;
    }

    public static String getUrl() {
        return url;
    }

    @Autowired
    private void autowire(SystemRepository systemRepository) {
        SystemCache.systemRepository = systemRepository;
    }
}
