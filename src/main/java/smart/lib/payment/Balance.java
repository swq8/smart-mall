package smart.lib.payment;

import smart.util.Json;

import java.math.BigDecimal;
import java.util.Map;

public class Balance implements Payment {
    public final static String NAME = "balance";
    private final String nameCn;
    private final boolean enable;
    private ConfigDto configDto = new ConfigDto();

    public Balance(String nameCn, boolean enable) {
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
    public void setConfig(String json) {
        var configTmp = (ConfigDto) newConfigInstance(json);
        if (configTmp != null) {
            configDto = configTmp;
        }

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
    public String getQrCode(String title, String orderNo, BigDecimal amount) throws Exception {
        return null;
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
    public String refund(long orderNo, BigDecimal amount) {
        return null;
    }

    public static class ConfigDto {
    }
}
