package smart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Range;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_admin_role")
public class AdminRoleEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;

    @NotEmpty(groups = {Add.class, Edit.class})
    private String name;

    @NotNull(groups = {Add.class, Edit.class})
    @Range(min = 1, max = 999,groups = {Add.class, Edit.class})
    private Integer orderNum;
    @NotNull(groups = {Add.class, Edit.class})
    @Pattern(regexp = "^(\\d+,?)*$", groups = {Add.class, Edit.class})
    private String authorize;

    @NotNull(groups = {Add.class, Edit.class})
    private Boolean enable;

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

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getAuthorize() {
        return authorize;
    }

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

}