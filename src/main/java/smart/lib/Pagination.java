package smart.lib;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import smart.config.AppConfig;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.LogUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分页类
 */
public final class Pagination {
    private static final Pattern patternSelect = Pattern.compile("^(\\s*select.+)\\sfrom\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern patternOrderBy = Pattern.compile("(\\sorder\\s+by\\s+.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private final long pageSize;
    private final Object[] params;
    private final String sql;
    private final List<Map<String, Object>> pages = new LinkedList<>();
    private long currentPage;
    private List<Map<String, Object>> rows;
    /**
     * http get method query
     */
    private String httpQuery = "?";
    private long totalPages;
    private long totalRecords = 0;

    /**
     * 初始化分页类
     *
     * @param builder builder
     */
    private Pagination(Builder builder) {
        this.sql = builder.sql;
        this.params = builder.sqlParams;
        this.pageSize = builder.pageSize;
        if (builder.query != null) {
            builder.query.forEach((k, v) -> httpQuery += String.format("%s=%s&",
                    URLEncoder.encode(k, StandardCharsets.UTF_8), URLEncoder.encode(v, StandardCharsets.UTF_8)));
        }
        initTotalRecords();
        totalPages = totalRecords / pageSize;
        if (totalRecords % pageSize > 0) {
            totalPages++;
        }
        currentPage = builder.page > 0 ? builder.page : 1;
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        long pageOffset = currentPage - 6;
        if (totalPages - pageOffset < 10) {
            pageOffset = totalPages - 10;
        }
        if (pageOffset < 0) {
            pageOffset = 0L;
        }
        // 显示页数范围10页
        for (long l = pageOffset; l < pageOffset + 10 && l < totalPages; ) {
            l++;
            Map<String, Object> map = new HashMap<>();
            map.put("num", l);
            pages.add(map);
        }
        initRows();
    }

    /**
     * 获取记录条数
     *
     * @param sql select语句
     * @return 记录数
     */
    private static String getCountSql(String sql) {
        Matcher matcher = patternSelect.matcher(sql);
        if (matcher.find()) {
            String g = matcher.group(1);
            sql = sql.substring(g.length());
            var matcher1 = patternOrderBy.matcher(sql);
            if (matcher1.find()) {
                String orderBy = matcher1.group(1);
                sql = sql.substring(0, sql.length() - orderBy.length());
            }
            return "select count(*)" + sql;

        } else {
            return null;
        }
    }

    public static Builder newBuilder(String sql) {
        return newBuilder(sql, null);
    }

    /**
     * new builder
     *
     * @param sql       查询sql
     * @param sqlParams 参数
     * @return builder
     */
    public static Builder newBuilder(String sql, Object[] sqlParams) {
        return new Builder(sql, sqlParams);
    }

    /**
     * 获取当前页的记录
     *
     * @return 当前页记录
     */
    public List<Map<String, Object>> getRows() {
        return rows;
    }

    /**
     * 初始化当前页记录
     */
    private void initRows() {
        rows = new ArrayList<>();
        if (currentPage < 1) {
            return;
        }
        var startIndex = (currentPage - 1) * pageSize;
        String sql1 = String.format("%s limit %d,%d", sql, startIndex, pageSize);
        AppConfig.getJdbcClient().sql(sql1).params(params).query().listOfRows().forEach(row -> {
            Map<String, Object> row1 = new LinkedHashMap<>();
            row.keySet().forEach(key -> row1.put(DbUtils.underscoresToCamelCaseNaming(key), row.get(key)));
            rows.add(row1);
        });
    }

    /**
     * 生成网页分页页脚
     *
     * @return html
     */
    public String generateWebPagination() {
        StringBuilder html = new StringBuilder("<ul class=\"pagination\">\n");
        if (currentPage == 1) {
            html.append("<li class=\"page-item disabled\">" +
                    "<a class=\"page-link\" aria-disabled=\"true\">首页</a></li>\n");
        } else {
            html.append(String.format("<li class=\"page-item\">" +
                    "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=1\">首页</a></li>\n", httpQuery));
        }
        long curPageNum;
        for (Map<String, Object> page : pages) {
            curPageNum = (long) page.get("num");
            if (currentPage == curPageNum) {
                html.append(String.format(" <li class=\"page-item active\"><a class=\"page-link\">%d</a></li>\n",
                        curPageNum));
            } else {
                html.append(String.format(" <li class=\"page-item\"><a class=\"page-link\" href=\"%spage=%d\">%d</a></li>\n",
                        httpQuery, curPageNum, curPageNum));
            }
        }
        if (currentPage == totalPages) {
            html.append("<li class=\"page-item disabled\">" +
                    "<a class=\"page-link\" aria-disabled=\"true\">尾页</a></li>\n");
        } else {
            html.append(String.format("<li class=\"page-item\">" +
                            "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=%d\">尾页</a></li>\n",
                    httpQuery, totalPages));
        }
        html.append(String.format("<li class=\"page-item\"><a class=\"page-link\">当前第%d页,共%d页</a></li>\n", currentPage, totalPages));
        html.append("</ul>\n");
        return html.toString();
    }


    /**
     * 获取当前页
     *
     * @return 当前页
     */
    public long getCurrentPage() {
        return currentPage;
    }

    /**
     * http query
     *
     * @return http query
     */
    public String getHttpQuery() {
        return httpQuery;
    }


    /**
     * 页脚页列表
     *
     * @return 页脚页列表
     */
    public List<Map<String, Object>> getPages() {
        return pages;
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public long getTotalPages() {
        return totalPages;
    }

    /**
     * 获取总记录数
     *
     * @return 总记录数
     */
    public long getTotalRecords() {
        return totalRecords;
    }

    /**
     * 初始化总记录数
     */
    private void initTotalRecords() {
        String countSql = getCountSql(sql);
        if (countSql == null) {
            LogUtils.error(this.getClass(), new Exception("sql statement error: " + sql));
        } else {
            totalRecords = AppConfig.getJdbcClient().sql(countSql).params(params).query(Long.class).single();
        }
    }

    public static final class Builder {
        private long pageSize = 20;
        private long page = 1;
        private Map<String, String> query = null;
        private String sql = null;
        private Object[] sqlParams = null;

        /**
         * @param sql       sql
         * @param sqlParams 参数
         */
        public Builder(String sql, @Nullable Object[] sqlParams) {
            this.sql = sql;
            this.sqlParams = sqlParams == null ? new Object[0] : sqlParams;

        }


        public Pagination build() {
            return new Pagination(this);
        }

        /**
         * 设置当前页
         *
         * @param page 当前页
         * @return this
         */
        public Builder page(Long page) {
            if (page != null) {
                this.page = page;
            }
            return this;
        }

        /**
         * 从http request 中读取当前页,参数名 page
         *
         * @param request http request
         * @return this
         */
        public Builder page(HttpServletRequest request) {
            page(request, "page");
            return this;
        }

        /**
         * 从http request 中读取当前页
         *
         * @param request http request
         * @param name    request name
         * @return this
         */
        public Builder page(HttpServletRequest request, String name) {
            page(Helper.parseNumber(request.getParameter(name), Long.class));
            return this;
        }

        /**
         * 设置每页记录数
         *
         * @param pageSize 每页记录数
         * @return this
         */
        public Builder pageSize(Long pageSize) {
            if (pageSize != null) {
                this.pageSize = pageSize;
            }
            return this;
        }

        /**
         * http 查询参数，在生成html页脚时使用
         *
         * @param query http query
         * @return this
         */
        public Builder query(Map<String, String> query) {
            this.query = query;
            return this;
        }

    }
}
