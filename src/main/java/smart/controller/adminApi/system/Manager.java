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
import smart.dto.PaginationDto;
import smart.entity.AdminUserEntity;
import smart.lib.ApiJsonResult;
import smart.repository.AdminRoleRepository;
import smart.service.AdminLogService;
import smart.service.AdminUserService;
import smart.util.validategroups.Add;
import smart.util.validategroups.Delete;
import smart.util.validategroups.Edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController(value = "adminApi/system/manager")
@RequestMapping(path = "/adminApi/system/manager", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Manager {

    @Resource
    AdminLogService adminLogService;
    @Resource
    AdminUserService adminUserService;

    @Resource
    AdminRoleRepository adminRoleRepository;


    @Authorize("/system/manager/add")
    @PostMapping("add")
    public ApiJsonResult add(HttpServletRequest request,
                             @RequestBody @Validated(Add.class) AdminUserEntity adminUserEntity) {
        var result = ApiJsonResult.successOrError(adminUserService.save(adminUserEntity));
        adminLogService.addLogIfSuccess(result, request, "新建管理账号", adminUserEntity);
        return result;
    }

    @Authorize("/system/manager/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(HttpServletRequest request,
                                @RequestBody @Validated(Delete.class) AdminUserEntity entity) {
        var result = ApiJsonResult.successOrError(adminUserService.deleteByUserId(entity.getUserId()));
        adminLogService.addLogIfSuccess(result, request, "删除管理账号", Map.of("uid", entity.getUserId()));
        return result;
    }


    @Authorize("/system/manager/query")
    @PostMapping("list")
    public ApiJsonResult list(@RequestBody @Validated PaginationDto query) {
        List<Map<String, Object>> roles = new ArrayList<>();
        for (var row : adminRoleRepository.findAllByOrderByOrderNum()) {
            roles.add(Map.of("id", row.getId(), "name", row.getName()));
        }
        return ApiJsonResult.success(adminUserService.query(query))
                .putDataItem("roles", roles);
    }

    @Authorize("/system/manager/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(HttpServletRequest request,
                              @RequestBody @Validated(Edit.class) AdminUserEntity adminUserEntity) {
        var result = ApiJsonResult.successOrError(adminUserService.save(adminUserEntity));
        adminLogService.addLogIfSuccess(result, request, "编辑管理账号", adminUserEntity);
        return result;
    }
}
