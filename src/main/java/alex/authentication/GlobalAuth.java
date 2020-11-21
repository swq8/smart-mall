package alex.authentication;

import alex.Application;
import alex.cache.CategoryCache;
import alex.cache.SystemCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Aspect
@Component
@Order(1)
public class GlobalAuth extends Auth {
    @Pointcut("execution(public * alex.controller..*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void auth(JoinPoint joinPoint) {
        HttpServletRequest request = getRequest();
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) {
            request.setAttribute(UserToken.KEY, UserToken.from("token"));
        } else {
            request.setAttribute(UserToken.KEY, UserToken.from(getRequest().getSession(false)));
        }
        request.setAttribute("beian", SystemCache.getBeian());
        request.setAttribute("siteName", SystemCache.getName());
        request.setAttribute("siteUrl", SystemCache.getUrl());
        request.setAttribute("categoryNodes", CategoryCache.getNodes());
        request.setAttribute("keywords", SystemCache.getKeywords());
        request.setAttribute("now", new Date());
    }
}
