package smart.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.config.AppConfig;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.sql.Timestamp;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_article")
public class ArticleEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;
    @NotNull(groups = { Add.class,Edit.class})
    private Long cateId;

    @Transient
    private String cateName;
    @NotBlank(groups = { Add.class,Edit.class})
    private String title;

    @Column(columnDefinition = "text")
    @NotNull(groups = { Add.class,Edit.class})
    private String content;
    @NotNull(groups = { Add.class,Edit.class})
    private Long orderNum;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = AppConfig.DATE_TIME_FORMAT, timezone = AppConfig.TIME_ZONE)
    @NotNull(groups = { Add.class,Edit.class})
    private Timestamp releaseTime;
    @NotNull(groups = { Add.class,Edit.class})
    private Boolean visible;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Timestamp getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Timestamp releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
