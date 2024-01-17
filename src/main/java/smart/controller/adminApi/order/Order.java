package smart.controller.adminApi.order;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.cache.ExpressCache;
import smart.entity.OrderEntity;
import smart.entity.UserEntity;
import smart.lib.ApiJsonResult;
import smart.repository.OrderRepository;
import smart.service.AdminLogService;
import smart.service.OrderService;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.dto.OrderQueryDto;

import java.util.Map;

@RestController(value = "adminApi/order/order")
@RequestMapping(path = "/adminApi/order/order", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Order {
    @Resource
    EntityManager entityManager;
    @Resource
    OrderRepository orderRepository;
    @Resource
    OrderService orderService;
    @Resource
    AdminLogService adminLogService;

    @Authorize("/order/order/query")
    @PostMapping("get")
    public ApiJsonResult get(@RequestBody OrderQueryDto query) {
        long orderNo = Helper.parseNumber(query.getNo(), Long.class);
        var orderEntity = orderService.getOrder(orderNo);
        if (orderEntity == null) {
            return ApiJsonResult.error("订单不存在");
        }
        return ApiJsonResult.success().putDataItem("order", orderEntity);
    }

    @Authorize("/order/order/pay")
    @PostMapping("pay")
    public ApiJsonResult pay(HttpServletRequest request, @RequestBody OrderEntity orderEntity) {
        var result = ApiJsonResult.successOrError(orderService.adminPay(orderEntity.getNo()));
        if (result.isSuccess()) {
            adminLogService.addLog(request, "支付订单:" + orderEntity.getNo(), null);
            entityManager.clear();
            result.putDataItem("order", orderService.getOrder(orderEntity.getNo()));
        }
        return result;
    }

    @Authorize("/order/order/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody OrderQueryDto query) {
        return ApiJsonResult.success(orderService.query(query)).putDataItem("expressCompanies", ExpressCache.getCompanies());
    }

    @Authorize("/order/order/cancelOrder")
    @PostMapping("cancelOrder")
    public ApiJsonResult cancelOrder(HttpServletRequest request, @RequestBody OrderEntity orderEntity) {
        Long orderNo = orderEntity.getNo();
        orderEntity = orderRepository.findByNoForWrite(orderNo);
        if (orderEntity == null) {
            return ApiJsonResult.error("订单不存在,no:" + orderNo);
        }
        var userEntity = DbUtils.findById(orderEntity.getUserId(), UserEntity.class);
        if (userEntity == null) {
            return ApiJsonResult.error("订单用户不存在,uid:" + orderEntity.getUserId());
        }
        var result = ApiJsonResult.successOrError(orderService.cancelOrder(userEntity, orderEntity));
        adminLogService.addLogIfSuccess(result, request, "取消订单:" + orderEntity.getNo(), "");
        return result;
    }


    @Authorize("/order/order/ship")
    @PostMapping("cancelShip")
    public ApiJsonResult cancelShip(HttpServletRequest request, @RequestBody OrderEntity orderEntity) {
        var result = ApiJsonResult.successOrError(orderService.cancelShip(orderEntity.getNo()));
        adminLogService.addLogIfSuccess(result, request, "取消发货:" + orderEntity.getNo(), "");
        return result;
    }

    @Authorize("/order/order/confirm")
    @PostMapping("confirm")
    public ApiJsonResult confirm(HttpServletRequest request, @RequestBody OrderEntity orderEntity) {
        var result = ApiJsonResult.successOrError(orderService.confirmOrder(orderEntity.getUserId(), orderEntity.getNo()));
        adminLogService.addLogIfSuccess(result, request, "确认收货:" + orderEntity.getNo(), "");
        return result;
    }

    @Authorize("/order/order/refund")
    @PostMapping("refund")
    public ApiJsonResult refund(HttpServletRequest request, @RequestBody @Validated RefundDto query) {
        var result = ApiJsonResult.successOrError(orderService.refundOrder(query.getNo(), query.getOrderStatusOnly()));
        adminLogService.addLogIfSuccess(result, request, "订单退款:" + query.getNo(), query);
        return result;
    }

    @Authorize("/order/order/ship")
    @PostMapping("ship")
    public ApiJsonResult ship(HttpServletRequest request, @RequestBody OrderEntity orderEntity) {
        var result = ApiJsonResult.successOrError(orderService.ship(orderEntity.getNo(), orderEntity.getExpressId(), orderEntity.getExpressNo()));
        if (result.isSuccess()) {
            adminLogService.addLog(request, "订单发货:" + orderEntity.getNo(),
                    Map.of("expressId", orderEntity.getExpressId(), "expressNo", orderEntity.getExpressNo()));
        }
        return result;
    }

    public static class RefundDto {
        @NotNull
        Long no;
        @NotNull
        Boolean orderStatusOnly;

        public Long getNo() {
            return no;
        }

        public void setNo(Long no) {
            this.no = no;
        }

        public Boolean getOrderStatusOnly() {
            return orderStatusOnly;
        }

        public void setOrderStatusOnly(Boolean orderStatusOnly) {
            this.orderStatusOnly = orderStatusOnly;
        }
    }
}
