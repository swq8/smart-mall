package smart.lib;

import smart.lib.session.Session;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import smart.util.Helper;

public class AdminHelper {

    /**
     * 使用json result机制通知客户端跳转消息页面查看消息
     *
     * @param jsonResult 要展示的信息
     * @param request    http request
     * @return json
     */
    public static String msgPage(JsonResult jsonResult, HttpServletRequest request) {
        Session session = Session.from(request);
        session.set("msg", jsonResult.getMsg());
        jsonResult.setMsg(null);
        String url = jsonResult.getUrl();
        if (url != null) {
            if (!url.startsWith("/")) {
                String uri = request.getRequestURI();
                url = uri.substring(0, uri.lastIndexOf("/") + 1) + url;
            }
            session.set("backUrl", url);
        }
        jsonResult.setUrl("/admin/msg");
        return jsonResult.toString();
    }


    /**
     * 渲染消息页面
     *
     * @param msg     要展示的消息
     * @param request http request
     * @return 消息页面
     */
    public static ModelAndView msgPage(String msg, HttpServletRequest request) {
        return msgPage(msg, null, request);
    }


    /**
     * 渲染消息页面
     *
     * @param msg     要展示的消息
     * @param backUrl 返回url
     * @param request http request
     * @return 消息页面
     */
    public static ModelAndView msgPage(String msg, String backUrl, HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/msg", request);
        modelAndView.addObject("msg", msg);
        modelAndView.addObject("backUrl", backUrl);
        modelAndView.addObject("title", msg);
        return modelAndView;
    }
}
