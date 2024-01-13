package smart.controller.adminApi.article;

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
import smart.entity.ArticleEntity;
import smart.lib.ApiJsonResult;
import smart.repository.ArticleCategoryRepository;
import smart.service.AdminLogService;
import smart.service.ArticleService;
import smart.util.Helper;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.util.Map;
import java.util.Objects;

@RestController(value = "adminApi/article/article")
@RequestMapping(path = "/adminApi/article/article", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Article {
    @Resource
    AdminLogService adminLogService;
    @Resource
    ArticleCategoryRepository articleCategoryRepository;
    @Resource
    ArticleService articleService;

    @Authorize("/article/article/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class) ArticleEntity articleEntity, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(articleService.save(articleEntity));
        adminLogService.addLogIfSuccess(result, request, "新建文章", articleEntity);
        return result;
    }

    @Authorize("/article/article/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(articleService.delete(idDto.getId()));
        adminLogService.addLogIfSuccess(result, request, "删除文章", idDto);
        return result;
    }

    @Authorize("/article/article/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class) ArticleEntity articleEntity, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(articleService.save(articleEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑文章", articleEntity);
        return result;
    }

    @Authorize("/article/article/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(articleService.query(query)).putDataItem("now", Helper.now()).putDataItem("categoryList", articleCategoryRepository.findAllByOrderByOrderNum());
    }

    @Authorize({"/article/article/add", "/article/article/edit"})
    @PostMapping("upload")
    public ApiJsonResult upload(HttpServletRequest request, @RequestParam(name = "file") MultipartFile file) {
        var msg = General.checkFile(file, 501_000, true);
        if (msg != null) return ApiJsonResult.error(msg);
        var result = General.upload(request, file);
        if (result.isSuccess()) {
            adminLogService.addLog(request, "上传图片(文章)", Map.of("src", Objects.requireNonNull(file.getOriginalFilename()), "size", Helper.getSizes(file.getSize()), "target", result.getData().get("url")));
        }
        return result;
    }
}
