package alex.cache;

import alex.entity.GoodsEntity;
import alex.entity.GoodsSpecEntity;
import alex.repository.GoodsSpecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@DependsOn("goodsCache")
public class GoodsSpecCache {
    private final static Map<Long, List<GoodsSpecEntity>> rows = new ConcurrentHashMap<>();
    private static GoodsSpecRepository goodsSpecRepository;

    @Autowired
    private void autowire(GoodsSpecRepository goodsSpecRepository) {
        GoodsSpecCache.goodsSpecRepository = goodsSpecRepository;
    }

    @PostConstruct
    public static synchronized void init() {
        rows.clear();
        var list = GoodsCache.getAllGoodsEntity();
        list.forEach(GoodsSpecCache::updateByGoods);

    }

    public static List<GoodsSpecEntity> getGoodsSpecEntities(long goodsId) {
        return rows.get(goodsId);
    }

    public static void updateByGoods(GoodsEntity goodsEntity) {

        if (goodsEntity == null || goodsEntity.getId() <= 0 ||goodsEntity.getSpec().length() < 10) {
            assert goodsEntity != null;
            rows.remove(goodsEntity.getId());
            return;
        }
        var list = goodsSpecRepository.findAllByGoodsIdForUpdate(goodsEntity.getId());
        if (list.size() > 0) {
            rows.put(goodsEntity.getId(), list);
        }
    }

    public static void updateByGoods(long goodsId) {
        if (goodsId <= 0 ) {
            return;
        }
        GoodsEntity goodsEntity = GoodsCache.getGoodsEntity(goodsId);
        if (goodsEntity == null || goodsEntity.getSpec().length() < 10) {
            rows.remove(goodsId);
            return;
        }
        var list = goodsSpecRepository.findAllByGoodsIdForUpdate(goodsId);
        if (list.size() > 0) {
            rows.put(goodsId, list);
        }
    }


}
