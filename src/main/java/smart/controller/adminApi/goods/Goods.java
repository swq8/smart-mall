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
import smart.entity.GoodsEntity;
import smart.lib.ApiJsonResult;
import smart.repository.GoodsRepository;
import smart.repository.GoodsSpecRepository;
import smart.repository.SpecRepository;
import smart.service.AdminLogService;
import smart.service.BrandService;
import smart.service.CategoryService;
import smart.service.GoodsService;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.util.Map;
import java.util.Objects;

@RestController(value = "adminApi/goods/goods")
@RequestMapping(path = "/adminApi/goods/goods", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Goods {

    @Resource
    AdminLogService adminLogService;

    @Resource
    BrandService brandService;
    @Resource
    CategoryService categoryService;

    @Resource
    GoodsRepository goodsRepository;
    @Resource
    GoodsService goodsService;

    @Resource
    GoodsSpecRepository goodsSpecRepository;

    @Resource
    SpecRepository specRepository;

    @Authorize("/goods/goods/add")
    @PostMapping("add")
    public ApiJsonResult add(@RequestBody @Validated(Add.class) GoodsEntity goodsEntity,
                             HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(goodsService.save(goodsEntity));
        adminLogService.addLogIfSuccess(result, request, "新建商品", goodsEntity);
        return result;
    }

    @Authorize("/goods/goods/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(@RequestBody @Validated IdDto idDto,
                                HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(goodsService.delete(idDto));
        adminLogService.addLogIfSuccess(result, request, "删除商品", idDto);
        return result;
    }

    @Authorize("/goods/goods/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated(Edit.class) GoodsEntity goodsEntity,
                              HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(goodsService.save(goodsEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑商品", goodsEntity);
        return result;
    }

    @Authorize("/goods/goods/query")
    @PostMapping("get")
    public ApiJsonResult get(@RequestBody @Validated IdDto idDto) {
        var goodsEntity = DbUtils.findById(idDto.getId(), GoodsEntity.class);
        if (goodsEntity == null) return ApiJsonResult.error("商品不存在");
        goodsEntity.setSpecProps(goodsSpecRepository.findByGoodsId(idDto.getId()));
        return ApiJsonResult.success()
                .putDataItem("goods", goodsEntity);
    }


    @Authorize("/goods/goods/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody GeneralQueryDto query) {
        return ApiJsonResult.success(goodsService.query(query))
                .putDataItem("brandList", brandService.findAll())
                .putDataItem("categoryList", categoryService.query())
                .putDataItem("specList", specRepository.findAllByOrderByName());
    }

    @Authorize({"/goods/goods/add", "/goods/goods/edit"})
    @PostMapping("upload")
    public ApiJsonResult upload(HttpServletRequest request,
                                @RequestParam(name = "file") MultipartFile file) {
        var msg = General.checkFile(file, 501_000, true);
        if (msg != null) return ApiJsonResult.error(msg);
        var result = General.upload(request, file);
        if (result.isSuccess()) {
            adminLogService.addLog(request, "上传图片(商品管理)",
                    Map.of("src", Objects.requireNonNull(file.getOriginalFilename()),
                            "size", Helper.getSizes(file.getSize()),
                            "target", result.getData().get("url")
                    ));
        }
        return result;
    }
}
