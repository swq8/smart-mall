package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smart.lib.payment.Alipay;
import smart.lib.payment.Balance;
import smart.lib.payment.Payment;
import smart.lib.payment.WeChat;
import smart.repository.PaymentRepository;
import smart.util.LogUtils;
import smart.util.Security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class PaymentCache {

    private static PaymentRepository paymentRepository;
    private static LinkedHashMap<String, Payment> payments;

    /**
     * 初始化
     */
    @PostConstruct
    public static synchronized void update() {
        LinkedHashMap<String, Payment> map = new LinkedHashMap<>();
        paymentRepository.findAllByOrderByOrderNum().forEach(row -> {
            String config = Security.aesDecrypt(row.getConfig());
            Payment payment = null;
            switch (row.getName()) {
                case Alipay.NAME -> payment = new Alipay(row.getNameCn(), row.getEnable());
                case Balance.NAME -> payment = new Balance(row.getNameCn(), row.getEnable());
                case WeChat.NAME -> payment = new WeChat(row.getNameCn(), row.getEnable());
                default -> LogUtils.error(PaymentCache.class, "unknown payment, name: " + row.getName());
            }

            if (payment != null) {
                payment.setConfig(config);
                map.put(payment.getName(), payment);
            }

        });
        payments = map;
    }

    /**
     * 根据支付名称获取支付方式
     *
     * @param name 支付名称(英文)
     * @return 支付方式
     */
    public static Payment getPaymentByName(String name) {
        return payments.get(name);
    }

    public static List<String> getAvailableNames() {
        return payments.values().stream().filter(Payment::getEnable).map(Payment::getName).toList();
    }

    public static List<Payment> getPayments() {
        return payments.values().stream().toList();
    }

    public static List<Payment> getAvailablePayments() {
        return payments.values().stream().filter(Payment::getEnable).toList();
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        PaymentCache.paymentRepository = paymentRepository;
    }
}
