package alex.controller.admin.user;

import alex.lib.Helper;
import alex.lib.Pagination;
import alex.lib.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Controller(value = "admin/user/user")
@RequestMapping(path = "/admin/user/user")
public class User {
    @GetMapping(value = "list")
    public ModelAndView getIndex(HttpServletRequest request,
                                 @RequestParam(required = false) String name) {
        long page = Helper.longValue(request.getParameter("page"), 1);
        String sqlWhere = "";
        Set<Object> params = new LinkedHashSet<>();
        Map<String, String> query = new HashMap<>();


        if (name != null) {
            name = name.trim();
            if (name.length() > 0) {
                if (Validate.letterOrNumber(name, "") == null) {
                    sqlWhere += " and name like ?";
                    params.add('%' + name + '%');
                } else {
                    sqlWhere += " and false";
                }

                query.put("name", name);
            }
        }
        if (sqlWhere.startsWith(" and ")) {
            sqlWhere = " where " + sqlWhere.substring(4);
        }
        Pagination pagination = new Pagination("select * from users" + sqlWhere + " order by id desc", params.toArray(), page, query);
        pagination.getRows().forEach(item -> {
            BigInteger surplus = (BigInteger) item.get("surplus");
            item.put("surplus", Helper.priceFormat(surplus.longValue()));
        });
        ModelAndView modelAndView = Helper.newModelAndView("admin/user/user/list", request);
        modelAndView.addObject("title", "会员列表");
        modelAndView.addObject("name", name);
        modelAndView.addObject("pagination", pagination);
        return modelAndView;
    }

}
