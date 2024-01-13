package smart.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.ArticleCache;
import smart.cache.SystemCache;
import smart.util.Helper;

@Controller
@RequestMapping(path = "article")
public class Article {

    @GetMapping(path = "detail/{id:\\d{1,18}}")
    public ModelAndView getDetail(@PathVariable("id") long id, HttpServletRequest request) {
        var article = ArticleCache.getArticleById(id);
        if (article == null) {
            return Helper.msgPage("信息不存在", request);
        }
        ModelAndView view = Helper.newModelAndView("article/detail", request);
        view.addObject("article", article);
        view.addObject("title", SystemCache.getSiteName() + '-' + article.getCateName());
        return view;
    }

    @GetMapping(path = "list")
    public ModelAndView getList(HttpServletRequest request) {
        // category id
        long cid = Helper.parseNumber(request.getParameter("cid"), Long.class);
        Object articles = null, cateName = null;
        for (var item : ArticleCache.getList()) {
            if (item.getId() == cid) {
                articles = item.getArticles();
                cateName = item.getName();
                break;
            }
        }
        if (articles == null) {
            return Helper.msgPage("文章分类不存在", request);
        }
        ModelAndView view = Helper.newModelAndView("article/list", request);
        view.addObject("articles", articles);
        view.addObject("cateId", cid);
        view.addObject("cateName", cateName);
        view.addObject("title", SystemCache.getSiteName() + '-' + cateName);
        return view;

    }
}
