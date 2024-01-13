package smart.lib;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.validation.BindingResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * rest result json data
 */
public class ApiJsonResult {
    public final static int CODE_SUCCESS = 1;
    public final static int CODE_FORBIDDEN = 2;
    public final static int CODE_UNAUTHORIZED = 3;
    public final static int CODE_BAD_REQUEST = 4;
    public final static int CODE_ERROR = 5;


    private int code;

    private Map<String, Object> data = new LinkedHashMap<>();


    private String msg;

    public static ApiJsonResult badRequest(String msg) {
        return new ApiJsonResult().setCode(CODE_BAD_REQUEST).setMsg(msg);

    }

    /**
     * return first parameter bind error
     *
     * @param bindingResult bind result
     * @return result
     */
    public static ApiJsonResult badRequest(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        if (bindingResult.hasErrors()) {
            var err = bindingResult.getFieldErrors().get(0);
            sb.append("数据错误, 字段 ")
                    .append(err.getField())
                    .append(" ")
                    .append(err.getDefaultMessage());

        }
        return ApiJsonResult.badRequest(sb.toString());
    }

    /**
     * generate error result response
     *
     * @param msg error msg
     * @return result
     */
    public static ApiJsonResult error(String msg) {
        return new ApiJsonResult().setCode(CODE_ERROR).setMsg(msg);

    }


    public static ApiJsonResult success() {
        return ApiJsonResult.success("");
    }

    /**
     * generate success result response
     *
     * @param msg success msg
     * @return result
     */
    public static ApiJsonResult success(String msg) {
        return new ApiJsonResult().setCode(CODE_SUCCESS).setMsg(msg);
    }

    /**
     * generate pagination response
     *
     * @param pagination pagination
     * @return ApiJsonResult
     */
    public static ApiJsonResult success(Pagination pagination) {
        return ApiJsonResult.success()
                .putDataItem("rows", pagination.getRows())
                .putDataItem("totalPages", pagination.getTotalPages())
                .putDataItem("totalRecords", pagination.getTotalRecords());
    }

    /**
     * return success if msg equals null, else return error with msg
     *
     * @param msg msg
     * @return result
     */
    public static ApiJsonResult successOrError(String msg) {
        if (msg == null) return ApiJsonResult.success();
        return ApiJsonResult.error(msg);
    }

    public int getCode() {
        return code;
    }

    public ApiJsonResult setCode(int code) {
        this.code = code;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ApiJsonResult setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public ApiJsonResult putDataItem(String key, Object value) {
        data.put(key, value);
        return this;
    }

    @JsonIgnore
    public boolean isError() {
        return code == ApiJsonResult.CODE_ERROR;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public ApiJsonResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return code == ApiJsonResult.CODE_SUCCESS;
    }
    public boolean notSuccess(){
        return code != ApiJsonResult.CODE_SUCCESS;
    }

}
