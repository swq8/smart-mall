package alex.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * rest result json data
 */
public class ApiJsonResult {

    /**
     * 0: success 1:request params error 2:token error
     */
    private int status = 0;
    private String msg;
    public final Map<String, String> data   = new LinkedHashMap<>();
    public final Map<String, String> error  = new LinkedHashMap<>();

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", status);
        map.put("msg", msg);
        map.put("data", data);
        map.put("error", error);
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.out.println(e.toString());
            return "error";
        }
    }
}
