package smart.lib.payment;

import jakarta.validation.constraints.NotBlank;
import smart.util.Helper;
import smart.util.Json;

import java.util.Map;

public class WeChat implements Payment {
    ConfigDto configDto = new ConfigDto();
    public static final String NAME = "wechat";
    private final String nameCn;

    private final boolean enable;

    public WeChat(String nameCn, boolean enable) {
        this.nameCn = nameCn;
        this.enable = enable;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getNameCn() {
        return nameCn;
    }

    @Override
    public Object getConfig() {
        return configDto;
    }

    @Override
    public boolean getEnable() {
        return enable;
    }

    @Override
    public Object newConfigInstance(String json) {
        return Json.parse(json, ConfigDto.class);
    }

    @Override
    public void setConfig(String json) {
        var configTmp = (ConfigDto) newConfigInstance(json);
        if (configTmp != null) {
            configDto = configTmp;
        }
    }

    @Override
    public String getQrCode(String title, String orderNo, long amount) throws Exception {
        return Helper.randomString(100);
    }

    @Override
    public String getSuccessResponse() {
        return null;
    }

    @Override
    public boolean notify(Map<String, String> map) {
        return false;
    }

    @Override
    public String refund(long orderNo, long amount) {
        return null;
    }
    public static class ConfigDto {
        @NotBlank
        private String appId;
        @NotBlank
        private String key1;

        public String getAppId() {
            return appId;
        }

        public String getKey1() {
            return key1;
        }
    }
}
