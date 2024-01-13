package smart.lib.status;

/**
 * 账号状态
 */
public enum AccountStatus {
    NORMAL(0L, "正常"),
    ABNORMAL_CLOSED(1L, "临时关闭"),
    PERMANENT_CLOSED(2L, "永久关闭");
    long code;
    String info;

    AccountStatus(long code, String info) {
        this.code = code;
        this.info = info;
    }

    public long getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static AccountStatus getInstance(long code) {
        for (var item : AccountStatus.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getStatusInfo(long code) {
        var item = getInstance(code);
        if (item == null) {
            return null;
        }
        return item.getInfo();
    }
}
