package smart.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import smart.cache.CategoryCache;
import smart.cache.GoodsCache;
import smart.dto.GoodsQueryDto;
import smart.dto.IdDto;
import smart.entity.BrandEntity;
import smart.entity.CategoryEntity;
import smart.entity.GoodsEntity;
import smart.lib.Pagination;
import smart.lib.thymeleaf.HelperUtils;
import smart.repository.GoodsSpecRepository;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Json;
import smart.util.SqlBuilder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class GoodsService {
    static String[] SORTABLE_COLUMNS = new String[]{
            "id", "price"
    };

    @Resource
    private GoodsSpecRepository goodsSpecRepository;


    public String delete(IdDto idDto) {
        GoodsCache.update();
        return null;
    }

    /**
     * 获取商品列表
     *
     * @param categoryId category id
     * @param keyword    keyword
     * @param sort       sort  排序：[null, "n", "p1", "p2"],
     *                   means [default, price asc, price desc]
     * @param page       page
     */
    public Pagination getGoodsList(long categoryId, String keyword, String sort, long page) {

        StringBuilder sql = new StringBuilder("select id,imgs->>'$[0]' as img,name,price from t_goods where on_sell = true");
        List<Object> sqlParams = new ArrayList<>();
        CategoryEntity categoryEntity = CategoryCache.getEntityById(categoryId);
        if (categoryEntity != null) {
            sql.append(" and cate_id in (").append(categoryId);
            for (var item : CategoryCache.getChildren(categoryId)) {
                sql.append(",").append(item.getId());
            }
            sql.append(")");
        } else {
            categoryId = 0;
        }

        if (StringUtils.hasLength(keyword)) {
            keyword = keyword.trim();
            sql.append(" and name like ?");
            sqlParams.add("%" + keyword + "%");
        }
        sql.append(" order by ");

        switch (sort) {
            // new 发布时间排序
            case "n" -> sql.append("update_time desc,order_num,id desc");

            // price 价格排序
            case "p1" -> sql.append("price,order_num,update_time desc,id desc");

            // 价格倒序
            case "p2" -> sql.append("price desc,order_num,update_time desc,id desc");

            // 默认按推荐排序
            default -> {
                sort = "";
                sql.append("order_num,update_time desc,id desc");
            }
        }
        return Pagination.newBuilder(sql.toString(), sqlParams.toArray())
                .page(page)
                .query(Map.of("cid", Long.toString(categoryId), "q", keyword, "sort", sort))
                .build();
    }

    public Pagination query(GoodsQueryDto query) {
        List<Object> sqlParams = new ArrayList<>();
        String sql = "select id,cate_id,imgs->>'$[0]' as img,name,price from t_goods";
        SqlBuilder sqlBuilder = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("name", query.getName())
                .andEqualsIfNotNull("onSell", query.getOnSell())
                .orderBy(SORTABLE_COLUMNS, query.getSort(), "id,desc");
        var pagination = Pagination.newBuilder(sqlBuilder.buildSql(), sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
        pagination.getRows().forEach(row -> {
            StringBuilder cateName = new StringBuilder();
            Long cid = Helper.parseNumber(row.get("cateId"), Long.class);
            var list = CategoryCache.getCategoryPath(cid);
            if (list != null) {
                for (var i = 0; i < list.size(); i++) {
                    var item = list.get(i);
                    cateName.append(item.getName());
                    if ((i + 1) < list.size()) {
                        cateName.append(" / ");
                    }
                }
            }
            row.put("cateName", cateName.toString());
            String img = (String) row.get("img");
            row.put("img", HelperUtils.imgZoom(img, 80));
        });
        return pagination;

    }

    public String save(GoodsEntity goodsEntity) {
        var msg = validate(goodsEntity);
        if (Objects.nonNull(msg)) {
            return msg;
        }
        if (Objects.isNull(goodsEntity.getImgs())) {
            goodsEntity.setImgs(Json.stringify(goodsEntity.getImgsObj()));
        }
        if (Objects.isNull(goodsEntity.getSpec())) {
            goodsEntity.setSpec(Json.stringify(goodsEntity.getSpecObj()));
        }
        goodsEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Long goodsId = goodsEntity.getId();
        if (Objects.isNull(goodsId)) {
            goodsEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            DbUtils.insert(goodsEntity);
            goodsId = DbUtils.getLastInsertId();
            for (var item : goodsEntity.getSpecProps()) {
                item.setGoodsId(goodsId);
            }
        } else {
            DbUtils.update(goodsEntity);
        }
        if (goodsEntity.getBrandNewSpec()) {
            DbUtils.insertAll(goodsEntity.getSpecProps());
        } else {
            goodsEntity.getSpecProps().forEach(DbUtils::update);
        }
        if (goodsEntity.getSpecProps().isEmpty()) {
            goodsSpecRepository.deleteByGoodsId(goodsId);
        }
        GoodsCache.update();
        return null;
    }

    public String validate(GoodsEntity goodsEntity) {
        if (Objects.nonNull(goodsEntity.getId())
                && Objects.isNull(DbUtils.findByIdForRead(goodsEntity.getId(), GoodsEntity.class))) {
            return "商品不存在";
        }
        if (goodsEntity.getBrandId() != null &&
                DbUtils.findByIdForRead(goodsEntity.getBrandId(), BrandEntity.class) == null) {
            return "品牌不存在";
        }
        if (DbUtils.findByIdForRead(goodsEntity.getCateId(), CategoryEntity.class) == null) {
            return "类别不存在";
        }
        if (Objects.isNull(goodsEntity.getId()) && !goodsEntity.getBrandNewSpec()) {
            return "规格数据错误,新建商品中的brandNewSpec必须为true";
        }
        if (goodsEntity.getSpecObj().isEmpty() && !goodsEntity.getSpecProps().isEmpty()) {
            return "规格数据错误，未知的规格属性数据";
        }
        // validate spec data
        if (!goodsEntity.getSpecObj().isEmpty()) {
            int len = goodsEntity.getSpecObj().size();
            int[] arr = new int[len];
            List<String> specNames = new ArrayList<>();
            while (arr[0] < goodsEntity.getSpecObj().getFirst().getList().size()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < len; i++) {
                    if (i > 0) {
                        sb.append(" ");
                    }
                    sb.append(goodsEntity.getSpecObj().get(i).getList().get(arr[i]).getVal());
                }
                specNames.add(sb.toString());
                arr[len - 1]++;
                for (int i = len - 1; i > 0; i--) {
                    if (arr[i] >= goodsEntity.getSpecObj().get(i).getList().size()) {
                        arr[i - 1]++;
                        arr[i] = 0;
                    }
                }
            }
            if (specNames.size() != goodsEntity.getSpecProps().size()) {
                return "规格数据错误,规格属性不匹配";
            }
            if (goodsEntity.getBrandNewSpec()) {
                if (goodsEntity.getSpecProps().stream().anyMatch(item -> Objects.nonNull(item.getId()))) {
                    return "规格数据错误,新规格属性中id字段必须为null";
                }
            } else {
                if (goodsEntity.getSpecProps().stream().anyMatch(item -> Objects.isNull(item.getId()))) {
                    return "规格数据错误,现有规格属性中id字段不能为null";
                }
                for (int i = 1; i < goodsEntity.getSpecProps().size(); i++) {
                    if (goodsEntity.getSpecProps().get(i - 1).getId() >= goodsEntity.getSpecProps().get(i).getId()) {
                        return "规格数据错误,现有规格属性id必须唯一且按id排序";
                    }
                }
                var specList = goodsSpecRepository.findAllByGoodsIdForWrite(goodsEntity.getId());
                if (specList.size() != goodsEntity.getSpecProps().size()) {
                    return "规格数据错误,现有规格属性数量错误";
                }
                for (int i = 0; i < specList.size(); i++) {
                    long tmpId = goodsEntity.getSpecProps().get(i).getId();
                    if (specList.stream().noneMatch(item -> item.getId() == tmpId)) {
                        return String.format("规格数据错误,现有规格属性id:%d 系统中不存在", tmpId);
                    }
                }
            }
            // write goods info, spec des info,
            goodsEntity.setPrice(goodsEntity.getSpecProps().getFirst().getPrice());
            goodsEntity.setStock(0L);
            goodsEntity.setWeight(0L);
            for (int i = 0; i < specNames.size(); i++) {
                var item = goodsEntity.getSpecProps().get(i);
                item.setGoodsId(goodsEntity.getId());
                if (goodsEntity.getPrice().compareTo(item.getPrice()) > 0) {
                    goodsEntity.setPrice(item.getPrice());
                }
                item.setDes(specNames.get(i));
            }
        }

        return null;
    }
}
