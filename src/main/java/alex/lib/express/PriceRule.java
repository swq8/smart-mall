package alex.lib.express;

import java.util.ArrayList;
import java.util.List;

public class PriceRule {

    // 默认首重g
    private long firstWeight;
    // 默认首重价格
    private long firstPrice;
    // 默认续重
    private long additionalWeight;
    // 默认续重价格
    private long additionalPrice;
    private final List<ProvincePrice> provincePrices = new ArrayList<>();

    // 其他地区默认运费
    private boolean otherDefault = true;

    public PriceRule(){}

    public boolean isOtherDefault() {
        return otherDefault;
    }

    public void setOtherDefault(boolean otherDefault) {
        this.otherDefault = otherDefault;
    }

    public long getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(long firstPrice) {
        this.firstPrice = firstPrice;
    }

    public long getFirstWeight() {
        return firstWeight;
    }

    public void setFirstWeight(long firstWeight) {
        this.firstWeight = firstWeight;
    }

    public long getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(long additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public long getAdditionalWeight() {
        return additionalWeight;
    }

    public void setAdditionalWeight(long additionalWeight) {
        this.additionalWeight = additionalWeight;
    }

    public List<ProvincePrice> getProvincePrices() {
        return provincePrices;
    }
}
