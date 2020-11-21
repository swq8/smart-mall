package alex.controller.admin.sys;


import alex.lib.Helper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller(value = "admin/sys/article")
@RequestMapping(path = "/admin/sys/article")
public class Article {
    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/article/index", request);
        return modelAndView;
    }
}
