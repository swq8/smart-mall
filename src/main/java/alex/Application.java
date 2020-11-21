package alex;

import alex.lib.ScheduledTasks;
import alex.lib.ShutdownThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@EnableScheduling
@SpringBootApplication
public class Application {
    // application dir
    public static String APP_DIR;
    public static ConfigurableApplicationContext CONTEXT;
    public static JdbcTemplate JDBC_TEMPLATE;
    public static StringRedisTemplate REDIS_TEMPLATE;
    public static String REDIS_SESSION_NAMESPACE;


    /**
     * get application dir
     *
     * @return application dir
     */
    private static String getAppDir() {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().toString();
        Pattern pattern = Pattern.compile("^jar:file:(.+?)!");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            String os = System.getProperty("os.name").toLowerCase();
            String dir = matcher.group(1);
            if (os.contains("win") && dir.charAt(0) == '/') {
                dir = dir.substring(1);
            }
            pattern = Pattern.compile("^(.*)(\\\\|/)");
            matcher = pattern.matcher(dir);
            if (matcher.find()) {
                if (os.contains("win")) {
                    return matcher.group(1).replace("/", "\\") + "\\";
                }
                return matcher.group(1) + "/";
            } else {
                Logger LOGGER = LoggerFactory.getLogger(Application.class);
                LOGGER.error(String.format("not found application dir: %s", dir));
                System.exit(-1);
                return null;
            }

        } else {
            return System.getProperty("user.dir") + File.separator;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Autowired
    private void setConfigurableApplicationContext(ConfigurableApplicationContext context) {
        CONTEXT = context;
        APP_DIR = getAppDir();
        Environment env = context.getEnvironment();
        REDIS_SESSION_NAMESPACE = env.getProperty("spring.session.redis.namespace", "spring:session:");
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        ScheduledTasks scheduledTasks = context.getBean(ScheduledTasks.class);
        scheduledTasks.cleanTemplateFiles();
    }

    @Autowired
    private void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        JDBC_TEMPLATE = jdbcTemplate;
    }

    @Autowired
    private void setRedisTemplate(StringRedisTemplate redisTemplate) {
        REDIS_TEMPLATE = redisTemplate;
    }
}
