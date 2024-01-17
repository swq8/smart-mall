package smart.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class AdminToken extends Token {
    private final String[] authorize;

    public AdminToken(String[] authorize) {
        this.authorize = authorize == null ? new String[0] : authorize;
    }

    public static AdminToken from(HttpServletRequest request) {
        return (AdminToken) request.getAttribute(StringUtils.uncapitalize(AdminToken.class.getSimpleName()));
    }

    public String[] getAuthorize() {
        return authorize;
    }
}
