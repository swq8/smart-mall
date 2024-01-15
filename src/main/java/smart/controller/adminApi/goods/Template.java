package smart.controller.adminApi.goods;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smart.auth.Authorize;
import smart.cache.SystemCache;
import smart.controller.adminApi.General;
import smart.dto.GoodsTemplateDto;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.SystemService;
import smart.util.Helper;

import java.util.Map;
import java.util.Objects;

@RestController(value = "adminApi/goods/template")
@RequestMapping(path = "/adminApi/goods/template", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Template {
    @Resource
    AdminLogService adminLogService;
    @Resource
    SystemService systemService;


    @Authorize("/goods/template/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated GoodsTemplateDto goodsTemplate, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(systemService.saveGoodsTemplate(goodsTemplate));
        adminLogService.addLogIfSuccess(result, request, "编辑商品模板", goodsTemplate);
        return result;
    }

    @Authorize("/goods/template/query")
    @PostMapping("get")
    public ApiJsonResult get() {
        return ApiJsonResult.success().putDataItem("record", SystemCache.getGoodsTemplate());
    }

    @Authorize("/goods/template/edit")
    @PostMapping("upload")
    public ApiJsonResult upload(HttpServletRequest request,
                                @RequestParam(name = "file") MultipartFile file) {
        var msg = General.checkFile(file, 501_000, true);
        if (msg != null) return ApiJsonResult.error(msg);
        var result = General.upload(request, file);
        if (result.isSuccess()) {
            adminLogService.addLog(request, "上传图片(商品模板)",
                    Map.of("src", Objects.requireNonNull(file.getOriginalFilename()),
                            "size", Helper.getSizes(file.getSize()),
                            "target", result.getData().get("url")
                    ));
        }
        return result;
    }
}
