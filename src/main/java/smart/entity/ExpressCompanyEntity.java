package smart.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_express_company")
public class ExpressCompanyEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;
    @NotBlank(groups = {Add.class, Edit.class})
    private String name;
    @NotNull(groups = {Add.class, Edit.class})
    private Boolean enable;
    @NotNull(groups = {Add.class, Edit.class})
    private String url;
    @NotNull(groups = {Add.class, Edit.class})
    private Long orderNum;

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

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
