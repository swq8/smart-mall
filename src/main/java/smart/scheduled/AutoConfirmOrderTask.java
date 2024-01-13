package smart.scheduled;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import smart.entity.OrderEntity;
import smart.service.OrderService;
import smart.util.DbUtils;
import smart.util.LogUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AutoConfirmOrderTask {
    @Resource
    JdbcClient jdbcClient;

    @Resource
    OrderService orderService;

    /**
     * 5:10 execute in everyday
     */
    @Scheduled(cron = "0 10 5 * * *")
    public void autoConfirm() {
        LogUtils.info(getClass(), "auto confirm order task start");
        var orders = jdbcClient.sql("select no, user_id from t_order where status = 2 and shipping_time < ?")
                .param(LocalDateTime.now().plusDays(-10L).format(DateTimeFormatter.ofPattern("y-M-d 0:0:0")))
                .query(OrderEntity.class)
                .list();
        DbUtils.commit();
        for(var order : orders) {
            orderService.confirmOrder(order.getUserId(), order.getNo());
            DbUtils.commit();
        }
        LogUtils.info(getClass(), "auto confirm order task finish,confirm orders " + orders.size());
    }
}
