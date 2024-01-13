package smart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import org.springframework.util.StringUtils;
import smart.util.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * base entity
 */
public abstract class AbstractEntity {

    /**
     * get field infos
     *
     * @return field infos
     */
    @JsonIgnore
    public Map<String, FiledInfo> getFieldInfos() {
        Map<String, FiledInfo> filedInfos = new LinkedHashMap<>();
        FiledInfo filedInfo;
        Class<?> cls = this.getClass();
        String getterName;
        Method method;
        for (Field field : cls.getDeclaredFields()) {
            filedInfo = new FiledInfo();
            filedInfo.setName(field.getName());
            filedInfo.setType(field.getType());
            getterName = "get" + StringUtils.capitalize(field.getName());
            try {
                method = cls.getDeclaredMethod(getterName);
            } catch (NoSuchMethodException ex) {
                LogUtils.warn(getClass(), ex);
                continue;
            }
            try {
                filedInfo.setValue(method.invoke(this));
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LogUtils.warn(getClass(), ex);
                continue;
            }
            filedInfo.setAnnotations(field.getAnnotations());
            filedInfo.setPrimaryKey(field.getAnnotation(Id.class) != null);
            filedInfos.put(filedInfo.getName(), filedInfo);
        }
        return filedInfos;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName()).append("(");
        Map<String, FiledInfo> fieldInfos = getFieldInfos();
        fieldInfos.forEach((name, filed) -> {
            builder.append(name).append("=").append(filed.getValue()).append(",");
        });
        if (builder.charAt(builder.length() - 1) == ',') {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        return builder.toString();
    }

}
