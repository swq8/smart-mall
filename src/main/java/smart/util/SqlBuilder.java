package smart.util;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SqlBuilder {
    private static final String[] AVAILABLE_SORTS = new String[]{"asc", "desc"};
    private static final Pattern WHERE_FIRST_WORD = Pattern.compile("^(\\s*\\w+)\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private final List<String> sortList = new ArrayList<>();
    private final List<Object> sqlParams;
    private final StringBuilder whereSql = new StringBuilder();
    private final StringBuilder sql;


    public SqlBuilder(String sql, List<Object> params) {
        this.sql = new StringBuilder(sql);
        this.sqlParams = params;
    }


    public SqlBuilder andIn(String name, Object... params) {
        return andInOrNotIn(name, true, params);
    }

    public SqlBuilder andNotIn(String name, Object... params) {
        return andInOrNotIn(name, false, params);
    }

    private SqlBuilder andInOrNotIn(String name, boolean isIn, Object... params) {
        String word = isIn ? "in" : "not in";

        whereSql.append(" and ")
                .append(getSqlName(name))
                .append(" ")
                .append(word)
                .append(" (");
        for (int i = 0; i < params.length; i++) {
            whereSql.append("?");
            if (i + 1 < params.length) {
                whereSql.append(",");
            }
            this.sqlParams.add(params[i]);
        }
        whereSql.append(")");
        return this;
    }

    public SqlBuilder andEquals(String name, Object param) {
        whereSql.append(" and ").append(getSqlName(name)).append(" = ?");
        sqlParams.add(param);
        return this;
    }

    public SqlBuilder andTrimEqualsIfNotBlank(String name, String param) {
        if (StringUtils.hasText(param)) {
            andEquals(name, param.trim());
        }
        return this;
    }

    public SqlBuilder andEqualsIfNotNull(String name, Object param) {
        if (param != null) {
            andEquals(name, param);
        }
        return this;
    }

    public SqlBuilder andLikeIfNotBlank(String name, String param) {
        if (StringUtils.hasText(param)) {
            param = param.trim()
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            whereSql.append(" and ").append(getSqlName(name)).append(" like ?");
            sqlParams.add("%" + param + "%");
        }
        return this;
    }

    private String getSqlName(String name) {
        return (name.contains(".")) ? name : "`" + name + "`";

    }

    public List<Object> getSqlParams() {
        return sqlParams;
    }

    public String buildSql() {
        StringBuilder result = new StringBuilder(sql);
        if (!whereSql.isEmpty()) {
            var matcher = WHERE_FIRST_WORD.matcher(whereSql.toString());
            if (matcher.find()) {
                String firstWord = matcher.group(1);
                result.append("\nwhere ").append(whereSql.substring(firstWord.length()));
            }
        }
        if (!sortList.isEmpty()) {
            result.append("\norder by ");
            for (int i = 0; i < sortList.size(); i++) {
                var sortParam = sortList.get(i);
                var arr = sortParam.split(",");
                result.append("`")
                        .append(DbUtils.camelCaseToUnderscoresNaming(arr[0]))
                        .append("`")
                        .append(" ")
                        .append(arr[1]);
                if (i + 1 < sortList.size()) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    public SqlBuilder orderBy(String sortParam) {
        sortList.add(sortParam);
        return this;
    }

    public SqlBuilder orderBy(String[] sortableColumns, String sortParam, String defaultSortParam) {
        if (sortParam == null) {
            sortParam = defaultSortParam;
        }
        var arr = sortParam.split(",");
        arr[0] = DbUtils.camelCaseToUnderscoresNaming(arr[0]);
        if (arr.length == 2
                && ObjectUtils.containsElement(sortableColumns, arr[0])
                && ObjectUtils.containsElement(AVAILABLE_SORTS, arr[1])) {
            sortList.add(sortParam);
        } else {
            sortList.add(defaultSortParam);
        }
        return this;
    }
}
