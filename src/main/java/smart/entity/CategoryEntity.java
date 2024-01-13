package smart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_category")
public class CategoryEntity extends AbstractEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;
    @NotNull(groups = {Add.class, Edit.class})
    private Boolean homepage;
    @NotNull(groups = {Add.class, Edit.class})
    private Long parentId;
    @NotBlank(groups = {Add.class, Edit.class})
    private String name;
    @NotNull(groups = {Add.class, Edit.class})
    private Integer orderNum;
    @NotNull(groups = {Add.class, Edit.class})
    private Boolean visibility;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHomepage() {
        return homepage;
    }

    public void setHomepage(Boolean homepage) {
        this.homepage = homepage;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}