package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import smart.config.AppConfig;
import smart.util.Helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@DependsOn("categoryCache")
public class GoodsCache {
    // 推荐商品(按类别分项),long:类别ID, Map<String, Object>
    private static List<Map<String, Object>> recommend;

    @PostConstruct
    public static synchronized void update() {
        updateRecommend();
    }

    /**
     * 获取推荐商品
     *
     * @return 推荐商品
     */
    public static List<Map<String, Object>> getRecommend() {
        return recommend;
    }

    /**
     * 获取指定类别下的推荐商品
     *
     * @param catId 类别id
     * @return 推荐商品
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getRecommendGoodsList(long catId) {
        for (var map : recommend) {
            if (catId == Helper.parseNumber(map.get("cateId"), Long.class)) {
                return (List<Map<String, Object>>) map.get("goodsList");
            }
        }
        return new LinkedList<>();
    }

    /**
     * 更新推荐商品
     */
    public synchronized static void updateRecommend() {
        List<Map<String, Object>> recommend1 = new LinkedList<>();
        AppConfig.getJdbcClient()
                .sql("select id,name from t_category where parent_id = 0 order by order_num")
                .query().listOfRows()
                .forEach(m -> {
                    Map<String, Object> map = new HashMap<>();
                    long cateId = Helper.parseNumber(m.get("id"), Long.class);

                    // 子类别
                    StringBuilder catIds = new StringBuilder(Long.toString(cateId));
                    for (var v : CategoryCache.getChildren(cateId)) {
                        catIds.append(",").append(v.getId());
                    }

                    var sql = String.format("select id,imgs->>'$[0]' as img,name,price from t_goods where cate_id in (%s) and on_sell = true order by order_num, update_time desc limit 16", catIds);
                    var goodsList = AppConfig.getJdbcClient().sql(sql).query().listOfRows();
                    sql = "select id, name from t_category where parent_id = ? order by order_num limit 16";
                    var cateList = AppConfig.getJdbcClient().sql(sql).param(cateId).query().listOfRows();
                    map.put("cateId", cateId);
                    map.put("name", m.get("name"));
                    map.put("cateList", cateList);
                    map.put("goodsList", goodsList);
                    recommend1.add(map);
                });
        recommend = recommend1;
    }

}
