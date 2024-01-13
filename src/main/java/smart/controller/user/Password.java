package smart.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping(path = "user/password")
@Transactional
public class Password {

}
