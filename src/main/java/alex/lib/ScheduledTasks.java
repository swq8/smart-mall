package alex.lib;

import alex.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

@Component
public class ScheduledTasks {
    private static final Log log = LogFactory.getLog(ScheduledTasks.class.getName());

    @Scheduled(cron = "0 0 4 * * *")
    @SuppressWarnings("all")
    public void cleanTemplateFiles() {
        long now = Calendar.getInstance().getTimeInMillis();
        Path path = Paths.get(Application.APP_DIR + "work", "Tomcat", "localhost", "ROOT");
        log.info("Clean template directory " + path);
        File file = path.toFile();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (var tmp : files) {
                    tmp.delete();
                }
            }
        }
    }
}
