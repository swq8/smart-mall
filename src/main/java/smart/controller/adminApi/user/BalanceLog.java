package smart.controller.adminApi.user;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.lib.ApiJsonResult;
import smart.service.UserBalanceLogService;
import smart.dto.GeneralQueryDto;

@RestController(value = "adminApi/user/balanceLog")
@RequestMapping(path = "/adminApi/user/balanceLog", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class BalanceLog {
    @Resource
    UserBalanceLogService userBalanceLogService;

    @Authorize("/user/balanceLog/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(userBalanceLogService.query(query));
    }
}
