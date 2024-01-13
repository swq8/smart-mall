package smart.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import smart.cache.PaymentCache;
import smart.entity.PaymentEntity;
import smart.repository.PaymentRepository;
import smart.util.Json;
import smart.util.Security;
import smart.util.ValidatorUtils;
import smart.util.validategroups.Edit;

import java.sql.Timestamp;

@Service
public class PaymentService {
    @Resource
    PaymentRepository paymentRepository;

    /**
     * @param jsonStr like {"name":"alipay","enable":true,"orderNum":9, "xxx": "xxx"...}
     * @return null if success, or error msg
     */
    public String save(String jsonStr) {
        var postEntity = Json.parse(jsonStr, PaymentEntity.class);
        if (postEntity == null) return "数据错误";
        var msg = ValidatorUtils.validate(postEntity, Edit.class);
        if (msg != null) return msg;
        var entity = paymentRepository.findByNameForWrite(postEntity.getName());
        var payment = PaymentCache.getPaymentByName(postEntity.getName());
        if (entity == null || payment == null) return "支付方式不存在, name:" + postEntity.getName();
        var configObj = payment.newConfigInstance(jsonStr);
        msg = ValidatorUtils.validate(configObj);
        if (msg != null) return msg;
        var configJson = Json.stringify(configObj);
        if (configJson == null) configJson = "{}";
        entity.setConfig(Security.aesEncrypt(configJson));
        entity.setEnable(postEntity.getEnable());
        entity.setOrderNum(postEntity.getOrderNum());
        entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        paymentRepository.saveAndFlush(entity);
        PaymentCache.update();
        return null;
    }
}
