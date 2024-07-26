package smart.dto.express;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProvinceFee {
    @NotEmpty
    private List<Long> provinces = new ArrayList<>();

    // 首重价格
    @Digits(integer = 8, fraction = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal firstFee;
    // 续重价格
    @Digits(integer = 8, fraction = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal additionalFee;

    public List<Long> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<Long> provinces) {
        this.provinces = provinces;
    }

    public BigDecimal getFirstFee() {
        return firstFee;
    }

    public void setFirstFee(BigDecimal firstFee) {
        this.firstFee = firstFee;
    }

    public BigDecimal getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(BigDecimal additionalFee) {
        this.additionalFee = additionalFee;
    }
}
