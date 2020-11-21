package alex;

import alex.config.AdminAuthority;
import alex.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ApplicationTests {

    @Resource
    UserRepository userRepository;

    @Test
    public void contextLoads() {
        AdminAuthority adminAuthority = new AdminAuthority(1);
        System.out.println(adminAuthority);
        adminAuthority = new AdminAuthority(10);
        System.out.println(adminAuthority);
    }

}
