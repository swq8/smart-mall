package smart.config;

import org.springframework.util.StringUtils;
import smart.auth.UserToken;
import smart.cache.ArticleCache;
import smart.cache.CategoryCache;
import smart.cache.SystemCache;
import smart.util.Helper;
import smart.lib.session.Session;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * http interceptor
 */
@SuppressWarnings("NullableProblems")
public class HttpInterceptor implements HandlerInterceptor {
    /**
     * initial global parameters
     * @param request req
     * @param response res
     * @param handler handler
     * @return true
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        Session session = new Session(request, response);
        request.setAttribute(StringUtils.uncapitalize(Session.class.getSimpleName()), session);
        request.setAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()), UserToken.from(session));

        request.setAttribute("articleList", ArticleCache.getList());
        request.setAttribute("beian", SystemCache.getBeian());
        request.setAttribute("jsPath", SystemCache.getStaticRes().getPath());
        request.setAttribute("siteName", SystemCache.getSiteName());
        request.setAttribute("siteUrl", SystemCache.getUrl());
        request.setAttribute("categoryNodes", CategoryCache.getNodes());
        request.setAttribute("keywords", SystemCache.getKeywords());
        //Delete later
        request.setAttribute("now", new Date());
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }
}
