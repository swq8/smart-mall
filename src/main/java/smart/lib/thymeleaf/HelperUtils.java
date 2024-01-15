package smart.lib.thymeleaf;

import smart.cache.ExpressCache;
import smart.cache.PaymentCache;
import smart.cache.RegionCache;
import smart.cache.SystemCache;
import smart.lib.Region;
import smart.lib.status.AccountStatus;
import smart.lib.status.GenderInfo;
import smart.lib.status.OrderGoodsStatus;
import smart.lib.status.OrderStatus;
import smart.util.Helper;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * thymeleaf utils
 * helper functions for thymeleaf
 * 自定义方言
 */
public class HelperUtils {
    public static String dateFormat(Date date) {
        return Helper.dateFormat(date);
    }

    public static String dateFormat(Date date, String pattern) {
        return Helper.dateFormat(date, pattern);
    }

    public static String dateFormat(LocalDateTime localDateTime) {
        return Helper.dateFormat(localDateTime);
    }

    public static String dateFormat(LocalDateTime localDateTime, String pattern) {
        return Helper.dateFormat(localDateTime, pattern);
    }

    /**
     * 获取账号状态信息
     *
     * @param code code
     * @return info
     */
    public static String getAccountStatusInfo(long code) {
        return AccountStatus.getStatusName(code);
    }

    /**
     * 获取快递公司名称
     *
     * @param id id
     * @return 快递公司名称
     */
    public static String getExpressNameById(long id) {
        return ExpressCache.getNameById(id);
    }

    /**
     * 获取支付名称
     *
     * @param name 支付方式英文名称
     * @return 中文名称
     */
    public static String getPayName(String name) {
        var payment = PaymentCache.getPaymentByName(name);
        if (payment == null) {
            return name;
        }
        return payment.getNameCn();
    }

    /**
     * 获取性别信息
     *
     * @param code code
     * @return info
     */
    public static String getGenderInfo(long code) {
        return GenderInfo.getGenderName(code);
    }

    /**
     * 获取订单商品状态信息
     *
     * @param code 订单商品状态码
     * @return 状态信息
     */
    public static String getOrderGoodsStatusInfo(long code) {
        return OrderGoodsStatus.getStatusName(code);
    }

    /**
     * 获取订单状态信息
     *
     * @param code 订单状态码
     * @return 状态信息
     */
    public static String getOrderStatusInfo(long code) {
        return OrderStatus.getStatusName(code);
    }

    public static Region getRegion(long code) {
        return RegionCache.getRegion(code);
    }

    /**
     * 正方形图片缩放
     *
     * @param url   图片地址
     * @param width 图片宽度
     * @return 缩放后的地址
     */
    public static String imgZoom(String url, long width) {
        //noinspection SuspiciousNameCombination
        return imgZoom(url, width, width);
    }

    /**
     * 图片缩放
     *
     * @param url    image url
     * @param width  zooming width
     * @param height zooming width
     * @return 缩放后的地址
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

    /**
     * 格式化价格
     *
     * @param price 价格(分)
     * @return 价格(元, 保留两位小数)
     */
    public static String priceFormat(Long price) {
        return price == null ? null : Helper.priceFormat(price);
    }

    /**
     * 根据系统配置生成静态文件(css/js)路径
     *
     * @param path css/js file path
     * @return css/js file url
     */
    public static String retouch(String path) {
        return SystemCache.getStaticRes().getPath() + path + "?v=" + SystemCache.getStaticRes().getVersion();
    }

}
