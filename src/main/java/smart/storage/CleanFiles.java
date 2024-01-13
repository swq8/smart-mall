package smart.storage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import smart.config.AppConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 清除无用的文件,使用前请做好备份
 * 请谨慎使用
 * <p> 使用方法</p>
 * {@code new CleanFiles().cleanFiles();}
 */
public class CleanFiles {
    private final Log log = LogFactory.getLog(this.getClass());
    private final Set<String> files = new HashSet<>();

    public CleanFiles() {
        // 商品表引用到的静态文件
        AppConfig.getJdbcClient().sql("select des,imgs,spec from t_goods").query().listOfRows().forEach(row -> {
            getUrlFromHtml((String) row.get("des")).forEach(url -> files.add(getPath(url)));
            getUrlFromJson((String) row.get("spec")).forEach(url -> files.add(getPath(url)));
            String[] imgs = ((String) row.get("imgs")).split(",");
            for (var img : imgs) {
                files.add(getPath(img));
            }
        });
        // 订单商品表
        AppConfig.getJdbcClient().sql("select img from t_order_goods").query().listOfRows().forEach(row -> {
            files.add(getPath((String) row.get("img")));
        });
        // 商品规格
        AppConfig.getJdbcClient().sql("select list from t_spec").query().listOfRows().forEach(row -> {
            getUrlFromJson((String) row.get("list")).forEach(url -> files.add(getPath(url)));
        });
        // 首页幻灯片
        String json = AppConfig.getJdbcClient()
                .sql("select value from t_system where entity='sys' and attribute='carousel'")
                .query(String.class).optional().orElse("[]");
        getUrlFromJson(json).forEach(url -> files.add(getPath(url)));

        files.remove(null);
    }

    boolean canClear(String file) {
        if (file.startsWith("static/")) {
            return false;
        }
        return !files.contains(file);
    }


    /**
     * 调用此方法清除没有引用到的文件
     */
    public void cleanFiles() {
        Storage storage = AppConfig.getContext().getBean(Storage.class);
        storage.cleanFiles(this::canClear);
    }

    String getPath(String url) {
        if (url == null || url.length() < 2) {
            return null;
        }

        if (url.charAt(0) == '/') {
            return url.substring(1);
        }
        Pattern pattern = Pattern.compile("https?://[.\\-0-9a-z:]+/(?<path>[^?\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group("path");
        }
        log.warn("未知文件名:" + url);
        return null;

    }

    /**
     * 从HTML中获取引用文件的URL
     *
     * @param html HTML
     * @return urls
     */
    Set<String> getUrlFromHtml(String html) {
        Set<String> urls = new HashSet<>();
        if (html == null) {
            return urls;
        }
        //(src|href)=("|')([.\-0-9a-z:/]+)
        Pattern pattern = Pattern.compile("(href|src)=['\"](?<url>[.\\-0-9a-z:/]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            urls.add(matcher.group("url"));
        }
        return urls;
    }

    /**
     * 从json中获取引用文件的URL
     *
     * @param json json
     * @return urls
     */
    Set<String> getUrlFromJson(String json) {
        Set<String> urls = new HashSet<>();
        if (json == null) {
            return urls;
        }
        Pattern pattern = Pattern.compile("\"https?://[^\"]+\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            var str = matcher.group();
            urls.add(str.substring(1, str.length() - 1));
        }
        return urls;
    }
}
