package alex.cache;

import alex.entity.GoodsEntity;
import alex.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GoodsCache {

    private final static Map<Long, GoodsEntity> rows = new ConcurrentHashMap<>();
    private static GoodsRepository goodsRepository;

    public static GoodsEntity getGoodsEntity(long goodsId) {
        return rows.get(goodsId);
    }

    public static List<GoodsEntity> getAllGoodsEntity() {
        List<GoodsEntity> list = new ArrayList<>();
        rows.forEach((id, goodsEntity) -> {
            list.add(goodsEntity);
        });
        return list;
    }


    @Autowired
    private void autowire(GoodsRepository goodsRepository) {
        GoodsCache.goodsRepository = goodsRepository;
    }

    @PostConstruct
    public static synchronized void init() {
        rows.clear();
        var list = goodsRepository.findAll();
        list.forEach(goodsEntity -> {
            rows.put(goodsEntity.getId(), goodsEntity);
        });
    }

    public static synchronized void updateGoods(long goodsId) {
        var goods = goodsRepository.findByIdForUpdate(goodsId);
        if (goods == null) {
            return;
        }
        rows.put(goodsId, goods);
    }


}
