package smart.controller.adminApi.other;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import smart.auth.Authorize;
import smart.cache.SystemCache;
import smart.dto.other.StaticResDto;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.SystemService;


@RestController(value = "adminApi/other/staticRes")
@RequestMapping(path = "/adminApi/other/staticRes", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class StaticRes {
    @Resource
    AdminLogService adminLogService;
    @Resource
    SystemService systemService;


    @Authorize("/other/staticRes/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated StaticResDto staticRes, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(systemService.saveStaticRes(staticRes));
        adminLogService.addLogIfSuccess(result, request, "编辑静态文件", staticRes);
        return ApiJsonResult.success();
    }

    @Authorize("/other/staticRes/query")
    @PostMapping("get")
    public ApiJsonResult get() {
        return ApiJsonResult.success()
                .putDataItem("record", SystemCache.getStaticRes());
    }
}
