package alex.lib.express;

import java.util.ArrayList;
import java.util.List;

public class ProvincePrice {
    private List<Long> provinces;
    // 首重价格
    private long firstPrice;
    // 续重价格
    private long additionalPrice;
    public ProvincePrice(){
        provinces = new ArrayList<>();
    }

    public List<Long> getProvinces() {
        return provinces;
    }

    public long getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(long additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public long getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(long firstPrice) {
        this.firstPrice = firstPrice;
    }
}
