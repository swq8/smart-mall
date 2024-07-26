package smart.dto.express;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * free express rule
 */
public class FreeRuleDto {
    //排除省份
    @NotNull
    private final List<Long> exclude = new ArrayList<>();
    // 是否启用包邮规则
    @NotNull
    private Boolean enable = false;

    @Digits(integer = 8, fraction = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal amount = BigDecimal.ZERO;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<Long> getExclude() {
        return exclude;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
