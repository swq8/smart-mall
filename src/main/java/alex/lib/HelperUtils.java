package alex.lib;

import alex.Application;
import alex.cache.RegionCache;
import alex.cache.SystemCache;

/**
 * thymeleaf utils
 * helper functions for thymeleaf
 */
public class HelperUtils {

    public static Region getRegion(long code) {
        return RegionCache.getRegion(code);
    }

    /**
     *
     * @param url image url
     * @param width zooming width
     * @param height zooming width
     * @return
     */
    public static String imgZoom(String url, long width, long height) {
        if (url != null) {
            String url1 = url.toLowerCase();
            if (url.startsWith("http")) {
                /* for aliyun oss */
                url += "?x-oss-process=image/resize,w_" + width;
                if (height > 0) {
                    url += ",h_" + height;
                }
                return url;
            }
            /* for nginx */
            url += "?w=" + width;
            if (height > 0) {
                url += "&h=" + height;
            }
            return url;
        }
        return null;
    }

    public static String priceFormat(long l) {
        return Helper.priceFormat(l);
    }

    /**
     * retouch css/js file path
     * @param path css/js file path
     * @return
     */
    public static String retouch(String path) {
        return SystemCache.getJsPath() + path + "?v=" + SystemCache.getJsVersion();
    }

}
