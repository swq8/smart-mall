package smart.controller.adminApi.express;

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
import smart.cache.ExpressCache;
import smart.cache.RegionCache;
import smart.dto.express.FeeRuleDto;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.SystemService;

import java.util.Map;


@RestController(value = "adminApi/express/feeRule")
@RequestMapping(path = "/adminApi/express/feeRule", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class FeeRule {
    @Resource
    AdminLogService adminLogService;
    @Resource
    SystemService systemService;


    @Authorize("/express/feeRule/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated FeeRuleDto feeRuleDto, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(systemService.saveExpressFeeRule(feeRuleDto));
        adminLogService.addLogIfSuccess(result, request, "编辑邮费规则", feeRuleDto);
        return result;
    }

    @Authorize("/express/feeRule/query")
    @PostMapping("info")
    public ApiJsonResult info() {
        var provinces = RegionCache.getProvinces().stream()
                .map(item -> Map.of("code", item.getCode(), "name", item.getName()))
                .toList();
        return ApiJsonResult.success()
                .putDataItem("provinces", provinces)
                .putDataItem("record", ExpressCache.getFeeRule());
    }
}
