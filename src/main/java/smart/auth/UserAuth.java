package smart.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        if ("buy".equals(uri)) {
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
        if ("login".equals(uri) || "register".equals(uri)) {
            return;
        }
        if (request.getAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName())) == null) {
            throw new UserAuthException();
        }
    }
}
