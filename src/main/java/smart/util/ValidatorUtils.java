package smart.util;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import smart.lib.Captcha;
import smart.lib.session.Session;

import java.util.regex.Pattern;

@Component
public class ValidatorUtils {

    public final static String ID_OR_PWD_ERROR = "账号或密码错误";
    private static Validator validator;

    public ValidatorUtils(Validator validator) {
        ValidatorUtils.validator = validator;
    }

    public static String validate(Object obj, Class<?>... classes) {
        var violation = validator.validate(obj, classes);
        if (violation.isEmpty()) {
            return null;
        }
        var hint = violation.iterator().next();
        return "%s %s".formatted(hint.getPropertyPath().toString(), hint.getMessage());
    }

    public static String validateAddress(String data, String alias) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }
        data = data.trim();
        if (data.length() < 6) {
            return alias + "最少需要6个字";
        }
        if (data.length() > 50) {
            return alias + "最多不得超过50个字";
        }
        return null;
    }

    /**
     * validate captcha
     *
     * @param captcha captcha
     * @param session http session
     * @param alias   alias
     * @return null: success or error msg
     */
    public static String validateCaptcha(String captcha, Session session, String alias) {
        if (captcha != null && session != null) {
            String val = (String) session.get(StringUtils.uncapitalize(Captcha.class.getSimpleName()));
            if (captcha.equals(val)) {
                return null;
            }
        }
        return alias + "不正确";
    }

    /**
     * validate ip address
     *
     * @param data  The string to be validated
     * @param alias alias
     * @return null: success or error msg
     */
    public static String validateIp(String data, String alias) {
        if (Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$", data)) {
            return null;
        }
        return alias + "不正确";
    }

    /**
     * 字母或数字
     *
     * @param data
     * @param alias
     * @return null 成功， or 错误信息
     */
    public static String validateLetterOrNumber(String data, String alias) {
        if (Pattern.matches("^[0-9a-zA-Z]*$", data)) {
            return null;
        }
        return alias + "只能由字母或数字组成";
    }

    /**
     * validate mobile phone
     *
     * @param data
     * @param alias
     * @return null 成功， or 错误信息
     */
    public static String validateMobile(String data, String alias) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        if (!Pattern.matches("^1\\d{10}$", data)) {
            return alias + "不正确";
        }
        return null;
    }

    /**
     * 用户名判断
     *
     * @param data  要判断的用户名
     * @param alias 用户名别称
     * @return null 成功， or 错误信息
     */
    public static String validateName(String data, String alias) {
        String msg = notEmpty(data, alias);
        if (msg != null) {
            return msg;
        }
        if (data.length() < 4) {
            return alias + "不能小于4位";
        }
        if (data.length() > 12) {
            return alias + "不能超过12位";
        }
        if (Pattern.matches("^\\d+$", data)) {
            return alias + "不能是纯数字";
        }
        if (!Pattern.matches("^[a-z\\d]+$", data)) {
            return alias + "只能由字母或数字组成";
        }
        if (data.toLowerCase().startsWith("admin")) {
            return alias + "不能以 admin 开头";
        }
        return null;
    }

    public static boolean validateNotNameAndPassword(String name, String password) {
        return !(validateName(name, "") == null && validatePassword(password, "") == null);
    }

    /**
     * 不为空判断
     *
     * @param data  要判断的内容,内容不能为null,或全是空格
     * @param alias 别名
     * @return null 成功， or 错误信息
     */
    public static String notEmpty(String data, String alias) {
        if (data == null || data.trim().isEmpty()) {
            return alias + "不能为空";
        }

        return null;
    }

    public static String validatePassword(String data, String alias) {
        String msg = notEmpty(data, alias);
        if (msg != null) {
            return msg;
        }

        if (data.length() < 6) {
            return alias + "过于简单";
        }
        return null;
    }

    public static String validatePrice(String data, String alias) {
        String msg = notEmpty(data, alias);
        if (msg != null) {
            return msg;
        }
        data = data.trim();
        if (Pattern.matches("^\\d+$", data)) {
            return null;
        }
        if (Pattern.matches("^\\d+\\.\\d{0,2}$", data)) {
            return null;
        }
        return alias + "格式不正确";
    }
}
