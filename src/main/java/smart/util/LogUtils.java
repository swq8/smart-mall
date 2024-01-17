package smart.util;

import org.apache.commons.logging.LogFactory;


public class LogUtils {
    public static void error(Class<?> clazz, Object message) {
        log("error", clazz.getName(), message, null);
    }

    public static void error(Class<?> clazz, Object message, Throwable t) {
        log("error", clazz.getName(), message, t);
    }

    public static void info(Class<?> clazz, Object message) {
        log("info", clazz.getName(), message, null);
    }

    public static void info(Class<?> clazz, Object message, Throwable t) {
        log("info", clazz.getName(), message, t);
    }

    public static void warn(Class<?> clazz, Object message) {
        log("warn", clazz.getName(), message, null);
    }

    public static void warn(Class<?> clazz, Object message, Throwable t) {
        log("warn", clazz.getName(), message, t);
    }

    static void log(String level, String name, Object message, Throwable t) {
        var log = LogFactory.getLog(name);
        switch (level) {
            case "error" -> {
                if (t == null) {
                    log.error(message);
                } else {
                    log.error(message, t);
                }
            }
            case "info" -> {
                if (t == null) {
                    log.info(message);
                } else {
                    log.info(message, t);
                }
            }
            case "warn" -> {
                if (t == null) {
                    log.warn(message);
                } else {
                    log.warn(message, t);
                }
            }
            default -> log.error("unknown level");
        }
    }
}
