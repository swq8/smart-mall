package smart.cache;

import smart.entity.ArticleCategoryEntity;
import smart.entity.ArticleEntity;
import smart.repository.ArticleCategoryRepository;
import smart.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 文章系统缓存
 */
@Component
public class ArticleCache {

    private static ArticleRepository articleRepository;
    private static ArticleCategoryRepository articleCategoryRepository;

    private static List<ArticleCategoryEntity> list;
    private static List<ArticleEntity> articles;

    /**
     * 获取文章内容
     *
     * @param articleId 文章ID
     * @return 文章内容
     */
    public static ArticleEntity getArticleById(long articleId) {
        for (ArticleEntity article : articles) {
            if (article.getId() == articleId) {
                return article;
            }
        }
        return null;
    }

    /**
     * 获取文章类别
     *
     * @param categoryId category id
     * @return article category
     */
    public static ArticleCategoryEntity getArticleCategoryById(long categoryId) {
        for (var item : getList()) {
            if (item.getId() == categoryId) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取文章类别列表
     *
     * @return article category list
     */
    public static List<ArticleCategoryEntity> getList() {
        return list;
    }

    @PostConstruct
    public synchronized static void update() {
        list = new ArrayList<>();
        list = articleCategoryRepository.findAllByOrderByOrderNum();
        articles = articleRepository.findAllVisible();
        list.forEach(cate -> {
            List<ArticleEntity> articles1 = new ArrayList<>();
            articles.forEach(article -> {
                if (Objects.equals(cate.getId(), article.getCateId())) {
                    article.setCateName(cate.getName());
                    articles1.add(article);
                }
            });
            cate.setArticles(articles1);
        });
    }

    @Autowired
    private void autowire(ArticleRepository repository) {
        articleRepository = repository;
    }

    @Autowired
    private void autowire(ArticleCategoryRepository repository) {
        articleCategoryRepository = repository;
    }
}
