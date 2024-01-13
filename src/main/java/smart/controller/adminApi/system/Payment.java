package smart.controller.adminApi.system;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.cache.PaymentCache;
import smart.entity.PaymentEntity;
import smart.lib.ApiJsonResult;
import smart.repository.PaymentRepository;
import smart.service.AdminLogService;
import smart.service.PaymentService;
import smart.util.Json;
import smart.dto.GeneralQueryDto;

@RestController(value = "adminApi/system/payment")
@RequestMapping(path = "/adminApi/system/payment", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Payment {

    @Resource
    AdminLogService adminLogService;
    @Resource
    PaymentService paymentService;

    @Resource
    PaymentRepository paymentRepository;


    @Authorize("/system/payment/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody String jsonStr, HttpServletRequest request) {
        ApiJsonResult result = ApiJsonResult.successOrError(paymentService.save(jsonStr));
        var entity = Json.parse(jsonStr, PaymentEntity.class);
        if (entity == null || result.notSuccess()) return result;
        entity.setConfig(null);
        adminLogService.addLog(request, "修改支付", entity);
        return result;
    }

    @Authorize("/system/payment/edit")
    @PostMapping("info")
    public ApiJsonResult info(@RequestBody GeneralQueryDto query) {
        var entity = paymentRepository.findByName(query.getName());
        var payment = PaymentCache.getPaymentByName(query.getName());
        if (entity == null || payment == null) return ApiJsonResult.error("支付方式不存在");
        var map = Json.parseObjectMap(Json.stringify(entity));
        map.remove("config");
        var configJson = Json.stringify(payment.getConfig());
        if (StringUtils.hasLength(configJson)) map.putAll(Json.parseObjectMap(configJson));
        return ApiJsonResult.success().putDataItem("payment", map);
    }

    @Authorize("/system/payment/query")
    @PostMapping("list")
    public ApiJsonResult list() {
        return ApiJsonResult.success().putDataItem("rows", paymentRepository.findAllByOrderByOrderNum());
    }
}
