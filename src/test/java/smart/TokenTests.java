package smart;

import smart.auth.UserToken;
import smart.util.Helper;
import smart.util.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TokenTests {

    @Test
    public void test() {
        var str = Helper.randomString(4);
        UserToken userToken = new UserToken();
        userToken.setAvatar(str);
        var token = Json.parse(userToken.toString(), UserToken.class);
        assert token != null;
        Assertions.assertEquals(token.getAvatar(), str);
    }
}
