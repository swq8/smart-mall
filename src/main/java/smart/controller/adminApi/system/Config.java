package smart.controller.adminApi.system;

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
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.SystemService;

@RestController(value = "adminApi/system/config")
@RequestMapping(path = "/adminApi/system/config", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Config {
    @Resource
    AdminLogService adminLogService;
    @Resource
    SystemService systemService;

    @Authorize("/system/config/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(HttpServletRequest request, @RequestBody @Validated SystemService.ConfigDto configDto){
        var result = ApiJsonResult.successOrError(systemService.saveConfig(configDto));

        //Remove sensitive information
        configDto.setOssAk(null);
        configDto.setOssAks(null);
        adminLogService.addLogIfSuccess(result, request, "修改系统配置", configDto);
        return result;
    }

    @Authorize("/system/config/query")
    @PostMapping("")
    public ApiJsonResult query() {
        return ApiJsonResult.success().putDataItem("record", systemService.getConfig());
    }

}
