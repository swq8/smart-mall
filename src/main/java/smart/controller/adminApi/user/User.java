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
import smart.dto.GeneralQueryDto;
import smart.dto.IdDto;
import smart.entity.UserBalanceLogEntity;
import smart.entity.UserEntity;
import smart.lib.ApiJsonResult;
import smart.repository.UserRepository;
import smart.service.AdminLogService;
import smart.service.UserAddressService;
import smart.service.UserService;
import smart.util.DbUtils;
import smart.util.Security;
import smart.util.ValidatorUtils;
import smart.util.validategroups.Add;

import java.util.Map;

@RestController(value = "adminApi/user/user")
@RequestMapping(path = "/adminApi/user/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class User {
    @Resource
    AdminLogService adminLogService;

    @Resource
    UserAddressService userAddressService;
    @Resource
    UserRepository userRepository;

    @Resource
    UserService userService;

    @Authorize("/user/user/changeBalance")
    @PostMapping("changeBalance")
    public ApiJsonResult changeBalance(HttpServletRequest request,
                                       @RequestBody @Validated(Add.class) UserBalanceLogEntity logEntity) {
        if (logEntity.getAmount() == 0) {
            return ApiJsonResult.error("无需调整");
        }
        var userEntity = userRepository.findByIdForWrite(logEntity.getUid());
        if (userEntity == null) {
            return ApiJsonResult.error("用户不存在");
        }
        var result = ApiJsonResult.successOrError(userService.changeBalance(userEntity, logEntity.getAmount(), logEntity.getNote()));
        adminLogService.addLogIfSuccess(result, request, "调整用户余额", logEntity);
        return result;
    }

    @Authorize("/user/user/query")
    @PostMapping("get")
    public ApiJsonResult get(@RequestBody @Validated IdDto idDto) {
        var entity = DbUtils.findById(idDto.getId(), UserEntity.class);
        if (entity == null) {
            ApiJsonResult.error("用户不存在, id:" + idDto.getId());
        }
        return ApiJsonResult.success()
                .putDataItem("address", userAddressService.findByUserId(idDto.getId()))
                .putDataItem("record", entity);
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
        String pwd = query.getPwd();
        pwd = Security.rsaDecrypt(SystemCache.getRsaPrivateKey(), pwd);
        String msg = ValidatorUtils.validatePassword(pwd, "密码");
        if (msg != null) {
            return new ApiJsonResult().setCode(ApiJsonResult.CODE_BAD_REQUEST).setMsg(msg);
        }
        msg = userService.changePassword(userId, pwd);
        if (msg != null) {
            return new ApiJsonResult().setCode(ApiJsonResult.CODE_BAD_REQUEST).setMsg(msg);
        }
        userService.deleteSessionsByUserId(userId);
        adminLogService.addLog(request, "更改用户密码", Map.of("userId", userId));
        return ApiJsonResult.success();
    }

}
