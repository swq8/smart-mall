package smart.service;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import smart.cache.ExpressCache;
import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.config.RedisConfig;
import smart.dto.GoodsTemplateDto;
import smart.dto.express.FeeRuleDto;
import smart.dto.express.FreeRuleDto;
import smart.dto.other.CarouselDto;
import smart.dto.other.StaticResDto;
import smart.repository.SystemRepository;
import smart.util.Helper;
import smart.util.Json;
import smart.util.Security;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SystemService {

    @Resource
    JdbcClient jdbcClient;

    @Resource
    SystemRepository systemRepository;

    private static String getString(RuntimeMXBean runtimeMXBean) {
        long upTime = runtimeMXBean.getUptime();
        long days = TimeUnit.MILLISECONDS.toDays(upTime);
        upTime -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(upTime);
        upTime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(upTime);
        upTime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(upTime);
        String upTimeStr = "";
        if (days == 1) {
            upTimeStr = "1 day, ";
        } else if (days > 1) {
            upTimeStr = String.format("%d days, ", days);
        }
        upTimeStr += String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return upTimeStr;
    }

    public ConfigDto getConfig() {
        ConfigDto result = new ConfigDto();
        result.setBeian(SystemCache.getBeian());
        result.setKeywords(SystemCache.getKeywordsStr());
        result.setMaxBuyNum(SystemCache.getMaxBuyNum());
        result.setOssAk(SystemCache.getOssAk());
        result.setOssAks(SystemCache.getOssAks());
        result.setOssBucket(SystemCache.getOssBucket());
        result.setOssBucketUrl(SystemCache.getOssBucketUrl());
        result.setOssEndpoint(SystemCache.getOssEndpoint());
        result.setSiteName(SystemCache.getSiteName());
        result.setSiteUrl(SystemCache.getUrl());
        result.setStorageType(SystemCache.getStorageType());
        result.setThemeMobile(SystemCache.getThemeMobile());
        result.setThemePc(SystemCache.getThemePc());
        return result;
    }

    public String saveCarousel(CarouselDto carousel) {
        var entity = systemRepository.findByEaForWrite("other", "carousel");
        if (entity == null) return "记录不存在";
        entity.setValue(Json.stringify(carousel));
        systemRepository.saveAndFlush(entity);
        SystemCache.update();
        return null;
    }

    public String saveConfig(ConfigDto config) {
        systemRepository.updateValueByEa(config.beian, "sys", "beian");
        systemRepository.updateValueByEa(config.keywords, "sys", "keywords");
        systemRepository.updateValueByEa(config.maxBuyNum, "sys", "maxBuyNum");
        systemRepository.updateValueByEa(config.ossAk, "storage", Security.aesEncrypt("ossAk"));
        systemRepository.updateValueByEa(config.ossAks, "storage", Security.aesEncrypt("ossAks"));
        systemRepository.updateValueByEa(config.ossBucket, "storage", "ossBucket");
        systemRepository.updateValueByEa(config.ossBucketUrl, "storage", "ossBucketUrl");
        systemRepository.updateValueByEa(config.ossEndpoint, "storage", "ossEndpoint");
        systemRepository.updateValueByEa(config.siteName, "sys", "siteName");
        systemRepository.updateValueByEa(config.siteUrl, "sys", "url");
        systemRepository.updateValueByEa(config.storageType, "storage", "type");
        systemRepository.updateValueByEa(config.themeMobile, "theme", "mobile");
        systemRepository.updateValueByEa(config.themePc, "theme", "pc");
        SystemCache.update();
        return null;
    }

    public String saveExpressFeeRule(FeeRuleDto feeRule) {
        var entity = systemRepository.findByEaForWrite("shipping", "feeRule");
        if (entity == null) return "记录不存在";
        List<Long> provinces = new ArrayList<>();
        for (var item : feeRule.getProvinceFees()) {
            if (item.getProvinces().stream().anyMatch(provinces::contains)) return "同地区存在多条规则";
            provinces.addAll(item.getProvinces());
        }
        entity.setValue(Json.stringify(feeRule));
        systemRepository.saveAndFlush(entity);
        ExpressCache.update();
        return null;
    }

    public String saveExpressFreeRule(FreeRuleDto freeRule) {
        var entity = systemRepository.findByEaForWrite("shipping", "freeRule");
        if (entity == null) return "记录不存在";
        entity.setValue(Json.stringify(freeRule));
        systemRepository.saveAndFlush(entity);
        ExpressCache.update();
        return null;
    }
    public String saveGoodsTemplate(GoodsTemplateDto goodsTemplate) {
        var entity = systemRepository.findByEaForWrite("sys", "goodsTemplate");
        if (entity == null) return "记录不存在";
        entity.setValue(Json.stringify(goodsTemplate));
        systemRepository.saveAndFlush(entity);
        SystemCache.update();
        return null;
    }


    public String saveStaticRes(StaticResDto staticRes) {
        var entity = systemRepository.findByEaForWrite("other", "staticRes");
        if (entity == null) return "记录不存在";
        staticRes.setPath(staticRes.getPath().replaceAll("\\s+", ""));
        staticRes.setVersion(staticRes.getVersion().replaceAll("\\s+", ""));
        entity.setValue(Json.stringify(staticRes));
        systemRepository.saveAndFlush(entity);
        SystemCache.update();
        return null;
    }

    public Map<String, Object> getInfo() {
        Map<String, Object> map = new HashMap<>();
        StringBuilder gc = new StringBuilder();
        for (var bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!gc.isEmpty()) gc.append(", ");
            gc.append(bean.getName());
        }
        Runtime rt = Runtime.getRuntime();
        var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String upTimeStr = getString(runtimeMXBean);


        map.put("dbVersion", jdbcClient.sql("select version()").query(String.class).single());
        map.put("dbTxIsolation", jdbcClient.sql("SELECT @@transaction_isolation").query(String.class).single());
        map.put("javaOsName", System.getProperty("os.name"));
        map.put("javaOsVersion", System.getProperty("os.version"));
        map.put("javaName", runtimeMXBean.getVmName());
        map.put("javaVersion", runtimeMXBean.getVmVersion());
        map.put("javaVendor", runtimeMXBean.getVmVendor());
        map.put("vmInfo", System.getProperty("java.vm.info"));
        map.put("args", runtimeMXBean.getInputArguments().toString());
        map.put("cpuCores", Runtime.getRuntime().availableProcessors());
        map.put("javaGc", gc.toString());
        map.put("applicationDir", AppConfig.getAppDir());
        map.put("springBootVersion", SpringApplication.class.getPackage().getImplementationVersion());
        Properties redisProperties = Objects.requireNonNull(RedisConfig.getStringRedisTemplate().getConnectionFactory()).getConnection().serverCommands().info();
        assert redisProperties != null;
        map.put("redisVersion", redisProperties.getProperty("redis_version"));
        long redisMemory = Helper.parseNumber(redisProperties.getProperty("used_memory"), Long.class);

        map.put("redisMemory", Helper.getSizes(redisMemory));
        map.put("freeMemory", Helper.getSizes(rt.freeMemory()));
        map.put("maxMemory", Helper.getSizes(rt.maxMemory()));
        map.put("totalMemory", Helper.getSizes(rt.totalMemory()));
        map.put("startTime", Helper.dateFormat(new Date(runtimeMXBean.getStartTime())));
        map.put("upTime", upTimeStr);
        return map;
    }

    public static class ConfigDto {
        @NotNull
        private String beian;
        @NotNull
        private String keywords;
        @NotNull
        private Long maxBuyNum;
        @NotNull
        private String siteName;
        @NotNull
        private String siteUrl;
        @NotNull
        private String storageType;
        @NotNull
        private String ossAk;
        @NotNull
        private String ossAks;
        @NotNull
        private String ossBucket;
        @NotNull
        private String ossBucketUrl;
        @NotNull
        private String ossEndpoint;
        @NotNull
        private String themeMobile;
        @NotNull
        private String themePc;

        public String getBeian() {
            return beian;
        }

        public void setBeian(String beian) {
            this.beian = beian;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public Long getMaxBuyNum() {
            return maxBuyNum;
        }

        public void setMaxBuyNum(Long maxBuyNum) {
            this.maxBuyNum = maxBuyNum;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getSiteUrl() {
            return siteUrl;
        }

        public void setSiteUrl(String siteUrl) {
            this.siteUrl = siteUrl;
        }

        public String getStorageType() {
            return storageType;
        }

        public void setStorageType(String storageType) {
            this.storageType = storageType;
        }

        public String getOssAk() {
            return ossAk;
        }

        public void setOssAk(String ossAk) {
            this.ossAk = ossAk;
        }

        public String getOssAks() {
            return ossAks;
        }

        public void setOssAks(String ossAks) {
            this.ossAks = ossAks;
        }

        public String getOssBucket() {
            return ossBucket;
        }

        public void setOssBucket(String ossBucket) {
            this.ossBucket = ossBucket;
        }

        public String getOssBucketUrl() {
            return ossBucketUrl;
        }

        public void setOssBucketUrl(String ossBucketUrl) {
            this.ossBucketUrl = ossBucketUrl;
        }

        public String getOssEndpoint() {
            return ossEndpoint;
        }

        public void setOssEndpoint(String ossEndpoint) {
            this.ossEndpoint = ossEndpoint;
        }

        public String getThemeMobile() {
            return themeMobile;
        }

        public void setThemeMobile(String themeMobile) {
            this.themeMobile = themeMobile;
        }

        public String getThemePc() {
            return themePc;
        }

        public void setThemePc(String themePc) {
            this.themePc = themePc;
        }
    }
}
