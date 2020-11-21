package alex.controller.admin.sys;

import alex.Application;
import alex.entity.AdminRoleEntity;
import alex.entity.UserEntity;
import alex.lib.*;
import alex.repository.AdminRoleRepository;
import alex.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller(value = "admin/sys/adminUser")
@RequestMapping(path = "/admin/sys/adminUser")
public class AdminUser {

    @Resource
    AdminRoleRepository adminRoleRepository;

    @Resource
    UserRepository userRepository;

    @PostMapping(value = "delete", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        long num = Application.JDBC_TEMPLATE.update("delete from adminUsers where userId = " + id);
        if (num == 0 || userEntity == null) {
            jsonResult.setMsg("该账号不存在,请刷新页面重试");
            return jsonResult.toString();
        }
        jsonResult.setMsg("已删除管理员账号: " + userEntity.getName());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView;
        long id = Helper.longValue(request.getParameter("id"));
        String sql = "select u.id,\n" +
                "       u.name as name,\n" +
                "       au.roleId\n" +
                "from adminUsers au\n" +
                "         left join users u on au.userId = u.id\n" +
                "where au.userId = ?";
        Map<String, Object> user = null;
        if (id > 0) {
            try {
                user = Application.JDBC_TEMPLATE.queryForMap(sql, id);
            } catch (DataAccessException e) {
                return AdminHelper.msgPage("用户不存在", "list", request);
            }
        } else {
            user = new HashMap<>();
            user.put("name", "");
            user.put("roleId", 0L);
        }

        var roles = adminRoleRepository.findAll();
        AdminRoleEntity adminRoleEntity = new AdminRoleEntity();
        adminRoleEntity.setId(0L);
        adminRoleEntity.setName("超级管理员");
        roles.add(0, adminRoleEntity);

        modelAndView = Helper.newModelAndView("admin/sys/adminUser/edit", request);
        modelAndView.addObject("title", "管理员");
        modelAndView.addObject("id", id);
        modelAndView.addObject("roles", roles);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        long roleId = Helper.longValue(request.getParameter("role"));
        if (roleId != 0 && adminRoleRepository.findById(roleId).orElse(null) == null) {
            jsonResult.setMsg("指定的角色不存在");
            return jsonResult.toString();
        }
        if (id > 0) {
            Application.JDBC_TEMPLATE.update("update adminUsers set roleId = ? where userId = ?", roleId, id);
            jsonResult.setMsg("更新成功");
            jsonResult.setUrl("list");
            return AdminHelper.msgPage(jsonResult, request);
        }

        // create administrator
        var name = Helper.stringValue(request.getParameter("name"));
        var msg = Validate.name(name, "账号");
        if ( msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        UserEntity userEntity = userRepository.findByName(name);
        if (userEntity == null) {
            jsonResult.setMsg("新建失败,该账号不存在");
            return jsonResult.toString();
        }
        try {
            Application.JDBC_TEMPLATE.update("insert into adminUsers set userId=?,roleId=?",
                    userEntity.getId(), roleId);
        } catch (DuplicateKeyException e) {
            jsonResult.setMsg("新建失败,该账号已存在");
            return jsonResult.toString();
        }
        jsonResult.setMsg("已完成新建管理员: " + name);
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);

    }

    @GetMapping(value = "list")
    public ModelAndView getIndex(HttpServletRequest request) {
        long page = Helper.longValue(request.getParameter("page"), 1);
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/adminUser/list", request);
        modelAndView.addObject("title", "管理员列表");
        String sql = "select u.id,\n" +
                "       u.name    as name,\n" +
                "       au.roleId as roleId,\n" +
                "       ar.name   as roleName\n" +
                "from adminUsers au\n" +
                "         left join users u on au.userId = u.id\n" +
                "         left join adminRoles ar on au.roleId = ar.id";
        Pagination pagination = new Pagination(sql, page);
        for (var row : pagination.getRows()) {
            long roleId = Helper.longValue(row.get("roleId"));
            if (roleId == 0) {
                row.put("roleName", "超级管理员");
            }
        }
        modelAndView.addObject("pagination", pagination);
        return modelAndView;
    }
}
