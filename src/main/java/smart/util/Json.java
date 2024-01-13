package smart.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * json 助手类
 */
@Component
public class Json {
    private static ObjectMapper mapper;

    public Json(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }

    /**
     * json 字符串转 map
     *
     * @param str json字符串
     * @return map
     */
    public static Map<String, String> parseStringMap(String str) {
        Map<String, String> map = null;
        try {
            map = mapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return map;
    }

    public static Map<String, Object> parseObjectMap(String str) {
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return map;
    }


    /**
     * json字符串转对象实列
     *
     * @param str    json字符串
     * @param tClass 实例的类型
     * @param <T>    泛型
     * @return 转换的对象实例，失败返回null
     */
    public static <T> T parse(String str, Class<T> tClass) {
        try {
            return mapper.readValue(str, tClass);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static List<Map<String, Object>> parseObjectMaps(String str) {

        List<Map<String, Object>> list = null;
        try {
            list = mapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return list;
    }


    /***
     * json字符串转换为List<Map<String, String>>
     * @param str json字符串
     * @return 转换后的List, 失败返回null
     */
    public static List<Map<String, String>> parseStringMaps(String str) {

        List<Map<String, String>> list = null;
        try {
            list = mapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return list;
    }

    /**
     * json字符串转换为指定类实例的List
     *
     * @param str    json字符串
     * @param tClass 实例的类型
     * @param <T>    泛型
     * @return 转换后的List, 失败返回null
     */
    public static <T> List<T> parseList(String str, Class<T> tClass) {
        return parseList(str, tClass, false);
    }

    public static <T> List<T> parseList(String str, Class<T> tClass, boolean notReturnNull) {
        List<T> list = null;
        try {
            list = mapper.readValue(str, mapper.getTypeFactory().constructParametricType(List.class, tClass));
        } catch (JsonProcessingException ignored) {
        }
        if (list == null && notReturnNull) list = new ArrayList<>();
        return list;
    }

    /**
     * 对象序列化为 json字符串
     *
     * @param value 要转换成 json 的实列
     * @return json字符串, 失败返回null
     */
    public static String stringify(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }

}
