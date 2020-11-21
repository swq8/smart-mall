package alex.controller;

import alex.Application;
import alex.cache.CategoryCache;
import alex.cache.RegionCache;
import alex.lib.Captcha;
import alex.lib.Helper;
import alex.lib.Pagination;
import alex.lib.Region;
import alex.storage.LocalStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class Site {
    /**
     * captcha
     */
    @RequestMapping(value = "captcha")
    @ResponseBody
    public String captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/png");

        OutputStream os = response.getOutputStream();

        var captchaResult = Captcha.getImageCode();
        request.getSession().setAttribute(Captcha.SESSION_NAME, captchaResult.getPhrase().toLowerCase());
        try {
            ImageIO.write(captchaResult.getImage(), "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
        return null;
    }


    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("index", request);
        return modelAndView;
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ModelAndView getList(HttpServletRequest request) {
        StringBuilder sql = new StringBuilder("select id,imgs,name,price from goods where status & 0b10");
        ModelAndView modelAndView = Helper.newModelAndView("list", request);
        long cid = Helper.longValue(request.getParameter("cid"));
        if (cid > 0 && CategoryCache.getEntityById(cid) != null) {
            sql.append(" and cateId in (").append(cid);
            for (var item : CategoryCache.getChildren(cid)) {
                sql.append(",").append(item.getId());
            }
            sql.append(")");
        } else {
            cid = 0;
        }
        long page = Helper.longValue(request.getParameter("page"));
        String q = request.getParameter("q");
        if (q == null) {
            q = "";
        } else {
            q = Helper.stringRemove(q, "'", "\"", "?", "%", "_", "[", "]").trim();
            StringBuilder qSql = new StringBuilder("'%");
            for (var key : q.split(" ")) {
                if (key.length() > 0) {
                    qSql.append(key).append("%");
                }
            }
            qSql.append("'");
            sql.append(" and name like ").append(qSql);
        }
        sql.append(" order by ");
        String sort = request.getParameter("sort");
        if (sort == null) {
            sort = "";
        }

        switch (sort) {
            case "n":
                sql.append("released desc,recommend desc,id desc");
                break;
            case "p1":
                sql.append("price,recommend desc,released desc,id desc");
                break;
            case "p2":
                sql.append("price desc,recommend desc,released desc,id desc");
                break;
            default:
                sort = "";
                sql.append("recommend desc,released desc,id desc");
        }
        Pagination pagination = new Pagination(sql.toString(), page,
                Map.of("cid", Long.toString(cid), "q", q, "sort", sort));

        String imgs;
        for (var row : pagination.getRows()) {
            imgs = (String) row.get("imgs");
            row.put("img", imgs.split(",")[0]);
        }
        modelAndView.addObject("cid", cid);
        modelAndView.addObject("q", q);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("categoryPath", CategoryCache.getCategoryPath(cid));
        modelAndView.addObject("pagination", pagination);
        return modelAndView;
    }

    @GetMapping(path = "region", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getRegion() {
        return RegionCache.getRegionJson();
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public String getTest(Model model, HttpServletRequest request) {

        Application.JDBC_TEMPLATE.update("update abc set id = 1");
        return "";

    }
    @RequestMapping(value = "test1", method = RequestMethod.GET)
    @ResponseBody
    public String getTest1(Model model, HttpServletRequest request) {
        TemplateEngine templateEngine = Application.CONTEXT.getBean(TemplateEngine.class);
        Context context = new Context();
        context.setVariable("error", "msg");
        String html = templateEngine.process("default/error", context);
        return html;

    }

    @RequestMapping(value = "msg", method = RequestMethod.GET)
    public ModelAndView getMsg(HttpServletRequest request, String title, String msg, String backUrl) {
        ModelAndView modelAndView = Helper.newModelAndView("msg", request);
        modelAndView.addObject("title", title);
        modelAndView.addObject("msg", msg);
        modelAndView.addObject("backUrl", backUrl);
        return modelAndView;
    }

    @GetMapping(value = "/upload", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String getUpload(HttpServletRequest request) {
        System.out.println(Arrays.toString(request.getParameterValues("abc")));
        return "";
    }

    @PostMapping(value = "/upload", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }

        String fileName = file.getOriginalFilename();
        String filePath = LocalStorage.UPLOAD_DIR;
        File dest = new File(filePath + fileName);
        try {
            file.transferTo(dest);
            return "上传成功";
        } catch (IOException ignored) {
        }
        return "上传失败！";
    }
}
