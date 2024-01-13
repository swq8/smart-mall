package smart.controller;

import smart.cache.PaymentCache;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调通知
 */
@RestController
@RequestMapping(path = "payNotify", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class PayNotify {
    @PostMapping("{name}")
    public String getName(@PathVariable String name, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> map.put(k, v[0]));
        var payment = PaymentCache.getPaymentByName(name);
        if (payment != null && payment.notify(map)) {
            return payment.getSuccessResponse();
        }
        return null;
    }
}
