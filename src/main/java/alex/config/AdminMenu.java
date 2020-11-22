package alex.config;

import java.util.*;

/**
 * admin menu
 */
public class AdminMenu {

    public final Map<String, Set<Map<String, String>>> menu = new LinkedHashMap<>();

    public AdminMenu(Set<String> actions) {
        Map<String, String> items = new LinkedHashMap<>();
        items.put("商品-商品列表", "/admin/goods/goods/list");
        items.put("商品-商品分类", "/admin/goods/category/list");
        items.put("商品-商品品牌", "/admin/goods/brand/list");
        items.put("商品-商品规格", "/admin/goods/spec/list");
        items.put("订单-订单列表", "/admin/order/order/list");
        items.put("会员-会员列表", "/admin/user/user/list");
        items.put("系统-系统管理", "/admin/sys/manage");
        items.put("系统-管理员", "/admin/sys/adminUser/list");
        items.put("系统-角色", "/admin/sys/adminRole/list");
        items.put("系统-支付管理", "/admin/sys/payment/list");
        items.put("系统-快递公司", "/admin/sys/shipping/companies");
        items.put("系统-包邮规则", "/admin/sys/shipping/freeRule");
        items.put("系统-运费规则", "/admin/sys/shipping/priceRule");
        items.put("系统-系统信息", "/admin/sys/info");

        items.put("其它-静态文件", "/admin/other/static");
        items.put("其它-文章分类", "/admin/other/article/category");
        items.put("其它-文章管理", "/admin/other/article/list");
        items.put("其它-上传文件", "/admin/upload");
        Set<String> delNames = new HashSet<>();
        items.forEach((key, val) -> {
            if (!actions.contains("get@" + val)) {
                delNames.add(key);
            }
        });
        delNames.forEach(items::remove);

        items.forEach((key, val) -> {
            var arr = key.split("-");
            var set = menu.computeIfAbsent(arr[0], k -> new LinkedHashSet<>());
            Map<String, String> map = new HashMap<>();
            map.put("name", arr[1]);
            map.put("uri", val);
            set.add(map);
        });
    }

}

