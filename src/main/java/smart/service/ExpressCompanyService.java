package smart.service;

import org.springframework.stereotype.Service;
import smart.cache.ExpressCache;
import smart.entity.ExpressCompanyEntity;
import smart.util.DbUtils;

@Service
public class ExpressCompanyService {

    public String delete(Long id) {
        DbUtils.deleteById(ExpressCompanyEntity.class, id);
        ExpressCache.update();
        return null;
    }

    public String save(ExpressCompanyEntity entity) {
        if (entity.getId() == null) {
            DbUtils.insert(entity);
        } else {
            DbUtils.update(entity);
        }
        ExpressCache.update();
        return null;
    }
}