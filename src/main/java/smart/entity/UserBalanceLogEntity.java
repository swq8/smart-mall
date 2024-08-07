package smart.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.config.AppConfig;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.math.BigDecimal;
import java.sql.Timestamp;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_user_balance_log")
public class UserBalanceLogEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = AppConfig.DATE_TIME_FORMAT, timezone = AppConfig.TIME_ZONE)

    private Timestamp time;

    @NotNull(groups = {Add.class, Edit.class})
    private Long uid;

    @Digits(integer = 12, fraction = 2)
    @NotNull(groups = {Add.class, Edit.class})
    private BigDecimal amount;
    private BigDecimal balance;
    @NotBlank(groups = {Add.class, Edit.class})
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
