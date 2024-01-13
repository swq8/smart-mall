package smart.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Run after startup
 */
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    //ApplicationRunner接口参数格式必须是：--key=value
    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
