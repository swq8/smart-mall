package alex.cache;

import alex.entity.ExpressCompanyEntity;
import alex.repository.ExpressCompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ExpressCache {
    private static List<ExpressCompanyEntity> companies;
    private static ExpressCompanyRepository expressCompanyRepository;

    @PostConstruct
    public static synchronized void init() {
        companies = expressCompanyRepository.findAll();
    }

    public static List<ExpressCompanyEntity> getCompanies() {
        return companies;
    }

    /**
     * 根据id获取快递公司名称
     *
     * @param id 快递id
     * @return 快递公司名称
     */
    public static String getNameById(long id) {
        for (var c : companies) {
            if (c.getId() == id) {
                return c.getName();
            }
        }
        return null;
    }

    @Autowired
    private void autowire(ExpressCompanyRepository expressCompanyRepository) {
        ExpressCache.expressCompanyRepository = expressCompanyRepository;
    }
}
