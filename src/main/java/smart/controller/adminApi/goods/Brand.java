package smart.controller.adminApi.goods;

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
import smart.entity.BrandEntity;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.BrandService;
import smart.dto.GeneralQueryDto;
import smart.dto.IdDto;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@RestController(value = "adminApi/goods/brand")
@RequestMapping(path = "/adminApi/goods/brand", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Brand {

    @Resource
    AdminLogService adminLogService;
    @Resource
    BrandService brandService;

    @Authorize("/goods/brand/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class) BrandEntity brandEntity,
                             HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(brandService.save(brandEntity));
        adminLogService.addLogIfSuccess(result, request, "新建品牌", brandEntity);
        return result;
    }

    @Authorize("/goods/brand/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto,
                                HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(brandService.delete(idDto));
        adminLogService.addLogIfSuccess(result, request, "删除品牌", idDto);
        return result;
    }
    @Authorize("/goods/brand/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class) BrandEntity brandEntity,
                             HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(brandService.save(brandEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑品牌", brandEntity);
        return result;
    }
    @Authorize("/goods/brand/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(brandService.query(query));
    }
}
