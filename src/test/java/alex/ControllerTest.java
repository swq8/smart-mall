package alex;

import alex.entity.UserEntity;
import alex.lib.Captcha;
import alex.lib.Helper;
import alex.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ControllerTest {

    @Resource
    UserRepository userRepository;
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Timeout(1)
    public void test() throws Exception {
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/user/login")).andReturn().getResponse().getStatus());
    }

    @Timeout(1)
    public void registerTest() throws Exception {
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/user/register")).andReturn().getResponse().getStatus());
        String name = "";
        String password = Helper.randomString(8) + "1!";
        while (true) {
            name = Helper.randomString(12);
            UserEntity userEntity = userRepository.findByName(name);
            if (userEntity == null) {
                break;
            }
        }
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/user/register")
                .param("name", name)
                .param("password", password)
                .param("password1", password)
                .param("captcha", "1234")
                .sessionAttr(Captcha.SESSION_NAME, "1234");
        MvcResult registerResult = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(200, registerResult.getResponse().getStatus());
        UserEntity userEntity = userRepository.findByName(name);
        Assertions.assertNotNull(userEntity);
        userRepository.deleteById(userEntity.getId());
    }

    @Test
    public void testCaptcha() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/captcha")).andReturn();
        HttpSession httpSession = mvcResult.getRequest().getSession();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assertions.assertNotNull(httpSession);
        String val = (String) httpSession.getAttribute(Captcha.SESSION_NAME);
        Assertions.assertEquals(4, val.length());
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertTrue(response.getContentAsByteArray().length > 600);
        Assertions.assertEquals("image/png", response.getContentType());
    }

    @Autowired
    private void setWebApplicationContext(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
}
