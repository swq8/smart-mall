package alex.controller.admin.sys;

import alex.cache.ExpressCache;
import alex.entity.ExpressCompanyEntity;
import alex.lib.AdminHelper;
import alex.lib.Helper;
import alex.lib.JsonResult;
import alex.repository.ExpressCompanyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller(value = "admin/sys/shipping")
@RequestMapping(path = "/admin/sys/shipping")
public class Shipping {

    @Resource
    ExpressCompanyRepository expressCompanyRepository;

    @GetMapping(value = "companies")
    public ModelAndView getCompanies(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/shipping/companies", request);
        modelAndView.addObject("companies", ExpressCache.getCompanies());
        modelAndView.addObject("title", "快递公司");
        return modelAndView;
    }

    @GetMapping(value = "company")
    public ModelAndView getCompany(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/shipping/company", request);
        long id = Helper.longValue(request.getParameter("id"));
        ExpressCompanyEntity entity;
        if (id > 0) {
            entity = expressCompanyRepository.findById(id).orElse(null);
            if (entity == null) {
                return AdminHelper.msgPage("要修改的快递公司不存在", "/admin/sys/shipping/companies", request);
            }
            modelAndView.addObject("title", "修改快递公司信息");
        } else {
            entity = new ExpressCompanyEntity();
            entity.setRecommend(100);
            modelAndView.addObject("title", "新建快递公司信息");
        }
        modelAndView.addObject("entity", entity);
        return modelAndView;
    }

    @PostMapping(value = "company", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postCompany(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        String name = request.getParameter("name");
        String url = request.getParameter("url");
        if (name == null || url == null) {
            jsonResult.setMsg("名称或网址不得为空");
            return jsonResult.toString();
        }
        long recommend = Helper.longValue(request.getParameter("recommend"));
        ExpressCompanyEntity entity = new ExpressCompanyEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setRecommend(recommend);
        entity.setUrl(url);
        expressCompanyRepository.save(entity);
        ExpressCache.init();
        jsonResult.setUrl("/admin/sys/shipping/companies");
        jsonResult.setMsg("已保存");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @PostMapping(value = "delete", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        expressCompanyRepository.deleteById(id);
        ExpressCache.init();
        jsonResult.setUrl("/admin/sys/shipping/companies");
        jsonResult.setMsg("已删除");
        return AdminHelper.msgPage(jsonResult, request);
    }
}
