package alex.controller;

import alex.lib.Helper;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * custom error page
 */
@Controller
public class Error implements ErrorController {

    private static final String PATH = "/error";
    private final ErrorAttributes errorAttributes;

    public Error(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;

    }

    @RequestMapping(value = PATH)
    public ModelAndView error(HttpServletRequest request) {
        ModelAndView modelAndViw = Helper.newModelAndView("error", request);
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        Map<String, Object> error = this.errorAttributes.getErrorAttributes(servletWebRequest, ErrorAttributeOptions.defaults());
        int statusCode = (Integer) error.get("status");
        if (statusCode > 900) {
            statusCode = HttpStatus.NOT_FOUND.value();
            error.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        }
        request.setAttribute("statusCode", statusCode);
        request.setAttribute("error", error.get("error"));
        return modelAndViw;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
