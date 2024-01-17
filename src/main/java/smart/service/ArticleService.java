package smart.service;

import org.springframework.stereotype.Service;
import smart.cache.ArticleCache;
import smart.entity.ArticleEntity;
import smart.lib.Pagination;
import smart.util.DbUtils;
import smart.util.SqlBuilder;
import smart.dto.GeneralQueryDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    public String delete(Long id) {
        DbUtils.deleteById(ArticleEntity.class, id);
        ArticleCache.update();
        return null;
    }

    public String save(ArticleEntity articleEntity) {
        if (ArticleCache.getArticleCategoryById(articleEntity.getCateId()) == null) {
            return "文章类别不存在";
        }
        if (articleEntity.getId() == null) {
            DbUtils.insert(articleEntity);
        } else {
            DbUtils.update(articleEntity);
        }
        ArticleCache.update();
        return null;
    }

    public Pagination query(GeneralQueryDto query) {
        List<Object> sqlParams = new ArrayList<>();
        String sql = """
                select a.id,
                       a.cate_id,
                       ac.name as cate_name,
                       a.content,
                       a.title,
                date_format(a.release_time, '%Y-%m-%d %T') as release_time,
                       a.visible,
                       a.order_num
                from t_article a
                         left join t_article_category ac on a.cate_id = ac.id
                """;
        var sqlBuilder = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("a.title", query.getName())
                .andEqualsIfNotNull("a.cate_id", query.getCid())
                .orderBy("orderNum,asc")
                .orderBy("releaseTime,desc");
        return Pagination.newBuilder(sqlBuilder.buildSql(), sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }
}