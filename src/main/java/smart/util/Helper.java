package smart.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.lib.JsonResult;
import smart.lib.session.Session;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * helper class
 */
public final class Helper {

    // default date formatter
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 移动端判别条件
    private final static String[] MOBILE_USER_AGENT_KEY_WORDS = {" (iPhone; CPU ", " (iPad; CPU ", " Android "};

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (Byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 格式化日期 'yyyy-MM-dd HH:mm:ss'
     *
     * @param localDateTime date
     * @return str
     */
    public static String dateFormat(LocalDateTime localDateTime) {

        return localDateTime == null ? null : dateTimeFormatter.format(localDateTime);
    }

    public static String dateFormat(LocalDateTime localDateTime, String pattern) {
        return localDateTime == null || pattern == null ? null : DateTimeFormatter.ofPattern(pattern).format(localDateTime);
    }

    /**
     * 格式化日期 'yyyy-MM-dd HH:mm:ss'
     *
     * @param date 要格式化的日期
     * @return 格式化后的日期
     */
    public static String dateFormat(Date date) {

        return date == null ? null : simpleDateFormat.format(date);
    }

    public static String dateFormat(Date date, String pattern) {

        return date == null || pattern == null ? null : new SimpleDateFormat(pattern).format(date);
    }

    /**
     * get client ip from http request
     *
     * @param request client request
     * @return client ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor == null || xForwardedFor.length() < 6) return request.getRemoteAddr();
        return xForwardedFor.split(",")[0].trim();
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    /**
     * 获取错误页面html
     *
     * @param httpStatus  http status
     * @param errorDetail error detail, only show in dev mod
     * @param themeName   theme name
     * @return error page html
     */
    public static String getErrorHtml(HttpStatus httpStatus, String errorDetail, String themeName) {
        TemplateEngine templateEngine = AppConfig.getContext().getBean(TemplateEngine.class);
        Context context = new Context();
        context.setVariable("title", httpStatus.value() + " " + httpStatus.getReasonPhrase());

        context.setVariable("detail", errorDetail);
        return templateEngine.process(themeName + "/error", context);
    }


    /**
     * 生成二维码
     *
     * @param text  需要生成二维码的内容
     * @param width 二维码宽高度
     * @return 二维码字节
     */
    public static byte[] getQRCodePng(String text, int width) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            //noinspection SuspiciousNameCombination
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, width);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception ex) {
            LogUtils.error(Helper.class, "", ex);
            return null;
        }
    }

    /**
     * return size str, like this: 1024 -> 1KB
     *
     * @param size size
     * @return size str
     */
    public static String getSizes(long size) {
        String numStr, unit;
        BigDecimal num = new BigDecimal(size);
        if (size < 1024) return num.toString();
        if (size < 0x10_0000) {
            numStr = num.divide(new BigDecimal(1024), 2, RoundingMode.HALF_UP).toString();
            unit = "K";
        } else if (size < 0x400_00000) {
            numStr = num.divide(new BigDecimal(0x100000), 2, RoundingMode.HALF_UP).toString();
            unit = "M";
        } else if (size < 0x100_0000_0000L) {
            numStr = num.divide(new BigDecimal(0x400_00000), 2, RoundingMode.HALF_UP).toString();
            unit = "G";
        } else {
            numStr = num.divide(new BigDecimal(0x100_0000_0000L), 2, RoundingMode.HALF_UP).toString();
            unit = "P";
        }
        if (numStr.endsWith("00")) numStr = numStr.substring(0, numStr.length() - 3);
        else if (numStr.endsWith("0")) numStr = numStr.substring(0, numStr.length() - 1);
        return numStr + unit + "B";

    }

    /**
     * get theme name
     *
     * @param request http request
     * @return relatively theme name
     */
    public static String getThemeName(HttpServletRequest request) {
        return isMobileRequest(request) ? SystemCache.getThemeMobile() : SystemCache.getThemePc();
    }


    /***
     * is it mobile request
     * @param request http request
     * @return mobile request
     */
    public static boolean isMobileRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.hasLength(userAgent)) {
            for (var keyWord : MOBILE_USER_AGENT_KEY_WORDS) {
                if (userAgent.contains(keyWord)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 使用json result机制通知客户端跳转消息页面查看消息
     *
     * @param jsonResult 要展示的信息
     * @param request    http request
     * @return json
     */
    public static String msgPage(JsonResult jsonResult, HttpServletRequest request) {
        Session session = Session.from(request);
        session.set("msg", jsonResult.getMsg());
        jsonResult.setMsg(null);
        String url = jsonResult.getUrl();
        if (url != null) {
            if (!url.startsWith("/")) {
                String uri = request.getRequestURI();
                url = uri.substring(0, uri.lastIndexOf("/") + 1) + url;
            }
            session.set("backUrl", url);
        }
        jsonResult.setUrl("/msg");
        return jsonResult.toString();
    }

    /**
     * 渲染消息页面
     *
     * @param msg     要展示的消息
     * @param request http request
     * @return 消息页面
     */
    public static ModelAndView msgPage(String msg, HttpServletRequest request) {
        return msgPage(msg, null, request);
    }

    /**
     * 渲染消息页面
     *
     * @param msg     要展示的消息
     * @param backUrl 返回url
     * @param request http request
     * @return 消息页面
     */
    public static ModelAndView msgPage(String msg, String backUrl, HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("msg", request);
        modelAndView.addObject("msg", msg);
        modelAndView.addObject("backUrl", backUrl);
        modelAndView.addObject("title", "信息");
        return modelAndView;
    }

    /**
     * create new model and view
     *
     * @param viewName view name
     * @param request  http quest
     * @return mode and view
     */
    public static ModelAndView newModelAndView(String viewName, HttpServletRequest request) {
        ModelAndView modelAndView;
        if (viewName.startsWith("admin/")) {
            modelAndView = new ModelAndView(viewName);
            modelAndView.addObject("theme", "admin");
        } else {
            String theme = getThemeName(request);
            modelAndView = new ModelAndView(theme + '/' + viewName);
            modelAndView.addObject("theme", theme);
        }
        return modelAndView;
    }

    /**
     * 返回当前时间, yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间
     */
    public static String now() {
        return dateFormat(new Date());
    }

    /**
     * 从字符串中解析日期，需要符合"yyyy-MM-dd HH:mm:ss"格式
     *
     * @param str 待解析的字符串
     * @return 日期，失败返回null
     */
    public static Date parseDate(String str) {
        return parseDate(str, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 从字符串中解析日期
     *
     * @param str     待解析的字符串
     * @param pattern 日期格式
     * @return 日期，失败返回null
     */
    public static Date parseDate(String str, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(str);
        } catch (ParseException ignored) {
            return null;
        }
    }

    public static <T extends Number> T parseNumber(Object obj, Class<T> targetClass) {
        return parseNumber(obj, targetClass, "0");
    }

    public static <T extends Number> T parseNumber(Object obj, Class<T> targetClass, String defaultValue) {
        if (obj == null) obj = defaultValue;
        T result;
        try {
            result = NumberUtils.parseNumber(obj.toString(), targetClass);
        } catch (Exception ignored) {
            result = NumberUtils.parseNumber(defaultValue, targetClass);
        }
        return result;
    }

    /**
     * 格式化价格
     *
     * @param bigDecimal 价格(元)
     * @return 价格(元, 保留两位小数)
     */
    public static String priceFormat(BigDecimal bigDecimal) {
        return priceFormat(bigDecimal.multiply(new BigDecimal(100)).longValue());
    }

    /**
     * 格式化价格
     *
     * @param price 价格(分)
     * @return 价格(元, 保留两位小数)
     */
    public static String priceFormat(long price) {
        String prefix = price < 0 ? "-" : "";
        price = Math.abs(price);
        return String.format("%s%d.%02d", prefix, price / 100, price % 100);
    }

    /**
     * 价格由小数点的元单位变为长整型的分单位
     *
     * @param str 要转换的价格(元)
     * @return 价格(分 ）
     */
    public static long priceToLong(String str) {
        BigDecimal bigDecimal = new BigDecimal(str);
        bigDecimal = bigDecimal.multiply(new BigDecimal(100)).setScale(0, RoundingMode.UNNECESSARY);
        return Long.parseLong(bigDecimal.toString());
    }

    /**
     * random string
     *
     * @param len length
     * @return random string
     */
    public static String randomString(int len) {
        if (len <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) (ThreadLocalRandom.current().nextInt(26) + 'a'));
        }
        return sb.toString();
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            LogUtils.error(Helper.class, "", ex);
        }
    }

    /***
     * replaceAll,忽略大小写
     *
     * @param input 输入字符串
     * @param regex 待替换的内容(正则)
     * @param replacement 要替换的字符串
     * @return 替换后的字符串
     */
    public static String stringReplaceAll(String input, String regex, String replacement) {
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        return m.replaceAll(replacement);

    }

    /**
     * url编码
     *
     * @param str 要编码的字符串
     * @return url编码后的字符串
     */
    public static String urlDecode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    /**
     * url解码
     *
     * @param str 要解码的字符串
     * @return url解码后的字符串
     */
    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

}
