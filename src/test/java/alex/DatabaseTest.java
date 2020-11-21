package alex;

import alex.lib.Database;
import alex.repository.UserLogRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DatabaseTest {


    @Test
    public void EntityTest() {
        long id = Database.insertUserLog(1, 1, "msg", "ip");
        Assertions.assertTrue(id > 0);
        UserLogRepository userLogRepository = Application.CONTEXT.getBean(UserLogRepository.class);
        userLogRepository.deleteById(id);

    }
}
