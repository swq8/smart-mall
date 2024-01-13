package smart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;


@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_admin_menu")
public class AdminMenuEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;
    private Boolean cache;
    private String component;

    @NotNull(groups = {Add.class, Edit.class})
    private Boolean enable;
    @NotNull(groups = {Add.class, Edit.class})
    private Boolean visible;

    private String icon;

    @NotBlank(groups = {Add.class, Edit.class})
    private String name;

    private Integer orderNum;

    @NotNull(groups = {Add.class, Edit.class})
    private Long parentId;

    @Null(groups = {Add.class, Edit.class})
    @Transient
    private String fullRoute;

    @NotBlank(groups = {Add.class, Edit.class})
    private String route;

    @Min(value = 1, groups = {Add.class, Edit.class})
    @Max(value = 3, groups = {Add.class, Edit.class})
    @NotNull(groups = {Add.class, Edit.class})
    private Integer type;

    public Boolean getCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean hidden) {
        this.visible = hidden;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getFullRoute() {
        return fullRoute;
    }

    public void setFullRoute(String fullRoute) {
        this.fullRoute = fullRoute;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

}