package smart.controller.adminApi.system;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.dto.GeneralQueryDto;

@RestController(value = "adminApi/system/log")
@RequestMapping(path = "/adminApi/system/log", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Log {
    @Resource
    AdminLogService adminLogService;

    @Authorize("/system/log/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(adminLogService.query(query));
    }

}
