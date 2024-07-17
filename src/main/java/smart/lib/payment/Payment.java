package smart.lib.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface Payment extends Cloneable {
    /**
     * 获取英文简称
     *
     * @return 英文简称
     */
    String getName();

    /**
     * 获取中文简称
     *
     * @return 中文简称
     */
    String getNameCn();

    Object getConfig();

    boolean getEnable();

    Object newConfigInstance(String json);

    /**
     * 应用配置
     */
    void setConfig(String json);

    /**
     * 获取收款码,需要转换成QR二维码使用
     *
     * @param title   交易标题
     * @param orderNo 订单号
     * @param amount  订单金额
     * @return 收款码
     * @throws Exception error
     */
    String getQrCode(String title, String orderNo, BigDecimal amount) throws Exception;

    /**
     * 成功信息短语
     *
     * @return 返会成功信息短语
     */
    String getSuccessResponse();

    /**
     * 处理异步通知
     *
     * @param map 通知参数
     * @return 是否成功
     */
    boolean notify(Map<String, String> map);

    /**
     * 退款
     *
     * @param orderNo 订单号
     * @param amount  退款金额
     * @return null成功, 或失败信息
     */
    String refund(long orderNo, BigDecimal amount);
}
