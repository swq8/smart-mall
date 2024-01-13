package smart.auth;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import smart.cache.AdminMenuCache;
import smart.repository.AdminUserRepository;
import smart.service.AdminRoleService;

import java.util.Arrays;

@Aspect
@Component
public class AdminAuth extends Auth {

    @Resource
    AdminRoleService adminRoleService;
    @Resource
    AdminUserRepository adminUserRepository;


    @Pointcut("execution(public * smart.controller.adminApi..*.*(..))")
    public void pointcut() {
    }


    @Before("pointcut()")
    public void auth(JoinPoint joinPoint) throws AdminAuthException, UserAuthException {
        HttpServletRequest request = getRequest();
        if (request.getRequestURI().startsWith("/adminApi/main/")) return;

        UserToken userToken = (UserToken) request.getAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()));
        if (userToken == null) {
            throw new AdminAuthException(false);
        }
        var adminUserEntity = adminUserRepository.findByUserId(userToken.getId());
        if (adminUserEntity == null || !adminUserEntity.getEnable()) {
            throw new AdminAuthException(false);
        }

        var authorizeIds = adminRoleService.getAuthorizeIdsByRolesId(adminUserEntity.getRolesId());
        var authorizeNames = AdminMenuCache.getEnabledAuthorizeByIds(authorizeIds);
        AdminToken adminToken = new AdminToken(authorizeNames);
        userToken.assign(adminToken);
        request.setAttribute(StringUtils.uncapitalize(AdminToken.class.getSimpleName()), adminToken);

        // System built-in administrator skips authorize check
        if (ObjectUtils.containsElement(adminUserEntity.getRolesId().split(","), "1")) return;

        var signature = (MethodSignature) joinPoint.getSignature();
        var authorize = signature.getMethod().getAnnotation(Authorize.class);
        if (authorize == null || authorize.value() == null) throw new AdminAuthException(true);

        // Specially designed for Account demo
        if (userToken.getName().equals("demo")) {
            if (Arrays.stream(authorize.value()).anyMatch(item -> item.endsWith("/query") && !item.equals("/system/config/query")))
                return;
            else throw new AdminAuthException(true);
        }

        for (var item : authorizeNames) if (ObjectUtils.containsElement(authorize.value(), item)) return;
        throw new AdminAuthException(true);
    }
}
