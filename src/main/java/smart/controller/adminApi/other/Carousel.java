package smart.controller.adminApi.other;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smart.auth.Authorize;
import smart.cache.SystemCache;
import smart.controller.adminApi.General;
import smart.dto.express.FreeRuleDto;
import smart.dto.other.CarouselDto;
import smart.dto.other.CarouselItem;
import smart.lib.ApiJsonResult;
import smart.service.AdminLogService;
import smart.service.SystemService;
import smart.util.Helper;
import smart.util.Json;
import smart.util.LogUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController(value = "adminApi/other/carousel")
@RequestMapping(path = "/adminApi/other/carousel", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Carousel {
    @Resource
    AdminLogService adminLogService;
    @Resource
    SystemService systemService;


    @Authorize("/other/carousel/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(@RequestBody @Validated CarouselDto carousel, HttpServletRequest request) {
        var result = ApiJsonResult.successOrError(systemService.saveCarousel(carousel));
        adminLogService.addLogIfSuccess(result, request, "编辑首页轮播", carousel);
        return ApiJsonResult.success();
    }

    @Authorize("/other/carousel/query")
    @PostMapping("get")
    public ApiJsonResult get() {
        return ApiJsonResult.success()
                .putDataItem("record", SystemCache.getCarousel());
    }

    @Authorize("/other/carousel/edit")
    @PostMapping("upload")
    public ApiJsonResult upload(HttpServletRequest request,
                                @RequestParam(name = "file") MultipartFile file) {
        var msg = General.checkFile(file, 501_000, true);
        if (msg != null) return ApiJsonResult.error(msg);
        var result = General.upload(request, file);
        if (result.isSuccess()) {
            adminLogService.addLog(request, "上传图片(首页轮播)",
                    Map.of("src", Objects.requireNonNull(file.getOriginalFilename()),
                            "size", Helper.getSizes(file.getSize()),
                            "target", result.getData().get("url")
                    ));
        }
        return result;
    }
}
