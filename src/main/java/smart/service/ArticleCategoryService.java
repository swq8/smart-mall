package smart.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import smart.cache.ArticleCache;
import smart.entity.ArticleCategoryEntity;
import smart.repository.ArticleRepository;
import smart.util.DbUtils;

@Service
public class ArticleCategoryService {

    @Resource
    ArticleRepository articleRepository;

    public String delete(Long id) {
        if (articleRepository.countByCateId(id) > 0) {
            return "请先删除该分类下文章";
        }
        var entity = new ArticleCategoryEntity();
        entity.setId(id);
        DbUtils.delete(entity);
        ArticleCache.update();
        return null;
    }

    public String save(ArticleCategoryEntity articleCategoryEntity) {
        if (articleCategoryEntity.getId() == null) {
            DbUtils.insert(articleCategoryEntity);
        } else {
            DbUtils.update(articleCategoryEntity);
        }
        ArticleCache.update();
        return null;
    }
}
