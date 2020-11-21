package alex.lib;

import alex.Application;
import alex.cache.CategoryCache;
import alex.cache.SystemCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * helper class
 */
public class Helper {
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (Byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * date format 'yyyy-MM-dd HH:mmm:ss'
     *
     * @param date
     * @return
     */
    public static String dateFormat(Date date) {
        return dateFormat(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String dateFormat(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    /**
     * flush session
     *
     * @param session http session
     */
    public static void flushSession(HttpSession session) {
        if (session != null) {
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String key = attributeNames.nextElement();
                session.removeAttribute(key);
            }
        }
    }

    /**
     * get client ip from http request
     *
     * @param request client request
     * @return client ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("x_forwarded_for");
        if (xForwardedFor == null || xForwardedFor.length() < 6) {
            return request.getRemoteAddr();
        }
        String[] array = xForwardedFor.split(",");
        return array[0].trim();
    }

    public static String getJson(Object value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get theme name
     *
     * @param request http request
     * @return relatively theme name
     */
    public static String getTheme(HttpServletRequest request) {
        return isMobileRequest(request) ? SystemCache.getThemeMobile() : SystemCache.getThemePc();
    }

    /**
     * get long value from object
     *
     * @param o
     * @return int
     */
    public static int intValue(Object o) {
        return intValue(o, 0);
    }

    public static int intValue(Object o, int defaultValue) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof BigDecimal) {
            return ((BigDecimal) o).intValue();
        } else if (o instanceof BigInteger) {
            return ((BigInteger) o).intValue();
        } else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /***
     * is it mobile request
     * @param request http request
     * @return mobile request
     */
    public static boolean isMobileRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        String[] mobileParam = {
                " (iPhone; CPU ",
                " (iPad; CPU ",
                " Android "
        };
        if (userAgent != null) {
            for (String s : mobileParam) {
                if (userAgent.contains(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get long value from object
     *
     * @param o
     * @return long
     */
    public static long longValue(Object o) {
        return longValue(o, 0L);
    }

    public static long longValue(Object o, long defaultValue) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof BigDecimal) {
            return ((BigDecimal) o).longValue();
        } else if (o instanceof BigInteger) {
            return ((BigInteger) o).longValue();
        } else if (o instanceof String) {
            try {
                return Long.parseLong((String) o);
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
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
            modelAndView.addObject("adminMenu", request.getAttribute("adminMenu"));
        } else {
            String theme = getTheme(request);
            modelAndView = new ModelAndView(theme + '/' + viewName);
            modelAndView.addObject("theme", theme);
        }

        return modelAndView;
    }

    public static String priceFormat(BigDecimal bigDecimal) {
        return priceFormat(bigDecimal.multiply(new BigDecimal(100)).longValue());
    }
    public static String priceFormat(long l) {
        String prefix = l < 0 ? "-" : "";
        l = Math.abs(l);
        return String.format("%s%d.%02d", prefix, l / 100, l % 100);
    }

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
        Random r = new Random();
        for (int i = 0; i < len; i++) {
            sb.append((char) (r.nextInt(26) + 'a'));
        }
        return sb.toString();
    }

    /**
     * remove specify strings
     * @param str
     * @param args
     * @return
     */
    public static String stringRemove(String str, String... args) {
        if (str == null) {
            return null;
        }
        for (String s : args) {
            str = str.replace(s, "");
        }
        return str;
    }
    public static String stringValue(Object obj) {
       if (obj == null) {
           return null;
       }
        return obj.toString();
    }

    /**
     * url decode
     *
     * @param str
     * @return
     */
    public static String urlDecode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    /**
     * url encode
     *
     * @param str
     * @return
     */
    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

}
