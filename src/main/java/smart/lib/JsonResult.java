package smart.lib;

import smart.util.Json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JsonResult网页端机制通讯数据格式
 */
public class JsonResult {
    public final Map<String, String> data = new LinkedHashMap<>();
    public final Map<String, String> error = new LinkedHashMap<>();
    /**
     * 客户端是否需要刷新验证码
     */
    private boolean captcha = false;
    // 通知给客户端的消息
    private String msg;
    // 通知客户端跳转URL,如有msg先显示msg
    private String url;

    public static JsonResult Error(String key, String error) {
        var result = new JsonResult();
        result.error.put(key, error);
        return result;
    }

    public Boolean getCaptcha() {
        return captcha;
    }

    /**
     * @param captcha client need refresh captcha
     */
    public void setCaptcha(boolean captcha) {
        this.captcha = captcha;
    }

    public String getMsg() {
        return msg;
    }

    public JsonResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public JsonResult setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<>();

        if (captcha) {
            map.put("captcha", true);
        }
        map.put("msg", msg);
        map.put("url", url);
        map.put("data", data);
        map.put("error", error);
        return Json.stringify(map);
    }
}
