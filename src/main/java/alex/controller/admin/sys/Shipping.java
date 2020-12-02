package alex.controller.admin.sys;

import alex.Application;
import alex.cache.ExpressCache;
import alex.cache.RegionCache;
import alex.entity.ExpressCompanyEntity;
import alex.lib.AdminHelper;
import alex.lib.Helper;
import alex.lib.JsonResult;
import alex.lib.express.FreeRule;
import alex.lib.express.PriceRule;
import alex.lib.express.ProvincePrice;
import alex.repository.ExpressCompanyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ini4j.Reg;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

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

    @GetMapping(value = "freeRule")
    public ModelAndView getFreeRule(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/shipping/freeRule", request);
        StringBuilder provincesHtml = new StringBuilder();
        for (var p : RegionCache.getProvinces()) {
            provincesHtml.append("<option value=\"")
                    .append(p.getCode())
                    .append("\">")
                    .append(p.getName())
                    .append("</option>");
        }
        modelAndView.addObject("provincesHtml", provincesHtml);
        modelAndView.addObject("title", "包邮规则");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            modelAndView.addObject("freeRule",
                    objectMapper.writeValueAsString(ExpressCache.getFreeRule()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();

        }
        return modelAndView;
    }

    @PostMapping(value = "freeRule", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postFreeRule(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        FreeRule freeRule = new FreeRule();
        freeRule.setEnable(Helper.longValue(request.getParameter("enable")) > 0);
        freeRule.setPrice(Helper.bigDecimalValue(request.getParameter("price")).multiply(new BigDecimal(100)).longValue());
        String exclude = request.getParameter("exclude");
        if (exclude != null && exclude.length() > 1) {
           for(var code : exclude.split(",")){
               var l = Helper.longValue(code);
               if (RegionCache.getRegion(l) != null) {
                   freeRule.getExclude().add(l);
               }
           }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(freeRule);
            Application.JDBC_TEMPLATE.update("update system set value = ? where entity='shipping' and attribute = 'freeRule'",
                    json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            jsonResult.setMsg("未知错误，请联系管理员");
            return jsonResult.toString();
        }
        ExpressCache.init();
        jsonResult.setMsg("包邮规则更新完成");
        return AdminHelper.msgPage(jsonResult, request);

    }

    @GetMapping(value = "priceRule")
    public ModelAndView getPriceRule(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/shipping/priceRule", request);
        StringBuilder provincesHtml = new StringBuilder();
        for (var p : RegionCache.getProvinces()) {
            provincesHtml.append("<option value=\"")
                    .append(p.getCode())
                    .append("\">")
                    .append(p.getName())
                    .append("</option>");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            modelAndView.addObject("priceRule",
                    objectMapper.writeValueAsString(ExpressCache.getPriceRule()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();

        }
        modelAndView.addObject("title", "运费规则");
        modelAndView.addObject("provincesHtml", provincesHtml);
        return modelAndView;
    }

    @PostMapping(value = "priceRule", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postPriceRule(HttpServletRequest request) {
        PriceRule priceRule = new PriceRule();
        JsonResult jsonResult = new JsonResult();
        priceRule.setFirstPrice(Helper.bigDecimalValue(request.getParameter("firstPrice")).multiply(new BigDecimal(100)).longValue());
        priceRule.setFirstWeight(Helper.longValue(request.getParameter("firstWeight")));
        priceRule.setAdditionalPrice(Helper.bigDecimalValue(request.getParameter("additionalPrice")).multiply(new BigDecimal(100)).longValue());
        priceRule.setAdditionalWeight(Helper.longValue(request.getParameter("additionalWeight")));
        if (priceRule.getFirstWeight() < 1 || priceRule.getFirstPrice() < 1) {
            jsonResult.setMsg("首重重量或价格不得为空");
            return jsonResult.toString();
        }
        if (priceRule.getAdditionalWeight() < 1 || priceRule.getAdditionalPrice() < 1) {
            jsonResult.setMsg("续重重量或价格不得为空");
            return jsonResult.toString();
        }
        priceRule.setOtherDefault(Helper.longValue(request.getParameter("otherDefault")) > 0);
        // 自定义地区规则
        String[] provinces = request.getParameterValues("provinces");
        String[] price1 = request.getParameterValues("price1");
        String[] price2 = request.getParameterValues("price2");
        if (provinces != null && price1 != null && price2 != null) {
            if (provinces.length != price1.length || provinces.length != price2.length) {
                jsonResult.setMsg("自定义地区数据格式错误");
                return jsonResult.toString();
            }
            for (int i = 0; i < provinces.length; i++) {
                ProvincePrice provincePrice = new ProvincePrice();
                for (var code : provinces[i].split(",")) {
                    long l = Helper.longValue(code);
                    if (l % 10000 > 0 || RegionCache.getRegion(l) == null) {
                        continue;
                    }
                    provincePrice.getProvinces().add(l);
                }
                provincePrice.setFirstPrice(Helper.bigDecimalValue(price1[i]).multiply(new BigDecimal(100)).longValue());
                provincePrice.setAdditionalPrice(Helper.bigDecimalValue(price2[i]).multiply(new BigDecimal(100)).longValue());
                if (provincePrice.getFirstPrice() <= 0 || provincePrice.getAdditionalPrice() <= 0) {
                    jsonResult.setMsg("价格不得为空或零");
                    return jsonResult.toString();
                }
                if (provincePrice.getProvinces().size() > 0) {
                    priceRule.getProvincePrices().add(provincePrice);
                }
            }
        }


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(priceRule);
            Application.JDBC_TEMPLATE.update("update system set value = ? where entity='shipping' and attribute = 'priceRule'",
                    json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            jsonResult.setMsg("未知错误，请联系管理员");
            return jsonResult.toString();
        }
        ExpressCache.init();
        jsonResult.setMsg("运费规则更新完成");
        return AdminHelper.msgPage(jsonResult, request);
    }
}
