package smart.auth;

import org.springframework.util.StringUtils;
import smart.util.Helper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class UserAuth extends Auth {

    @Pointcut("execution(public * smart.controller.Cart.*(..))")
    public void pointcutCart() {
    }

    @Before("pointcutCart()")
    public void authCart(JoinPoint joinPoint) throws UserAuthException {
        HttpServletRequest request = getRequest();
        String uri = request.getServletPath();
        if (uri.length() < 8) {
            return;
        }
        uri = uri.substring(6);
        if (uri.equals("buy")) {
            if (request.getAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName())) == null) {
                throw new UserAuthException();
            }
        }

    }

    @Pointcut("execution(public * smart.controller.user..*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void auth(JoinPoint joinPoint) throws UserAuthException {
        HttpServletRequest request = getRequest();
        String uri = request.getRequestURI();
        uri = uri.substring(6);
        if (uri.equals("login") || uri.equals("register")) {
            return;
        }
        if (request.getAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName())) == null) {
            throw new UserAuthException();
        }
    }
}
