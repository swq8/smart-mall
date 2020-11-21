package alex.cache;


import alex.entity.BrandEntity;
import alex.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BrandCache {
    private static BrandService BrandService;
    private static List<BrandEntity> rows;

    @Autowired
    private void autowire(BrandService service) {
        BrandService = service;
    }

    @PostConstruct
    public synchronized static void init() {
        rows = BrandService.findAll();
    }


    public static BrandEntity getEntityById(long id) {
        return rows.stream().filter(row -> row.getId() == id).findFirst().orElse(null);
    }

    public static List<BrandEntity> getRows() {
        return rows;
    }


}
