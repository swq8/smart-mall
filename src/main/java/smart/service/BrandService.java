package smart.service;

import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import smart.entity.BrandEntity;
import smart.lib.Pagination;
import smart.repository.BrandRepository;
import smart.util.DbUtils;
import smart.util.SqlBuilder;
import smart.dto.GeneralQueryDto;
import smart.dto.IdDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class BrandService {
    static String BRAND_IS_EXIST = "商品品牌已存在";

    @Resource
    BrandRepository brandRepository;

    public String delete(IdDto idDto) {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setId(idDto.getId());
        DbUtils.delete(brandEntity);
        return null;
    }

    public List<BrandEntity> findAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return brandRepository.findAll(sort);
    }

    public Pagination query(GeneralQueryDto query) {
        List<Object> sqlParams = new ArrayList<>();
        String sql = "select  * from t_brand";
        SqlBuilder sqlBuilder = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("name", query.getName());
        return Pagination.newBuilder(sqlBuilder.buildSql(), sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    public String save(BrandEntity brandEntity) {
        brandEntity.setName(brandEntity.getName().trim());
        try {
            if (brandEntity.getId() == null) {
                if (brandRepository.findFirstByName(brandEntity.getName()) != null) {
                    return BRAND_IS_EXIST;
                }
                DbUtils.insert(brandEntity);
            } else {
                if (brandRepository.findFirstByNameAndIdIsNot(brandEntity.getName(), brandEntity.getId()) != null) {
                    return BRAND_IS_EXIST;
                }
                DbUtils.update(brandEntity);
            }
        } catch (DuplicateKeyException ignored) {
            return BRAND_IS_EXIST;
        }


        return null;
    }


}
