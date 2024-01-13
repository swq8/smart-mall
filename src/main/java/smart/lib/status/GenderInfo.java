package smart.lib.status;

/**
 * 性别状态
 */
public enum GenderInfo {
    UNKNOWN(0L, "未知"),
    MALE(1L, "男"),
    FEMALE(2L, "女");
    final long code;
    final String info;

    GenderInfo(long code, String info) {
        this.code = code;
        this.info = info;
    }

    public long getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    public static GenderInfo getInstance(long code) {
        for (var item : GenderInfo.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getGenderInfo(long code) {
        var item = getInstance(code);
        if (item == null) {
            return null;
        }
        return item.getInfo();
    }

}
