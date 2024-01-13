package smart.controller.adminApi.goods;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smart.auth.Authorize;
import smart.controller.adminApi.General;
import smart.dto.GeneralQueryDto;
import smart.dto.IdDto;
import smart.entity.SpecEntity;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.SpecService;
import smart.util.Helper;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.util.Map;
import java.util.Objects;

@RestController(value = "adminApi/goods/spec")
@RequestMapping(path = "/adminApi/goods/spec", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Spec {

    @Resource
    AdminLogService adminLogService;
    @Resource
    SpecService specService;

    @Authorize("/goods/spec/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class) SpecEntity specEntity,
                             HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(specService.save(specEntity));
        adminLogService.addLogIfSuccess(result, request, "新建商品规格", specEntity);
        return result;
    }

    @Authorize("/goods/spec/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto,
                                HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(specService.delete(idDto));
        adminLogService.addLogIfSuccess(result, request, "删除商品规格", idDto);
        return result;
    }

    @Authorize("/goods/spec/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class) SpecEntity specEntity,
                              HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(specService.save(specEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑商品规格", specEntity);
        return result;
    }

    @Authorize("/goods/spec/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(specService.query(query));
    }

    @Authorize({"/goods/spec/add", "/goods/spec/edit"})
    @PostMapping("upload")
    public ApiJsonResult upload(HttpServletRequest request,
                                @RequestParam(name = "file") MultipartFile file) {
        var msg = General.checkFile(file, 501_000, true);
        if (msg != null) return ApiJsonResult.error(msg);
        var result = General.upload(request, file);
        if (result.isSuccess()) {
            adminLogService.addLog(request, "上传图片(商品规格)",
                    Map.of("src", Objects.requireNonNull(file.getOriginalFilename()),
                            "size", Helper.getSizes(file.getSize()),
                            "target", result.getData().get("url")
                    ));
        }
        return result;
    }
}
