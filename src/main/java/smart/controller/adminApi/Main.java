package smart.controller.adminApi;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.UserToken;
import smart.cache.AdminMenuCache;
import smart.cache.SystemCache;
import smart.entity.AdminUserEntity;
import smart.lib.ApiJsonResult;
import smart.lib.session.Session;
import smart.repository.AdminUserRepository;
import smart.service.AdminLogService;
import smart.service.AdminMenuService;
import smart.service.AdminRoleService;
import smart.service.AdminUserService;
import smart.util.Helper;
import smart.util.Security;
import smart.util.ValidatorUtils;
import smart.dto.GeneralQueryDto;


/**
 * admin api
 * 管理员接口
 */
@RestController("adminApi/main")
@RequestMapping(path = "/adminApi/main", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Main {

    @Resource
    AdminLogService adminLogService;
    @Resource
    AdminMenuService adminMenuService;

    @Resource
    AdminRoleService adminRoleService;

    @Resource
    AdminUserRepository adminUserRepository;
    @Resource
    AdminUserService adminUserService;

    /**
     * get user info
     *
     * @return user info , menu, permission
     */
    @PostMapping("info")
    public ApiJsonResult getInfo(UserToken userToken) {
        AdminUserEntity adminUserEntity;
        ApiJsonResult apiJsonResult = ApiJsonResult.success();
        if (userToken == null
                || (adminUserEntity = adminUserRepository.findByUserId(userToken.getId())) == null
                || !adminUserEntity.getEnable()) {
            return apiJsonResult;
        }
        var authorizeIds = adminRoleService.getAuthorizeIdsByRolesId(adminUserEntity.getRolesId());
        var superAdmin = ObjectUtils.containsElement(adminUserEntity.getRolesId().split(","), "1");
        var authorizeNames = AdminMenuCache.getEnabledAuthorizeByIds(authorizeIds);
        return apiJsonResult
                .putDataItem("authorize", authorizeNames)
                .putDataItem("name", userToken.getName())
                .putDataItem("menu", adminMenuService.getMenu(authorizeNames, superAdmin))
                .putDataItem("superAdmin", superAdmin);
    }

    /**
     * administrator login
     *
     * @param query query
     * @return {}
     */
    @PostMapping("login")
    public ApiJsonResult login(@RequestBody GeneralQueryDto query,
                               HttpServletRequest request, Session session) {
        query.setPass(Security.rsaDecrypt(SystemCache.getRsaPrivateKey(), query.getPass()));
        if (StringUtils.hasText(query.getName())) {
            query.setName(query.getName().toLowerCase());
        }
        if (ValidatorUtils.validateNotNameAndPassword(query.getName(), query.getPass())) {
            return ApiJsonResult.error(ValidatorUtils.ID_OR_PASS_ERROR);
        }
        var loginResult = adminUserService.login(query.getName(), query.getPass(), Helper.getClientIp(request));
        if (loginResult.getError() != null) {
            return ApiJsonResult.error(loginResult.getError());
        }
        UserToken userToken = loginResult.getUserToken();
        userToken.save(session);
        request.setAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()), userToken);
        adminLogService.addLog(request, "登录成功", userToken);
        return ApiJsonResult.success("登录成功")
                .putDataItem("name", query.getName());
    }


    @PostMapping("logout")
    public ApiJsonResult logout(HttpServletRequest request, Session session, UserToken userToken) {
        session.destroy();
        if (userToken != null) {
            adminLogService.addLog(request, "退出登录", userToken);
        }
        return ApiJsonResult.success();
    }

    /**
     * @return RSA public key
     */
    @PostMapping("rsaPubKey")
    public ApiJsonResult getRsaPubKey() {
        return ApiJsonResult
                .success("")
                .putDataItem("rsaPubKey", SystemCache.getRsaPublicKey());
    }
}
