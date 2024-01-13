package smart.controller.adminApi.article;

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
import smart.entity.ArticleCategoryEntity;
import smart.lib.ApiJsonResult;
import smart.repository.ArticleCategoryRepository;
import smart.service.AdminLogService;
import smart.service.ArticleCategoryService;
import smart.dto.IdDto;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@RestController(value = "adminApi/article/category")
@RequestMapping(path = "/adminApi/article/category", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Category {

    @Resource
    AdminLogService adminLogService;

    @Resource
    ArticleCategoryRepository articleCategoryRepository;

    @Resource
    ArticleCategoryService articleCategoryService;

    @Authorize("/article/category/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class) ArticleCategoryEntity articleCategoryEntity, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(articleCategoryService.save(articleCategoryEntity));
        adminLogService.addLogIfSuccess(result, request, "新建文章分类", articleCategoryEntity);
        return result;
    }

    @Authorize("/article/category/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(articleCategoryService.delete(idDto.getId()));
        adminLogService.addLogIfSuccess(result, request, "删除文章分类", idDto);
        return result;
    }

    @Authorize("/article/category/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class) ArticleCategoryEntity articleCategoryEntity, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(articleCategoryService.save(articleCategoryEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑文章分类", articleCategoryEntity);
        return result;
    }

    @Authorize("/article/category/query")
    @PostMapping("list")
    public ApiJsonResult list() {
        return ApiJsonResult.success().putDataItem("rows", articleCategoryRepository.findAllByOrderByOrderNum());
    }
}
