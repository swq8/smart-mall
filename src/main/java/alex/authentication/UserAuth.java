package alex.authentication;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Aspect
@Component
public class UserAuth extends Auth {

    @Pointcut("execution(public * alex.controller.Cart.*(..))")
    public void pointcutCart() {
    }

    @Before("pointcutCart()")
    public void authCart(JoinPoint joinPoint) throws UserAuthException {
        HttpServletRequest request = getRequest();
        String uri = request.getRequestURI();
        if (uri.length() < 8) {
            return;
        }
        uri = uri.substring(6);
        if (uri.equals("buy")) {
            if (request.getAttribute(UserToken.KEY) == null) {
                throw new UserAuthException();
            }
        }

    }

    @Pointcut("execution(public * alex.controller.user..*.*(..))")
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
        if (request.getAttribute(UserToken.KEY) == null) {
            throw new UserAuthException();
        }
    }
}
