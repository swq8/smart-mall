package smart.service;

import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import smart.entity.SpecEntity;
import smart.lib.Pagination;
import smart.lib.thymeleaf.HelperUtils;
import smart.repository.SpecRepository;
import smart.util.DbUtils;
import smart.util.Json;
import smart.util.SqlBuilder;
import smart.dto.GeneralQueryDto;
import smart.dto.IdDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecService {
    static String[] SORTABLE_COLUMNS = new String[]{
            "id", "name"
    };
    static String SPEC_IS_EXIST = "规格已存在";

    @Resource
    SpecRepository specRepository;

    public String delete(IdDto idDto) {
        SpecEntity specEntity = new SpecEntity();
        specEntity.setId(idDto.getId());
        DbUtils.delete(specEntity);
        return null;
    }

    public List<SpecEntity> findAll(){
        return specRepository.findAllByOrderByName();
    }

    public Pagination query(GeneralQueryDto query) {
        List<Object> sqlParams = new ArrayList<>();
        String sql = "select  * from t_spec";
        SqlBuilder sqlBuilder = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("name", query.getName())
                .orderBy(SORTABLE_COLUMNS, query.getSort(), "name,asc");
        var builder = Pagination.newBuilder(sqlBuilder.buildSql(), sqlParams.toArray());
        var result = builder.page(query.getPage()).pageSize(query.getPageSize()).build();

        result.getRows().forEach(row -> {
            row.put("itemsObj", Json.parseList(row.get("items").toString(), SpecEntity.Item.class, true));
            row.remove("items");
        });
        return result;
    }

    public String save(SpecEntity specEntity) {
        specEntity.setName(specEntity.getName().trim());
        // image zoom
        specEntity.getItemsObj().forEach(item -> {
            if (item.getImg().length() > 4 && !item.getImg().contains("?")) {
                item.setImg(HelperUtils.imgZoom(item.getImg(), 40));
            }
        });
        if (specEntity.getItems() == null && specEntity.getItemsObj() != null) {
            specEntity.setItems(Json.stringify(specEntity.getItemsObj()));
        }
        try {
            if (specEntity.getId() == null) {
                if (specRepository.findFirstByNameAndNote(specEntity.getName(), specEntity.getNote()) != null) {
                    return SPEC_IS_EXIST;
                }
                DbUtils.insert(specEntity);
            } else {
                if (specRepository.findFirstByNameAndNoteAndIdIsNot(specEntity.getName(),
                        specEntity.getNote(), specEntity.getId()) != null) {
                    return SPEC_IS_EXIST;
                }
                DbUtils.update(specEntity);
            }
        } catch (DuplicateKeyException ignored) {
            return SPEC_IS_EXIST;
        }


        return null;
    }

}
