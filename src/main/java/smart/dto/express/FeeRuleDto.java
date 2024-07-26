package smart.dto.express;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 快递价格规则
 */
public class FeeRuleDto {
    @NotNull
    @Valid
    private final List<ProvinceFee> provinceFees = new ArrayList<>();
    // 首重价格
    @Digits(integer = 8, fraction = 2)
    @NotNull
    @Positive
    private BigDecimal firstFee;
    // 首重重量g
    @NotNull
    @Positive
    private Long firstWeight;
    // 续重价格
    @Digits(integer = 8, fraction = 2)
    @NotNull
    @Positive
    private BigDecimal additionalFee;
    // 续重重量
    @NotNull
    @Positive
    private Long additionalWeight;
    // 其他地区默认运费
    @NotNull
    private Boolean otherDefault;

    public BigDecimal getFirstFee() {
        return firstFee;
    }

    public void setFirstFee(BigDecimal firstFee) {
        this.firstFee = firstFee;
    }

    public Long getFirstWeight() {
        return firstWeight;
    }

    public void setFirstWeight(Long firstWeight) {
        this.firstWeight = firstWeight;
    }

    public BigDecimal getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(BigDecimal additionalFee) {
        this.additionalFee = additionalFee;
    }

    public Long getAdditionalWeight() {
        return additionalWeight;
    }

    public void setAdditionalWeight(Long additionalWeight) {
        this.additionalWeight = additionalWeight;
    }

    public List<ProvinceFee> getProvinceFees() {
        return provinceFees;
    }

    public Boolean getOtherDefault() {
        return otherDefault;
    }

    public void setOtherDefault(Boolean otherDefault) {
        this.otherDefault = otherDefault;
    }


    /**
     * 计算物流费用
     *
     * @param code   收货地区代码
     * @param weight 重量g
     * @return 所需费用, 负数不支持该地区派送
     */
    public BigDecimal getShippingFee(long code, long weight) {
        BigDecimal fee1 = null, fee2 = null;
        // 查找目标区域首重、续重价格
        for (var rule : provinceFees) {
            if (rule.getProvinces().contains(code)) {
                fee1 = rule.getFirstFee();
                fee2 = rule.getAdditionalFee();
                break;
            }
        }
        if (fee1 == null || fee2 == null) {
            if (otherDefault) {
                fee1 = firstFee;
                fee2 = additionalFee;
            } else {
                return BigDecimal.valueOf(-1);
            }
        }

        //计算返回运费
        if (weight <= firstWeight) {
            return fee1;
        }
        weight -= firstWeight;
        long num = weight / additionalWeight;
        if (weight % additionalWeight > 0) {
            num++;
        }
        return fee2.subtract(new BigDecimal(num)).add(fee1);
    }
}
