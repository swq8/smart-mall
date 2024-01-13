package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smart.config.AppConfig;
import smart.dto.express.FeeRuleDto;
import smart.dto.express.FreeRuleDto;
import smart.entity.ExpressCompanyEntity;
import smart.repository.ExpressCompanyRepository;
import smart.util.Json;

import java.util.List;

@Component
public class ExpressCache {
    private static List<ExpressCompanyEntity> companies;
    private static ExpressCompanyRepository expressCompanyRepository;
    private static FreeRuleDto freeRuleDto;
    private static FeeRuleDto feeRuleDto;

    @PostConstruct
    public static synchronized void update() {
        companies = expressCompanyRepository.findAllAvailable();
        var list = AppConfig.getJdbcClient().sql("select * from t_system where entity='shipping'").query().listOfRows();
        String json;
        for (var map : list) {
            json = (String) map.get("value");
            if (json.length() < 10) {
                continue;
            }
            switch ((String) map.get("attribute")) {
                case "feeRule" -> feeRuleDto = Json.parse(json, FeeRuleDto.class);
                case "freeRule" -> freeRuleDto = Json.parse(json, FreeRuleDto.class);

            }
        }
        if (freeRuleDto == null) {
            freeRuleDto = new FreeRuleDto();
        }
        if (feeRuleDto == null) {
            feeRuleDto = new FeeRuleDto();
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

    public static FreeRuleDto getFreeRule() {
        return freeRuleDto;
    }

    public static FeeRuleDto getFeeRule() {
        return feeRuleDto;
    }

    @Autowired
    private void autowire(ExpressCompanyRepository expressCompanyRepository) {
        ExpressCache.expressCompanyRepository = expressCompanyRepository;
    }


}
