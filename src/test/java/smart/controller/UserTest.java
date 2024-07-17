package smart.controller;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;
import smart.config.AppConfig;
import smart.entity.UserEntity;
import smart.lib.Captcha;
import smart.lib.session.Session;
import smart.repository.UserRepository;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.UserAgent;

import java.math.BigDecimal;
import java.util.Objects;

@AutoConfigureMockMvc
@SpringBootTest
public class UserTest {
    @Resource
    UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPage() throws Exception {
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/user/login")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/user/login").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/user/register")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/user/register").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
    }

    @Test
    @Timeout(5)
    @Transactional
    public void registerTest() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders.get("/captcha")).andReturn().getResponse();
        String sid = Objects.requireNonNull(response.getCookie(Session.COOKIE_NAME)).getValue();
        Session session = new Session(sid);
        String code = (String) session.get(StringUtils.uncapitalize(Captcha.class.getSimpleName()));
        String name;
        String password = Helper.randomString(8) + "1!";
        while (true) {
            name = "test" + Helper.randomString(8);
            UserEntity userEntity = userRepository.findByName(name);
            if (userEntity == null) {
                break;
            }
        }
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/user/register").cookie(response.getCookies()).param("name", name).param("password", password).param("password1", password).param("captcha", code);
        MvcResult registerResult = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(200, registerResult.getResponse().getStatus());
        UserEntity userEntity = userRepository.findByNameForWrite(name);
        Assertions.assertNotNull(userEntity);
        userEntity.setEmail("test@test.test");
        userEntity.setBalance(BigDecimal.valueOf(99999999.99));
        userRepository.saveAndFlush(userEntity);

        String[] urls = {"/user/address", "/user/central", "/user/info", "/user/order", "/user/password"};
        for (var url : urls) {
            Assertions.assertNotEquals(200, mockMvc.perform(MockMvcRequestBuilders.get(url)).andReturn().getResponse().getStatus());
            Assertions.assertNotEquals(200, mockMvc.perform(MockMvcRequestBuilders.get(url).header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
            Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get(url).cookie(response.getCookies())).andReturn().getResponse().getStatus());
            Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get(url).cookie(response.getCookies()).header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
        }
        userRepository.deleteById(userEntity.getId());
        userRepository.flush();
        AppConfig.getJdbcClient().sql("alter table `%s` AUTO_INCREMENT=1".formatted(DbUtils.getTableName(UserEntity.class))).update();

    }

}
