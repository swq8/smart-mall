package smart.config;

import smart.entity.AdminRoleEntity;
import smart.repository.AdminRoleRepository;

import java.util.*;

public class AdminAuthority {
    public final Set<String> actions = new HashSet<>();
    public final Map<String, String[]> rules = new LinkedHashMap<>();

    /**
     * @param roleId 0: super administrator
     */
    public AdminAuthority(long roleId) {
        rules.put("商品-商品列表", new String[]{"get@/admin/goods/goods/list"});
        rules.put("商品-管理商品", new String[]{"post@/admin/goods/goods/delete",
                "get@/admin/goods/goods/edit", "post@/admin/goods/goods/edit"});
        rules.put("商品-商品分类列表", new String[]{"get@/admin/goods/category/list"});
        rules.put("商品-管理商品分类", new String[]{"post@/admin/goods/category/delete",
                "get@/admin/goods/category/edit", "post@/admin/goods/category/edit"});
        rules.put("商品-商品品牌列表", new String[]{"get@/admin/goods/brand/list"});
        rules.put("商品-管理商品品牌", new String[]{"post@/admin/goods/brand/delete",
                "get@/admin/goods/brand/edit", "post@/admin/goods/brand/edit"});
        rules.put("商品-商品规格列表", new String[]{"get@/admin/goods/spec/list"});
        rules.put("商品-管理商品规格", new String[]{"post@/admin/goods/spec/delete",
                "get@/admin/goods/spec/edit", "post@/admin/goods/spec/edit"});
        rules.put("订单-订单列表", new String[]{"get@/admin/order/order/list"});
        rules.put("订单-编辑订单", new String[]{"get@/admin/order/order/edit"});
        rules.put("订单-支付订单", new String[]{"post@/admin/order/order/pay"});
        rules.put("订单-发货/修改物流信息", new String[]{"post@/admin/order/order/ship"});
        rules.put("订单-取消订单", new String[]{"post@/admin/order/order/cancel"});
        rules.put("订单-退款", new String[]{"post@/admin/order/order/refund"});
        rules.put("订单-确认收货", new String[]{"post@/admin/order/order/confirm"});
        rules.put("会员-会员列表", new String[]{"get@/admin/user/user/list"});
        rules.put("会员-编辑会员", new String[]{"get@/admin/user/user/edit", "post@/admin/user/user/edit"});
        rules.put("会员-删除会员", new String[]{"post@/admin/user/user/delete"});
        rules.put("系统-系统管理", new String[]{"get@/admin/sys/manage", "post@/admin/sys/manage"});
        rules.put("系统-管理员列表", new String[]{"get@/admin/sys/adminUser/list"});
        rules.put("系统-管理管理员", new String[]{
                "get@/admin/sys/adminUser/edit", "post@/admin/sys/adminUser/edit", "post@/admin/sys/adminUser/delete"});
        rules.put("系统-角色列表", new String[]{"get@/admin/sys/adminRole/list"});
        rules.put("系统-管理角色", new String[]{
                "get@/admin/sys/adminRole/edit", "post@/admin/sys/adminRole/edit", "post@/admin/sys/adminRole/delete"});
        rules.put("系统-支付管理", new String[]{"get@/admin/sys/payment/list", "get@/admin/sys/payment/edit",
                "post@/admin/sys/payment/edit"});
        rules.put("系统-查看运费配置", new String[]{"get@/admin/sys/shipping/companies", "get@/admin/sys/shipping/priceRule",
                "get@/admin/sys/shipping/freeRule"});
        rules.put("系统-管理运费配置", new String[]{"get@/admin/sys/shipping/companies", "get@/admin/sys/shipping/priceRule",
                "get@/admin/sys/shipping/freeRule", "get@/admin/sys/shipping/company", "post@/admin/sys/shipping/company",
                "post@/admin/sys/shipping/delete", "post@/admin/sys/shipping/priceRule", "post@/admin/sys/shipping/freeRule"});
        rules.put("系统-系统信息", new String[]{"get@/admin/sys/info"});
        rules.put("其它-首页轮播", new String[]{"get@/admin/other/home/carousel", "post@/admin/other/home/carousel"});
        rules.put("其它-静态文件", new String[]{"get@/admin/other/static", "post@/admin/other/static"});
        rules.put("其它-文章分类", new String[]{"post@/admin/article/category/delete", "get@/admin/article/category/list",
                "get@/admin/article/category/edit", "post@/admin/article/category/edit"});
        rules.put("其它-文章管理", new String[]{"post@/admin/article/article/delete", "get@/admin/article/article/edit",
                "post@/admin/article/article/edit","get@/admin/article/article/list"});
        rules.put("其它-上传文件", new String[]{"get@/admin/upload", "post@/admin/upload"});

        if (roleId != 0) {
            AdminRoleRepository adminRoleRepository = AppConfig.getContext().getBean(AdminRoleRepository.class);
            AdminRoleEntity adminRoleEntity = adminRoleRepository.findById(roleId).orElse(null);
            if (adminRoleEntity == null) {
                rules.clear();
            } else {
                String[] ruleNames = adminRoleEntity.getAuthorize().split(",");
                Arrays.sort(ruleNames);
                String[] allNames = rules.keySet().toArray(String[]::new);
                for (String name : allNames) {
                    if (Arrays.binarySearch(ruleNames, name) < 0) {
                        rules.remove(name);
                    }
                }
            }
        }
        rules.values().forEach(arr -> actions.addAll(Arrays.asList(arr)));
    }


}
