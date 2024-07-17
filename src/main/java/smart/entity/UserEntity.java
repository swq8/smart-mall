package smart.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.config.AppConfig;
import smart.lib.status.AccountStatus;
import smart.lib.status.GenderInfo;
import smart.util.Helper;

import java.math.BigDecimal;
import java.sql.Timestamp;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_user")
public class UserEntity extends AbstractEntity {
    private String avatar;
    private BigDecimal balance;
    private String email;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String lastLoginIp;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = AppConfig.DATE_TIME_FORMAT, timezone = AppConfig.TIME_ZONE)
    private Timestamp lastLoginTime;
    private Integer level;
    private String phone;
    private String name;
    @JsonIgnore
    private String password;
    private Long gender;
    private Long status;
    private String registerIp;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = AppConfig.DATE_TIME_FORMAT, timezone = AppConfig.TIME_ZONE)
    private Timestamp registerTime;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }


    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

    public Long getGender() {
        return gender;
    }

    public void setGender(Long code) {
        this.gender = code;
    }

    public String getGenderStr() {
        return GenderInfo.getGenderName(gender);
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getStatusStr() {
        return AccountStatus.getStatusName(status);
    }


    public String getBalanceStr() {
        return Helper.priceFormat(balance);
    }

    public Timestamp getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Timestamp registerTime) {
        this.registerTime = registerTime;
    }
}