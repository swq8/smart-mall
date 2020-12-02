package alex.lib.express;

import java.util.ArrayList;
import java.util.List;

/**
 * 包邮规则
 */
public class FreeRule {
    // 是否启用包邮规则
    private boolean enable = false;

    // 包邮价
    private long price;

    //排除省份
    private final List<Long> exclude = new ArrayList<>();

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<Long> getExclude() {
        return exclude;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
