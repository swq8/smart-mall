package smart.dto.express;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

public class ProvinceFee {
    @NotEmpty
    private List<Long> provinces = new ArrayList<>();
    @NotNull
    @PositiveOrZero
    // 首重价格
    private Long firstFee;
    @NotNull
    @PositiveOrZero
    // 续重价格
    private Long additionalFee;

    public List<Long> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<Long> provinces) {
        this.provinces = provinces;
    }

    public Long getFirstFee() {
        return firstFee;
    }

    public void setFirstFee(Long firstFee) {
        this.firstFee = firstFee;
    }

    public Long getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Long additionalFee) {
        this.additionalFee = additionalFee;
    }
}
