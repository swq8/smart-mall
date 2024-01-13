package smart.cache;


import smart.entity.BrandEntity;
import smart.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BrandCache {
    private static BrandService BrandService;
    private static Map<Long, BrandEntity> rows;

    @PostConstruct
    public synchronized static void update() {
        Map<Long, BrandEntity> map = new ConcurrentHashMap<>();
        BrandService.findAll().forEach(brandEntity -> map.put(brandEntity.getId(), brandEntity));
        rows = map;
    }

    public static Map<Long, BrandEntity> getRows() {
        return rows;
    }

    @Autowired
    private void autowire(BrandService service) {
        BrandService = service;
    }


}
