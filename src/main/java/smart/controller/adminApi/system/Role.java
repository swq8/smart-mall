package smart.controller.adminApi.system;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.entity.AdminRoleEntity;
import smart.lib.ApiJsonResult;
import smart.repository.AdminMenuRepository;
import smart.repository.AdminRoleRepository;
import smart.service.AdminLogService;
import smart.service.AdminRoleService;
import smart.dto.IdDto;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.util.*;

@RestController(value = "adminApi/system/role")
@RequestMapping(path = "/adminApi/system/role", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Role {

    @Resource
    AdminLogService adminLogService;

    @Resource
    AdminMenuRepository adminMenuRepository;
    @Resource
    AdminRoleRepository adminRoleRepository;

    @Resource
    AdminRoleService adminRoleService;

    @Authorize("/system/role/add")
    @PostMapping("add")
    public ApiJsonResult add(HttpServletRequest request,
                             @RequestBody @Validated(Add.class) AdminRoleEntity adminRoleEntity) {
        var result = ApiJsonResult.successOrError(adminRoleService.save(adminRoleEntity));
        adminLogService.addLogIfSuccess(result, request, "新建角色", adminRoleEntity);
        return result;
    }

    @Authorize("/system/role/delete")
    @PostMapping("delete")
    public ApiJsonResult delete(HttpServletRequest request, @Validated @RequestBody IdDto idDto, BeanPropertyBindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            return ApiJsonResult.badRequest(bindingResult);
        }
        var msg = adminRoleService.delete(idDto.getId());
        if (msg != null) {
            return ApiJsonResult.error(msg);
        }
        adminLogService.addLog(request, "删除角色", idDto);
        return ApiJsonResult.success();
    }

    @Authorize("/system/role/query")
    @PostMapping("list")
    public ApiJsonResult list() {
        var rows = adminRoleRepository.findAllByOrderByOrderNum();
        return ApiJsonResult.success()
                .putDataItem("rows", rows)
                .putDataItem("treeData", treeData());
    }

    @Authorize("/system/role/edit")
    @PostMapping("edit")
    public ApiJsonResult edit(HttpServletRequest request,
                              @RequestBody @Validated(Edit.class) AdminRoleEntity adminRoleEntity) {
        var result = ApiJsonResult.successOrError(adminRoleService.save(adminRoleEntity));
        adminLogService.addLogIfSuccess(result, request, "修改角色", adminRoleEntity);
        return result;
    }

    public List<Map<String, Object>> treeData() {
        var rows = adminMenuRepository.findAllByOrderByOrderNumAscIdAsc();
        List<Map<String, Object>> treeData = new ArrayList<>();
        rows.stream().filter(item -> item.getType() == 1).forEach(item -> {
            Map<String, Object> mapDirectory = new HashMap<>();
            mapDirectory.put("id", item.getId());
            mapDirectory.put("label", item.getName());
            List<Map<String, Object>> childrenMenu = new ArrayList<>();
            rows.stream().filter(itemMenu -> Objects.equals(itemMenu.getParentId(), item.getId())).forEach(itemMenu -> {
                Map<String, Object> mapMenu = new HashMap<>();
                mapMenu.put("id", itemMenu.getId());
                mapMenu.put("label", itemMenu.getName());
                List<Map<String, Object>> childrenAuthorize = new ArrayList<>();
                rows.stream().filter(itemAuthorize -> Objects.equals(itemAuthorize.getParentId(), itemMenu.getId()))
                        .forEach(itemAuthorize -> {
                            Map<String, Object> mapAuthorize = new HashMap<>();
                            mapAuthorize.put("id", itemAuthorize.getId());
                            mapAuthorize.put("label", itemAuthorize.getName());
                            childrenAuthorize.add(mapAuthorize);
                        });
                mapMenu.put("children", childrenAuthorize);
                childrenMenu.add(mapMenu);
            });
            mapDirectory.put("children", childrenMenu);
            treeData.add(mapDirectory);
        });
        return treeData;
    }
}
