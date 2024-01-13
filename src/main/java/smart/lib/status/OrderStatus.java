package smart.lib.status;

/**
 * 订单状态
 */
public enum OrderStatus {
    WAIT_FOR_PAY(0L, "待付款"),
    WAIT_FOR_SHIPPING(1L, "待发货"),
    SHIPPED(2L, "已发货"),
    COMPLETED(3L, "已完成"),
    CANCELLED(4L, "已取消"),
    REFUND(5L, "已退款");

    private final long code;
    private final String name;

    OrderStatus(long code, String name) {
        this.code = code;
        this.name = name;

    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static OrderStatus getInstance(long code) {
        for (var item : OrderStatus.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getStatusName(long code) {
        var item = getInstance(code);
        if (item == null) {
            return null;
        }
        return item.getName();
    }
}
