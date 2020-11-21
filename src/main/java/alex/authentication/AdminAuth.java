package alex.authentication;

import alex.config.AdminAuthority;
import alex.config.AdminMenu;
import alex.repository.AdminUserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AdminAuth extends Auth {

    @Resource
    AdminUserRepository adminUserRepository;

    @Pointcut("execution(public * alex.controller.admin..*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void auth(JoinPoint joinPoint) throws AdminAuthException, UserAuthException {
        HttpServletRequest request = getRequest();
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        if (userToken == null) {
            throw new UserAuthException();
        }
        var adminUserEntity = adminUserRepository.findById(userToken.getId()).orElse(null);
        if (adminUserEntity == null) {
            throw new AdminAuthException();
        }
        AdminAuthority adminAuthority = new AdminAuthority(adminUserEntity.getRoleId());
        String curAction = request.getMethod().toLowerCase() + '@' + request.getRequestURI();
        if (!adminAuthority.actions.contains(curAction)
                && !curAction.equals("get@/admin/msg")
                && !curAction.equals("get@/admin")
                && !curAction.equals("get@/admin/")) {
            throw new AdminAuthException();
        }
        AdminMenu adminMenu = new AdminMenu(adminAuthority.actions);
        request.setAttribute("adminMenu", adminMenu.menu);
    }
}
