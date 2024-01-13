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
import smart.entity.CategoryEntity;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.CategoryService;
import smart.dto.IdDto;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@RestController(value = "adminApi/goods/category")
@RequestMapping(path = "/adminApi/goods/category", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Category {

    @Resource
    AdminLogService adminLogService;
    @Resource
    CategoryService categoryService;

    @Authorize("/goods/category/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class)CategoryEntity categoryEntity,
                             HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(categoryService.save(categoryEntity));
        adminLogService.addLogIfSuccess(result, request, "新建商品分类", categoryEntity);
        return result;
    }

    @Authorize("/goods/category/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto,
                                HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(categoryService.delete(idDto));
        adminLogService.addLogIfSuccess(result, request, "删除商品分类", idDto);
        return result;
    }
    @Authorize("/goods/category/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class)CategoryEntity categoryEntity,
                              HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(categoryService.save(categoryEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑商品分类", categoryEntity);
        return result;
    }
    @Authorize("/goods/category/query")
    @PostMapping("list")
    public ApiJsonResult list() {
        return ApiJsonResult.success()
                .putDataItem("rows", categoryService.query());
    }
}
