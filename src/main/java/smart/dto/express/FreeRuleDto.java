package smart.dto.express;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

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

    @NotNull
    @PositiveOrZero
    private Long amount = 0L;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<Long> getExclude() {
        return exclude;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

}
