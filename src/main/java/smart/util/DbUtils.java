package smart.util;

import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import smart.entity.AbstractEntity;
import smart.entity.FiledInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * database utils
 */
@Component
public class DbUtils {

    private final static int LOCK_TYPE_NULL = 0;
    private final static int LOCK_TYPE_READ = 1;
    private final static int LOCK_TYPE_WRITE = 2;

    private static JdbcClient jdbcClient;


    /**
     * camel case to underscores naming name
     *
     * @param name camel case naming name
     * @return underscores naming name
     */
    public static String camelCaseToUnderscoresNaming(String name) {
        if (name == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(name);
        for (int i = 1; i < builder.length(); i++) {
            if (Character.isLowerCase(builder.charAt(i - 1)) && Character.isUpperCase(builder.charAt(i)) && Character.isLowerCase(builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return builder.toString().toLowerCase();
    }

    /**
     * commit
     */
    public static void commit() {
        jdbcClient.sql("commit").update();
    }


    /**
     * 获取表行数
     *
     * @param entityClass 表的实体类
     * @param condition   条件
     * @return 符合条件的行数
     */
    public static long count(Class<? extends AbstractEntity> entityClass, Map<String, Object> condition) {
        var conditionObject = getConditionObject(condition);
        String sql = "SELECT COUNT(*) FROM `" + getTableName(entityClass) + "`" + conditionObject.sql;
        return jdbcClient.sql(sql).params(conditionObject.params()).query(Long.class).single();
    }

    /**
     * delete row by primary key
     *
     * @param entity table entity
     * @return deleted num
     */
    public static long delete(AbstractEntity entity) {
        var pk = getPrimaryKey(entity);
        if (pk == null) {
            throw new IllegalArgumentException("missing primary key");
        }
        return delete(entity.getClass(), Map.of(pk.getName(), pk.getValue()));
    }

    /**
     * 删除符合条件的行
     *
     * @param entityClass table entity class
     * @param condition   condition
     * @return deleted num
     */
    public static long delete(Class<? extends AbstractEntity> entityClass, Map<String, Object> condition) {
        // condition cannot be empty
        if (condition.isEmpty()) {
            throw new IllegalArgumentException("condition cannot be empty");
        }
        var conditionObject = getConditionObject(condition);
        String sql = "DELETE FROM `" + getTableName(entityClass) + "`" + conditionObject.sql();
        return jdbcClient.sql(sql).params(conditionObject.params).update();
    }

    public static boolean deleteById(Class<? extends AbstractEntity> entityClass, long id) {
        return jdbcClient.sql("delete from `" + getTableName(entityClass) + "` where id = ?")
                .param(id)
                .update() > 0;
    }

    public static <T extends AbstractEntity> List<T> findAll(Class<T> entityClass) {
        String sql = "select * from `" +
                getTableName(entityClass) +
                "`";

        return jdbcClient.sql(sql)
                .query(entityClass)
                .list();
    }

    public static <T extends AbstractEntity> T findById(long id, Class<T> entityClass) {
        return findById(id, entityClass, LOCK_TYPE_NULL);
    }

    public static <T extends AbstractEntity> T findByIdForRead(long id, Class<T> entityClass) {
        return findById(id, entityClass, LOCK_TYPE_READ);
    }

    public static <T extends AbstractEntity> T findByIdForWrite(long id, Class<T> entityClass) {
        return findById(id, entityClass, LOCK_TYPE_WRITE);
    }

    private static <T extends AbstractEntity> T findById(long id, Class<T> entityClass, int lockType) {
        StringBuilder sql = new StringBuilder("select * from `")
                .append(getTableName(entityClass))
                .append("` where id = ?");
        switch (lockType) {
            case LOCK_TYPE_READ -> sql.append(" lock in share mode");
            case LOCK_TYPE_WRITE -> sql.append(" for update");
        }
        return jdbcClient.sql(sql.toString())
                .param(id)
                .query(entityClass)
                .optional()
                .orElse(null);
    }

    /**
     * get table name by entity
     *
     * @param entity table entity
     * @return table name
     */
    public static String getTableName(Class<? extends AbstractEntity> entity) {
        Table annotation = entity.getAnnotation(Table.class);
        if (annotation == null || !StringUtils.hasLength(annotation.name())) {
            throw new IllegalArgumentException("entity has not table name");
        }
        return annotation.name();
    }

    /**
     * 获取最近的自增id
     *
     * @return 自增id值
     */
    public static long getLastInsertId() {
        return jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).optional().orElse(0L);
    }

    /**
     * batch insert
     *
     * @param entities the entities to insert, field will be ignored if value is null in first entity
     */
    public static void insertAll(List<? extends AbstractEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        final List<String> fieldNames = new LinkedList<>();
        final List<Object> params = new LinkedList<>();
        entities.getFirst().getFieldInfos().forEach((k, v) -> {
            if (v.getValue() != null) {
                fieldNames.add(k);
            }
        });
        StringBuilder sql = new StringBuilder(getInsertHeader(getTableName(entities.getFirst().getClass()), fieldNames.stream().map(DbUtils::camelCaseToUnderscoresNaming).toArray(String[]::new)));
        for (var entity : entities) {
            sql.append("(");
            Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
            for (String fieldName : fieldNames) {
                var val = fieldInfos.get(fieldName).getValue();
                if (val == null) {
                    throw new IllegalArgumentException("value not be null");
                }
                sql.append("?,");
                params.add(val);
            }
            sql.deleteCharAt(sql.length() - 1).append("),");
        }
        sql.deleteCharAt(sql.length() - 1);
        jdbcClient.sql(sql.toString()).params(params).update();
    }

    /**
     * insert entity
     *
     * @param entity the entity to insert, property will be ignored if value is null
     */
    public static void insert(AbstractEntity entity) {
        List<String> sqlFieldNames = new LinkedList<>();
        List<Object> params = new LinkedList<>();
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        fieldInfos.forEach((name, field) -> {
            if (field.getAnnotation(Transient.class) == null && field.getValue() != null) {
                sqlFieldNames.add(DbUtils.camelCaseToUnderscoresNaming(name));
                params.add(field.getValue());
            }
        });
        StringBuilder sql = new StringBuilder(getInsertHeader(getTableName(entity.getClass()), sqlFieldNames.toArray(String[]::new))).append("(").append("?,".repeat(sqlFieldNames.size()));
        sql.deleteCharAt(sql.length() - 1).append(")");
        jdbcClient.sql(sql.toString()).params(params).update();
    }

    /**
     * rollback
     */
    public static void rollback() {
        jdbcClient.sql("rollback").update();
    }

    /**
     * underscores to camel case naming
     *
     * @param name underscores naming name
     * @return camel case naming name
     */
    public static String underscoresToCamelCaseNaming(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase();
        StringBuilder sb = new StringBuilder(name);
        for (int i = 2; i < sb.length(); ) {
            if (sb.charAt(i - 1) == '_') {
                sb.replace(i - 1, i + 1, String.valueOf(sb.charAt(i)).toUpperCase());
            } else {
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 更新行数据
     *
     * @param entityClass  表的实体类
     * @param conditionMap 条件
     * @param row          the map to update, field will be remove if value is null
     * @return int
     */
    public static int update(Class<? extends AbstractEntity> entityClass, Map<String, Object> conditionMap, Map<String, Object> row) {
        row.values().removeIf(Objects::isNull);
        ConditionObject conditionObject = getConditionObject(conditionMap);
        StringBuilder sql = new StringBuilder(String.format("UPDATE `%s` SET", getTableName(entityClass)));
        List<Object> params = new LinkedList<>();
        for (String key : row.keySet()) {
            if (row.get(key) != null) {
                sql.append(String.format(" `%s`=?,", key));
                params.add(row.get(key));
            }
        }
        sql.deleteCharAt(sql.length() - 1).append(conditionObject.sql());
        params.addAll(conditionObject.params);
        return jdbcClient.sql(sql.toString()).params(params).update();
    }

    /**
     * update entity by primary key
     *
     * @param entity     the entity to update, property will be ignored if value is null
     * @param fieldNames update with filed names, default all
     * @return rows num
     */
    public static int update(AbstractEntity entity, String... fieldNames) {
        AtomicReference<FiledInfo> idFiledInfo = new AtomicReference<>();
        Set<String> unusedNames = new HashSet<>();
        Collections.addAll(unusedNames, fieldNames);
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        Map<String, Object> row = new LinkedHashMap<>();
        fieldInfos.forEach((name, field) -> {
            // primary key
            if (field.isPrimaryKey()) {
                idFiledInfo.set(field);
            } else if (field.getValue() != null && field.getAnnotation(Transient.class) == null && (fieldNames.length == 0 || unusedNames.contains(name))) {
                row.put(DbUtils.camelCaseToUnderscoresNaming(name), field.getValue());
            }
            unusedNames.remove(name);
        });
        FiledInfo primaryKey = idFiledInfo.get();
        if (primaryKey == null) {
            throw new IllegalArgumentException("missing primary key");
        }
        if (!unusedNames.isEmpty()) {
            throw new IllegalArgumentException("illegal filed name: " + Arrays.toString(unusedNames.toArray()));
        }
        return update(entity.getClass(), Map.of(primaryKey.getName(), primaryKey.getValue()), row);
    }

    private static String getInsertHeader(String tableName, String... colNames) {
        StringBuilder sql = new StringBuilder("INSERT INTO `");
        sql.append(tableName).append("` (");
        for (var name : colNames) {
            sql.append("`").append(name).append("`,");
        }
        sql.deleteCharAt(sql.length() - 1).append(") VALUES ");
        return sql.toString();
    }

    private static ConditionObject getConditionObject(Map<String, Object> condition) {
        //Prevent mistakes
        if (CollectionUtils.isEmpty(condition)) {
            throw new IllegalArgumentException("condition must not be empty");
        }
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append(" WHERE");
        condition.forEach((k, v) -> {
            sql.append(" `").append(camelCaseToUnderscoresNaming(k)).append("`");
            if (v == null) {
                sql.append(" IS NULL");
            } else if (v instanceof Object[] objects) {
                if (objects.length == 0) {
                    throw new IllegalArgumentException(String.format("param '%s' must not empty", k));
                }
                sql.append(" IN (").append("?,".repeat(objects.length)).deleteCharAt(sql.length() - 1).append(")");
                params.addAll(List.of(objects));
            } else {
                sql.append("=?");
                params.add(v);
            }
            sql.append(" AND");
        });
        sql.delete(sql.length() - 4, sql.length());
        return new ConditionObject(sql.toString(), params);

    }

    private static FiledInfo getPrimaryKey(AbstractEntity entity) {
        Map<String, FiledInfo> fieldInfos = entity.getFieldInfos();
        for (String name : fieldInfos.keySet()) {
            var filedInfo = fieldInfos.get(name);
            if (filedInfo.isPrimaryKey()) {
                return filedInfo;
            }
        }
        return null;
    }

    @Autowired
    public void setJdbcClient(JdbcClient jdbcClient) {
        DbUtils.jdbcClient = jdbcClient;
    }

    record ConditionObject(String sql, List<Object> params) {
    }

}
