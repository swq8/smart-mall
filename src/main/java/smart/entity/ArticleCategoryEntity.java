package smart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.util.List;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_article_category")
public class ArticleCategoryEntity extends AbstractEntity {

    // 临时变量，存放该分类下的所有文章
    @Transient
    private List<ArticleEntity> articles;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;

    @NotBlank(groups = {Add.class, Edit.class})
    private String name;
    @NotNull(groups = {Add.class, Edit.class})
    private Boolean footerShow;
    @NotNull(groups = {Add.class, Edit.class})
    private Long orderNum;


    public List<ArticleEntity> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleEntity> articles) {
        this.articles = articles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFooterShow() {
        return footerShow;
    }

    public void setFooterShow(Boolean footerShow) {
        this.footerShow = footerShow;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }
}
