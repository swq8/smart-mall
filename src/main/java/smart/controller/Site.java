package smart.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.*;
import smart.lib.Captcha;
import smart.lib.Pagination;
import smart.lib.session.Session;
import smart.service.GoodsService;
import smart.util.Helper;
import smart.util.LogUtils;

import javax.imageio.ImageIO;
import java.io.IOException;

@Controller
@Transactional
public class Site {

    @Resource
    GoodsService goodsService;

    /**
     * 首页
     *
     * @param request request
     * @return view
     */
    @GetMapping(value = "/")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("index", request);
        modelAndView.addObject("carousel", SystemCache.getCarousel());
        modelAndView.addObject("newsArticle", ArticleCache.getArticleCategoryById(1));
        modelAndView.addObject("recommend", GoodsCache.getRecommend());
        modelAndView.addObject("title", SystemCache.getSiteName());
        return modelAndView;
    }

    /**
     * 验证码
     *
     * @param response response
     * @param session  session
     */
    @RequestMapping(value = "captcha")
    @ResponseBody
    public void getCaptcha(HttpServletResponse response, Session session) {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/png");
        var captchaResult = Captcha.getImageCode();
        session.set(StringUtils.uncapitalize(Captcha.class.getSimpleName()), captchaResult.phrase());
        try {
            ImageIO.write(captchaResult.image(), "png", response.getOutputStream());
        } catch (IOException ignored) {
        }
    }

    /**
     * 分类页面
     *
     * @param request http request
     */
    @GetMapping("category")
    public ModelAndView getCategory(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("category", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 分类");
        return modelAndView;

    }

    /**
     * 商品列表
     *
     * @param request request
     * @return view
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ModelAndView getList(
            HttpServletRequest request,
            @RequestParam(name = "cid", defaultValue = "") String cidStr,
            @RequestParam(name = "page", defaultValue = "") String pageStr,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String sort
    ) {
        long cid = Helper.parseNumber(cidStr, Long.class);
        long page = Helper.parseNumber(pageStr, Long.class);
        ModelAndView modelAndView = Helper.newModelAndView("list", request);
        Pagination pagination = goodsService.getGoodsList(cid, q, sort, page);
        modelAndView.addObject("cid", cid);
        modelAndView.addObject("q", q);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("categoryPath", CategoryCache.getCategoryPath(cid));
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 商品列表");
        return modelAndView;
    }

    /**
     * redirect manage platform
     *
     * @return index
     */
    @GetMapping({"manage", "manage/"})
    public String getManage() {
        return "redirect:/manage/index.html";
    }

    @GetMapping(path = "region", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getRegion() {
        return RegionCache.getRegionJson();
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public String getTest(Session session) {
        return null;
    }


    /**
     * 显示消息页面
     *
     * @param request  request
     * @param response response
     * @param session  session
     * @return 消息页面
     */
    @GetMapping(value = "msg")
    public ModelAndView getMsg(HttpServletRequest request, HttpServletResponse response, Session session) {
        ModelAndView modelAndView = Helper.newModelAndView("msg", request);
        var msg = session.get("msg");
        if (msg == null) {
            try {
                response.sendRedirect("/");
            } catch (IOException ex) {
                LogUtils.error(getClass(), "", ex);
            }
        }
        modelAndView.addObject("msg", msg);
        modelAndView.addObject("backUrl", session.get("backUrl"));
        session.delete("msg", "backUrl");
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 信息");
        return modelAndView;
    }
}
