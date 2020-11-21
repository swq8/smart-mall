package alex.controller.admin.sys;

import alex.lib.Helper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller(value = "admin/sys/shipping")
@RequestMapping(path = "/admin/sys/shipping")
public class Shipping {

    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/other/shipping/index", request);
        return modelAndView;
    }

    @PostMapping(value = "", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postIndex(HttpServletRequest request) {
        return "";
    }
}
