package smart.lib.payment;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.service.OrderService;
import smart.util.Helper;
import smart.util.Json;
import smart.util.LogUtils;

import java.math.BigDecimal;
import java.util.Map;

public class Alipay implements Payment {

    public static final String NAME = "alipay";
    private final String nameCn;
    private final boolean enable;
    private ConfigDto configDto = new ConfigDto();

    public Alipay(String nameCn, boolean enable) {
        this.nameCn = nameCn;
        this.enable = enable;
    }


    @Override
    public String getName() {
        // 英文名称
        return NAME;
    }

    @Override
    public String getNameCn() {
        return nameCn;
    }

    @Override
    public boolean getEnable() {
        return enable;
    }

    /**
     * 获取收款码代码
     *
     * @param title   交易标题
     * @param orderNo 订单号
     * @param amount  订单金额
     * @return 收款码
     * @throws Exception error
     */
    @Override
    public String getQrCode(String title, String orderNo, long amount) throws Exception {

        AlipayTradePrecreateResponse alipayResponse = Factory.Payment.FaceToFace()
                .preCreate(title, orderNo, Helper.priceFormat(amount));
        if (ResponseChecker.success(alipayResponse)) {

            return alipayResponse.qrCode;
        }
        throw new Exception(alipayResponse.msg + "," + alipayResponse.subMsg);
    }

    @Override
    public String getSuccessResponse() {
        return "SUCCESS";
    }

    @Override
    public boolean notify(Map<String, String> map) {
        try {
            if (Factory.Payment.Common().verifyNotify(map)
                    // 需要交易成功状态
                    && "TRADE_SUCCESS".equals(map.get("trade_status"))) {
                long orderNo = Helper.parseNumber(map.get("out_trade_no"), Long.class);
                BigDecimal decimal = new BigDecimal(map.get("buyer_pay_amount"));
                decimal = decimal.multiply(new BigDecimal("100"));
                long payAmount = Helper.parseNumber(decimal, Long.class);
                OrderService orderService = AppConfig.getContext().getBean(OrderService.class);
                if (orderService.pay(orderNo, NAME, payAmount, map.get("trade_no")) == null) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtils.error(getClass(), e);
        }
        return false;
    }


    @Override
    public String refund(long orderNo, long amount) {
        try {
            AlipayTradeRefundResponse response = Factory.Payment.Common().refund(Long.toString(orderNo), Helper.priceFormat(amount));
            if (!"Success".equals(response.msg)) {
                return response.subMsg;
            }
        } catch (Exception e) {
            LogUtils.error(getClass(), e);
            return e.getMessage();
        }
        return null;
    }

    @Override
    public ConfigDto getConfig() {
        return configDto;
    }

    @Override
    public void setConfig(String json) {
        var configTmp = (ConfigDto) newConfigInstance(json);
        if (configTmp != null) {
            configDto = configTmp;
        }
        if (configDto.sanBox == null) {
            configDto.sanBox = true;
        }
        Config config = new Config();
        config.protocol = "https";

        // 正式 openapi.alipay.com
        // 沙箱 openapi-sandbox.dl.alipaydev.com
        if (configDto.getSanBox()) {
            config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";
        } else {
            config.gatewayHost = "openapi.alipay.com";
        }
        config.signType = "RSA2";
        config.appId = configDto.getAppId();
        config.merchantPrivateKey = configDto.getMerchantPrivateKey();
        config.alipayPublicKey = configDto.getAlipayPublicKey();
        //异步通知接收服务地址（可选）
        config.notifyUrl = SystemCache.getUrl() + "/payNotify/" + getName();
        // 设置参数（全局只需设置一次）
        Factory.setOptions(config);
    }

    @Override
    public Object newConfigInstance(String json) {
        return Json.parse(json, ConfigDto.class);
    }

    public static class ConfigDto {
        @NotBlank
        private String appId;
        @NotBlank
        private String merchantPrivateKey;
        @NotBlank
        private String alipayPublicKey;

        @NotNull
        private Boolean sanBox;

        public String getAppId() {
            return appId;
        }

        public String getMerchantPrivateKey() {
            return merchantPrivateKey;
        }

        public String getAlipayPublicKey() {
            return alipayPublicKey;
        }

        public Boolean getSanBox() {
            return sanBox;
        }
    }
}
