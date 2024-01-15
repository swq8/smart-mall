package smart.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.util.validategroups.Add;
import smart.util.validategroups.Delete;
import smart.util.validategroups.Edit;

import java.sql.Timestamp;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_admin_user")
public class AdminUserEntity extends AbstractEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;

    @Null(groups = {Add.class, Edit.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
    @NotNull(groups = Delete.class)
    private Long userId;

    @NotNull(groups = {Add.class, Edit.class})
    private Boolean enable;

    @NotBlank(groups = {Add.class, Edit.class})
    @Transient
    private String name;

    @NotNull(groups = {Add.class, Edit.class})
    @Pattern(regexp = "^(\\d+,?)*$", groups = {Add.class, Edit.class})
    private String rolesId;
    private String trueName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRolesId() {
        return rolesId;
    }

    public void setRolesId(String rolesId) {
        this.rolesId = rolesId;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }
}