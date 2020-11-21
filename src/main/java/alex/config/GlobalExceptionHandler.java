package alex.config;

import alex.Application;
import alex.authentication.AdminAuthException;
import alex.authentication.UserAuthException;
import alex.lib.Helper;
import alex.lib.JsonResult;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AdminAuthException.class)
    public ModelAndView adminAuthExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        if (request.getMethod().equals("GET")) {
            ModelAndView modelAndView = Helper.newModelAndView("error", request);
            modelAndView.addObject("statusCode", "");
            modelAndView.addObject("error", "无此权限");
            return modelAndView;
        } else {
            response.setContentType("application/json;charset=UTF-8");
            JsonResult jsonResult = new JsonResult();
            jsonResult.setMsg("无此权限");
            try {
                response.getWriter().write(jsonResult.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public void sizeLimitExceededException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        TemplateEngine templateEngine = Application.CONTEXT.getBean(TemplateEngine.class);
        Context context = new Context();
        context.setVariable("statusCode", HttpStatus.PAYLOAD_TOO_LARGE.value());
        context.setVariable("error", HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase());
        String html = templateEngine.process(Helper.getTheme(request) + "/error", context);
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        try {
            response.getWriter().write(html);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @ExceptionHandler(UserAuthException.class)
    public void userAuthExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        String loginUri = "/user/login";
        if (request.getMethod().equals("GET")) {
            loginUri += "?back=" + Helper.urlEncode(request.getRequestURI());
            String queryString = request.getQueryString();
            if (queryString != null) {
                loginUri += Helper.urlEncode('?' + queryString);
            }
            try {
                response.sendRedirect(loginUri);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            JsonResult jsonResult = new JsonResult();
            jsonResult.setUrl(loginUri);
            try {
                response.getWriter().write(jsonResult.toString());
                response.getWriter().write(jsonResult.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

}
