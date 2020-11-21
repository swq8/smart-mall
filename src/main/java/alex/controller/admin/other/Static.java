package alex.controller.admin.other;

import alex.Application;
import alex.cache.SystemCache;
import alex.lib.AdminHelper;
import alex.lib.Helper;
import alex.lib.JsonResult;
import alex.repository.SystemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller(value = "admin/other/static")
@RequestMapping(path = "/admin/other/static")
public class Static {

    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/other/static/index", request);
        modelAndView.addObject("jsVersion", SystemCache.getJsVersion());
        modelAndView.addObject("title", "静态文件");
        return modelAndView;
    }

    @PostMapping(value = "", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postIndex(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        String jsVersion = request.getParameter("jsVersion");
        if (jsVersion == null) {
            jsVersion = "";
        }
        Application.JDBC_TEMPLATE.update("update system set value=? where entity='sys' and attribute='jsVersion'", jsVersion);
        SystemCache.setJsVersion(jsVersion);
        jsonResult.setMsg("保存成功");
        return AdminHelper.msgPage(jsonResult, request);
    }
}
