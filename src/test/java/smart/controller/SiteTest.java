package smart.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;
import smart.lib.Captcha;
import smart.util.Helper;
import smart.util.UserAgent;
import smart.lib.session.Session;

import java.util.Objects;

@AutoConfigureMockMvc
@SpringBootTest
@Timeout(2)
public class SiteTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCaptcha() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/captcha")).andReturn().getResponse();
        String sid = Objects.requireNonNull(response.getCookie(Session.COOKIE_NAME)).getValue();
        Session session = new Session(sid);
        String code = (String) session.get(StringUtils.uncapitalize(Captcha.class.getSimpleName()));
        Assertions.assertEquals(Captcha.SIZE, code.length());
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertTrue(response.getContentAsByteArray().length > 600);
        var contentType = response.getContentType();
        Assertions.assertNotNull(contentType);
        Assertions.assertTrue(contentType.startsWith("image/png"));
    }

    @Test
    public void testPage() throws Exception {
        Assertions.assertEquals(405, mockMvc.perform(MockMvcRequestBuilders.post("/")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/category")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/category").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/list")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/list").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
    }

}
