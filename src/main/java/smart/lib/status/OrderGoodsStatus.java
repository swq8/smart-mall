package smart.lib.status;

/**
 * 订单中的商品状态
 */
public enum OrderGoodsStatus {
    UNSHIPPED(0L, "未发货"),
    SHIPPED(1L, "已发货"),
    RECEIVED(2L, "已收货"),
    RETURNED(3L, "已退货");

    private final long code;
    private final String name;

    OrderGoodsStatus(long code, String name) {
        this.code = code;
        this.name = name;

    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static OrderGoodsStatus getInstance(long code) {
        for (var item : OrderGoodsStatus.values()) {
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
