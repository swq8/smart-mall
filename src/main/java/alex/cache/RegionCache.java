package alex.cache;

import alex.entity.RegionEntity;
import alex.lib.Region;
import alex.repository.RegionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegionCache {
    private static RegionRepository regionRepository;
    private static List<Region> provinces;
    private static String regionJson;

    @PostConstruct
    public static synchronized void init() {
        var regionEntities = regionRepository.findAll();
        List<Region> provinces1 = new ArrayList<>();
        List<RegionEntity> tmpEntities = new ArrayList<>();
        regionEntities.forEach(regionEntity -> {
            if (regionEntity.getCode() > 700000) {
                return;
            }
            if (regionEntity.getCode() % 10000 == 0) {
                provinces1.add(new Region(null, regionEntity.getCode(), regionEntity.getName()));
                tmpEntities.add(regionEntity);
            }
        });
        tmpEntities.forEach(regionEntities::remove);
        tmpEntities.clear();

        provinces1.forEach(province -> {
            long max = province.getCode() + 9999;
            regionEntities.forEach(regionEntity -> {
                if (regionEntity.getCode() > max) {
                    return;
                }
                if (regionEntity.getCode() > province.getCode() && regionEntity.getCode() % 100 == 0) {
                    tmpEntities.add(regionEntity);
                    province.getChildren().add(new Region(province, regionEntity.getCode(), regionEntity.getName()));
                }

            });
        });
        tmpEntities.forEach(regionEntities::remove);
        tmpEntities.clear();

        provinces1.forEach(province -> {
            province.getChildren().forEach(city -> {
                regionEntities.forEach(regionEntity -> {
                    long max = city.getCode() + 99;
                    if (regionEntity.getCode() > max) {
                        return;
                    }
                    if (regionEntity.getCode() > city.getCode()) {
                        city.getChildren().add(new Region(city, regionEntity.getCode(), regionEntity.getName()));
                        tmpEntities.add(regionEntity);
                    }
                });
                tmpEntities.forEach(regionEntities::remove);
                tmpEntities.clear();
            });
        });
        provinces = provinces1;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            regionJson = objectMapper.writeValueAsString(provinces);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static Region getRegion(long code) {
        if (code < 110000) {
            return null;
        }
        long tmp = code / 10000 * 10000;
        Region province = null;
        for (var item : provinces) {
            if (item.getCode() == tmp) {
                province = item;
                break;
            }
        }
        if (province == null) {
            return null;
        }
        if (tmp == code) {
            return province;
        }
        tmp = code / 100 * 100;
        Region city = null;
        for (var item : province.getChildren()) {
            if (item.getCode() == tmp) {
                city = item;
                break;
            }
        }
        if (city == null) {
            return null;
        }
        if (city.getCode() == code) {
            return city;
        }
        Region area = null;
        for (var item : city.getChildren()) {
            if (item.getCode() == code) {
                area = item;
                break;
            }
        }
        return area;
    }

    public static List<Region> getProvinces() {
        return provinces;
    }

    public static String getRegionJson() {
        return regionJson;
    }

    @Autowired
    private void autowire(RegionRepository regionRepository) {
        RegionCache.regionRepository = regionRepository;
    }
}
