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
import smart.entity.AdminMenuEntity;
import smart.lib.ApiJsonResult;
import smart.repository.AdminMenuRepository;
import smart.service.AdminLogService;
import smart.service.AdminMenuService;
import smart.dto.IdDto;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@RestController(value = "adminApi/system/menu")
@RequestMapping(path = "/adminApi/system/menu", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Menu {

    @Resource
    AdminLogService adminLogService;

    @Resource
    AdminMenuService adminMenuService;
    @Resource
    AdminMenuRepository adminMenuRepository;

    @Authorize("/system/menu/add")
    @PostMapping("add")
    public ApiJsonResult add(HttpServletRequest request,
                             @RequestBody @Validated(Add.class) AdminMenuEntity adminMenuEntity) {
        var result = ApiJsonResult.successOrError(adminMenuService.save(adminMenuEntity));
        adminLogService.addLogIfSuccess(result, request, "新建菜单", adminMenuEntity);
        return result;
    }

    @Authorize("/system/menu/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(HttpServletRequest request, @RequestBody @Validated IdDto idDto) {
        var result = ApiJsonResult.successOrError(adminMenuService.delete(idDto.getId()));
        adminLogService.addLogIfSuccess(result, request, "删除菜单", idDto);
        return result;
    }


    @Authorize("/system/menu/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(HttpServletRequest request,
                              @RequestBody @Validated(Edit.class) AdminMenuEntity adminMenuEntity) {
        var result = ApiJsonResult.successOrError(adminMenuService.save(adminMenuEntity));
        adminLogService.addLogIfSuccess(result, request, "修改菜单", adminMenuEntity);
        return result;
    }

    @Authorize("/system/menu/query")
    @PostMapping("list")
    public ApiJsonResult list() {
        return ApiJsonResult.success()
                .putDataItem("rows", adminMenuRepository.findAllByOrderByOrderNumAscIdAsc());
    }
}
