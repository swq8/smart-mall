package smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import smart.lib.ShutdownThread;

import java.util.concurrent.atomic.AtomicLong;

@Component
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
    public final static String DATE_FORMAT = "yyyy-MM-dd";
    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String TIME_FORMAT = "HH:mm:ss";
    public final static String TIME_ZONE = "GMT+8";

    // application dir
    private static String appDir;

    private static ConfigurableApplicationContext context;

    private static JdbcClient jdbcClient;

    // 订单 id
    private static AtomicLong orderId;

    public static String getAppDir() {
        return appDir;
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    public static JdbcClient getJdbcClient() {
        return jdbcClient;
    }

    public static AtomicLong getOrderId() {
        return orderId;
    }


    @Autowired
    private void setConfigurableApplicationContext(ConfigurableApplicationContext context) {
        appDir = System.getProperty("user.dir");
        AppConfig.context = context;
        jdbcClient = context.getBean(JdbcClient.class);
        long orderId = jdbcClient.sql("SELECT MAX(id) as id FROM t_order").query(Long.class).optional().orElse(0L);
        AppConfig.orderId = new AtomicLong(orderId);
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }


}
