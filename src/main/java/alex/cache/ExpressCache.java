package alex.cache;

import alex.Application;
import alex.entity.ExpressCompanyEntity;
import alex.lib.express.FreeRule;
import alex.lib.express.PriceRule;
import alex.lib.express.ProvincePrice;
import alex.repository.ExpressCompanyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExpressCache {
    private static List<ExpressCompanyEntity> companies;
    private static ExpressCompanyRepository expressCompanyRepository;
    private static FreeRule freeRule;
    private static PriceRule priceRule;

    @Autowired
    private void autowire(ExpressCompanyRepository expressCompanyRepository) {
        ExpressCache.expressCompanyRepository = expressCompanyRepository;
    }

    @PostConstruct
    public static synchronized void init() {
        companies = expressCompanyRepository.findAll();
        var list = Application.JDBC_TEMPLATE.queryForList("select * from system where entity='shipping'");
        String json = null;
        ObjectMapper objectMapper = new ObjectMapper();
        for (var map : list) {
            json = (String) map.get("value");
            if (json.length() < 10) {
                continue;
            }
            switch ((String)map.get("attribute")) {
                case "freeRule":
                    try {
                        freeRule = objectMapper.readValue(json, FreeRule.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "priceRule":
                    try {
                        priceRule = objectMapper.readValue(json, PriceRule.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        if (freeRule == null) {
            freeRule = new FreeRule();
        }
        if (priceRule == null) {
            priceRule = new PriceRule();
        }
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

    public static FreeRule getFreeRule() {
        return freeRule;
    }

    public static PriceRule getPriceRule() {
        return priceRule;
    }


}
