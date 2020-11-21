package alex.controller.admin.sys;

import alex.Application;
import alex.config.AdminAuthority;
import alex.entity.AdminRoleEntity;
import alex.lib.AdminHelper;
import alex.lib.Helper;
import alex.lib.JsonResult;
import alex.lib.Pagination;
import alex.repository.AdminRoleRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Controller(value = "admin/sys/adminRole")
@RequestMapping(path = "/admin/sys/adminRole")
public class AdminRole {


    @Resource
    AdminRoleRepository adminRoleRepository;

    @PostMapping(value = "delete", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        AdminRoleEntity adminRoleEntity = adminRoleRepository.findById(id).orElse(null);
        if (adminRoleEntity != null) {
            Long num = Application.JDBC_TEMPLATE.queryForObject("select count(*) from adminUsers where roleId=" + id, Long.class);
            if (num != null && num > 0) {
                jsonResult.setMsg("该角色正在被使用中,删除失败");
                return jsonResult.toString();
            }
        }
        adminRoleRepository.deleteById(id);
        jsonResult.setMsg("删除成功");
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/adminRole/edit", request);
        long id = Helper.longValue(request.getParameter("id"));
        AdminRoleEntity adminRoleEntity;
        String authorityStr = "";
        String name = "";
        if (id == 0) {
            modelAndView.addObject("title", "创建角色");
        } else {
            adminRoleEntity = adminRoleRepository.findById(id).orElse(null);
            if (adminRoleEntity == null) {
                return AdminHelper.msgPage("角色不存在", "list", request);
            }
            authorityStr = adminRoleEntity.getAuthority().replace("/", "\\\\/");
            name = adminRoleEntity.getName();
            modelAndView.addObject("title", "修改角色");
        }
        AdminAuthority adminAuthority = new AdminAuthority(0);
        Map<String, Set<String>> rules = new LinkedHashMap<>();
        adminAuthority.rules.keySet().forEach(key -> {
            String[] arr = key.split("-");
            var names = rules.computeIfAbsent(arr[0], k -> new LinkedHashSet<>());
            names.add(arr[1]);
        });

        modelAndView.addObject("id", id);
        modelAndView.addObject("rules", rules);
        modelAndView.addObject("authorityStr", authorityStr);
        modelAndView.addObject("name", name);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        String name = request.getParameter("name");
        long id = Helper.longValue(request.getParameter("id"));
        String[] authority = request.getParameterValues("authority");
        JsonResult jsonResult = new JsonResult();
        if (name != null) {
            name = Helper.stringRemove(name, " ");
        }
        if (name == null || name.length() == 0) {
            jsonResult.setMsg("角色名称不得为空");
            return jsonResult.toString();
        }
        if (authority == null || authority.length == 0) {
            jsonResult.setMsg("请至少勾选一项权限");
            return jsonResult.toString();
        }
        StringBuilder authorityStr = new StringBuilder();
        AdminAuthority adminAuthority = new AdminAuthority(0);
        AdminRoleEntity adminRoleEntity = new AdminRoleEntity();
        for (int i = 0; ; ) {
            if (adminAuthority.rules.containsKey(authority[i])) {
                authorityStr.append(authority[i]);
            }
            if (++i == authority.length) {
                break;
            } else {
                authorityStr.append(",");
            }
        }
        adminRoleEntity.setName(name);
        adminRoleEntity.setAuthority(authorityStr.toString());
        if (id > 0) {
            adminRoleEntity.setId(id);
            jsonResult.setMsg("角色修改成功");
        } else {
            jsonResult.setMsg("角色添加成功");
        }
        try {
            adminRoleRepository.save(adminRoleEntity);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                var obj = (ConstraintViolationException) e.getCause();
                if (obj.getSQLState().equals("23000")) {
                    jsonResult.setMsg("该角色名称已存在,请更换角色名称");
                    return jsonResult.toString();
                }
            } else {
                e.printStackTrace();
            }
        }
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "list")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/adminRole/list", request);
        long page = Helper.longValue(request.getParameter("page"), 1);
        Pagination pagination = new Pagination("select * from adminRoles", page);
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", "角色列表");
        return modelAndView;
    }
}
