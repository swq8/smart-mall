package smart.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import smart.util.Helper;

public class AdminToken extends Token {
    private final String[] authorize;

    public AdminToken(String[] authorize) {
        this.authorize = authorize == null ? new String[0] : authorize;
    }

    public String[] getAuthorize() {
        return authorize;
    }

    public boolean hasAuthorize(String name) {
        for (var item : authorize) if (name.equals(item)) return true;
        return false;
    }

    public static AdminToken from(HttpServletRequest request) {
        return (AdminToken) request.getAttribute(StringUtils.uncapitalize(AdminToken.class.getSimpleName()));
    }


}
