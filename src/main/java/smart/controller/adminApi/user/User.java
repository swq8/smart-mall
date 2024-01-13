package smart.controller.adminApi.user;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.cache.SystemCache;
import smart.entity.UserBalanceLogEntity;
import smart.lib.ApiJsonResult;
import smart.repository.UserRepository;
import smart.service.AdminLogService;
import smart.service.UserService;
import smart.util.Security;
import smart.util.ValidatorUtils;
import smart.dto.GeneralQueryDto;
import smart.util.validategroups.Add;

import java.util.Map;

@RestController(value = "adminApi/user/user")
@RequestMapping(path = "/adminApi/user/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class User {
    @Resource
    AdminLogService adminLogService;

    @Resource
    UserRepository userRepository;

    @Resource
    UserService userService;


    @Authorize("/user/user/changeBalance")
    @PostMapping("changeBalance")
    public ApiJsonResult changeBalance(HttpServletRequest request,
                                       @RequestBody @Validated(Add.class) UserBalanceLogEntity logEntity) {
        if (logEntity.getAmount() == 0) return ApiJsonResult.error("无需调整");
        var userEntity = userRepository.findByIdForWrite(logEntity.getUid());
        if (userEntity == null) return ApiJsonResult.error("用户不存在");
        var result = ApiJsonResult.successOrError(userService.changeBalance(userEntity, logEntity.getAmount(), logEntity.getNote()));
        adminLogService.addLogIfSuccess(result, request, "调整用户余额", logEntity);
        return result;
    }

    @Authorize("/user/user/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(userService.query(query));
    }

    @Authorize("/user/user/changePassword")
    @PostMapping("changePassword")
    public ApiJsonResult changePassword(HttpServletRequest request, @RequestBody GeneralQueryDto query) {
        long userId = query.getId();
        String pass = query.getPass();
        pass = Security.rsaDecrypt(SystemCache.getRsaPrivateKey(), pass);
        String msg = ValidatorUtils.validatePassword(pass, "密码");
        if (msg != null) return new ApiJsonResult().setCode(ApiJsonResult.CODE_BAD_REQUEST).setMsg(msg);
        msg = userService.changePassword(userId, pass);
        if (msg != null) return new ApiJsonResult().setCode(ApiJsonResult.CODE_BAD_REQUEST).setMsg(msg);
        adminLogService.addLog(request, "更改用户密码", Map.of("userId", userId));
        return ApiJsonResult.success();
    }

}
