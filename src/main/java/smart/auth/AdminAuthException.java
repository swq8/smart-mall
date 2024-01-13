package smart.auth;

/**
 * 管理员鉴权异常类,在未登录或无权限时抛出
 */
public class AdminAuthException extends Exception {
    // 权限不足
    private final boolean unauthorized;


    /**
     * @param unauthorized true: needAuthorize  false: need login
     */
    public AdminAuthException(boolean unauthorized) {
        this.unauthorized = unauthorized;
    }

    public boolean isUnauthorized() {
        return unauthorized;
    }
}
