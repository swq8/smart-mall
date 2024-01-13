package smart.lib.status;

import java.util.HashMap;

/**
 * 支付状态
 */
public class PayStatus {
    static final HashMap<Long, String> STATUS;

    static {
        STATUS = new HashMap<>();
        STATUS.put(0L, "未支付");
        STATUS.put(1L, "已支付");
        STATUS.put(2L, "已退款");
    }

    public static String getStatusInfo(long code) {
        return STATUS.get(code);
    }
}
