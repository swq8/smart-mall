package alex.lib;

import alex.Application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pagination
 */
public class Pagination {
    private static final Pattern patternSelect = Pattern.compile("^(\\s*select.+)\\sfrom\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern patternOrderBy = Pattern.compile("(\\sorder\\s+by\\s+.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private long currentPage = 1;
    private final long pageSize;
    private final Object[] params;
    private List<Map<String, Object>> rows;
    private String sql;
    private long pageCount = 10;
    private final List<Map<String, Object>> pages = new LinkedList<>();
    /**
     * http get method query
     */
    private String httpQuery = "?";
    private long startIndex;
    private long endIndex;
    private long totalPages = 0;
    private long totalRecords = 0;

    public Pagination(String sql, long page) {
        this(sql, null, 20, page, null);
    }

    public Pagination(String sql, long page, Map<String, String> query) {
        this(sql, null, 20, page, query);
    }

    public Pagination(String sql, Object[] params, long page, Map<String, String> query) {
        this(sql, params, 20, page, query);
    }

    public Pagination(String sql, Object[] params, long pageSize, long page, Map<String, String> query) {
        this.sql = sql;
        this.params = params;
        this.pageSize = pageSize;
        if (query != null) {
            query.forEach((k, v) -> {
                httpQuery += String.format("%s=%s&",
                        URLEncoder.encode(k, StandardCharsets.UTF_8), URLEncoder.encode(v, StandardCharsets.UTF_8));
            });
        }
        initTotalRecords();
        totalPages = totalRecords / pageSize;
        if (totalRecords % pageSize > 0) {
            totalPages++;
        }
        currentPage = page > 0 ? page : 1;
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
        for (long l = pageOffset; l < pageOffset + pageCount && l < totalPages; ) {
            l++;
            Map<String, Object> map = new HashMap<>();
            map.put("num", l);
            pages.add(map);
        }
        initRows();
    }

    public static String getCountSql(String sql) {
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
    public List<Map<String, Object>> getRows() {
        return rows;
    }

    private void initRows() {
        startIndex = (currentPage - 1) * pageSize;
        if (startIndex < 0) {
            startIndex = 0;
        }
        String sql1 = String.format("%s limit %d,%d", sql, startIndex, pageSize);
        rows = Application.JDBC_TEMPLATE.queryForList(sql1, params);
        endIndex = startIndex + rows.size();
    }

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


    public String generateBootstrapPagination() {
        StringBuilder html = new StringBuilder("<ul class=\"pagination justify-content-end\">\n");
        html.append(String.format("<li class=\"page-item%s\">" +
                        "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=1\">&lt;&lt;&lt;</a></li>\n",
                currentPage == 1 ? " disabled" : "", httpQuery));
        for (Map<String, Object> page : pages) {
            long curPageNum = (long) page.get("num");
            html.append(String.format(" <li class=\"page-item%s\"><a class=\"page-link\" href=\"%spage=%d\">%d</a></li>\n",
                    currentPage == curPageNum ? " active" : "", httpQuery, curPageNum, curPageNum));
        }
        html.append(String.format("<li class=\"page-item%s\">" +
                        "<a class=\"page-link\" aria-disabled=\"true\" href=\"%spage=%d\">&gt;&gt;&gt;</a></li>\n",
                currentPage == totalPages ? " disabled" : "", httpQuery, totalPages));
        html.append("</ul>\n");
        return html.toString();
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public String getHttpQuery() {
        return httpQuery;
    }

    public List<Map<String, Object>> getPages() {
        return pages;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    private void initTotalRecords() {
        String countSql = getCountSql(sql);
        if (countSql == null) {
            Exception ex = new Exception("sql statement error: " + sql);
            ex.printStackTrace();
        } else {
            Long count = Application.JDBC_TEMPLATE.queryForObject(countSql, params, Long.class);
            totalRecords = count == null ? 0 : count;
        }
    }
}
