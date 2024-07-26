package smart.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.config.AppConfig;
import smart.util.Json;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_goods")
public class GoodsEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;
    @Positive(groups = {Add.class, Edit.class})
    private Long brandId;
    @NotNull(groups = {Add.class, Edit.class})
    @Positive(groups = {Add.class, Edit.class})
    private Long cateId;
    @Null(groups = {Add.class, Edit.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = AppConfig.DATE_TIME_FORMAT, timezone = AppConfig.TIME_ZONE)
    private Timestamp createTime;
    @Null(groups = {Add.class, Edit.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = AppConfig.DATE_TIME_FORMAT, timezone = AppConfig.TIME_ZONE)
    private Timestamp updateTime;
    @NotBlank(groups = {Add.class, Edit.class}, message = "商品名称不能为空")
    private String name;
    @NotNull(groups = {Add.class, Edit.class})
    @Column(columnDefinition = "text")
    private String des;
    @Column(columnDefinition = "text")
    @JsonIgnore
    @Null(groups = {Add.class, Edit.class})
    private String imgs;

    @NotEmpty(groups = {Add.class, Edit.class})
    @Transient
    private List<String> imgsObj;
    @NotNull(groups = {Add.class, Edit.class})
    private Boolean onSell;

    @Digits(integer = 8, fraction = 2, groups = {Add.class, Edit.class})
    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private BigDecimal price;

    @NotNull(groups = {Add.class, Edit.class})
    private Boolean shippingFee;
    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private Long stock;
    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private Long weight;
    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private Long orderNum;
    @NotNull(groups = {Add.class, Edit.class})
    @Transient
    private Boolean brandNewSpec;
    @JsonIgnore
    @Column(columnDefinition = "text")
    private String spec;

    @NotNull(groups = {Add.class, Edit.class})
    @Transient
    @Valid
    private List<SpecItem> specObj;
    @NotNull(groups = {Add.class, Edit.class})
    @Transient
    @Valid
    private List<GoodsSpecEntity> specProps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public List<String> getImgsObj() {
        if (imgsObj == null) {
            imgsObj = Json.parseList(imgs, String.class);
        }
        return imgsObj;
    }


    public Boolean getOnSell() {
        return onSell;
    }

    public void setOnSell(Boolean onSell) {
        this.onSell = onSell;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Boolean shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Boolean getBrandNewSpec() {
        return brandNewSpec;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public List<SpecItem> getSpecObj() {
        if (specObj == null) {
            specObj = Json.parseList(spec, SpecItem.class, true);
        }
        return specObj;
    }

    public List<GoodsSpecEntity> getSpecProps() {
        return specProps;
    }

    public void setSpecProps(List<GoodsSpecEntity> specProps) {
        this.specProps = specProps;
    }

    public static class SpecItem {
        @NotBlank(groups = {Add.class, Edit.class})
        String name;

        @NotEmpty(groups = {Add.class, Edit.class})
        @Valid
        List<SpecEntity.Item> list;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<SpecEntity.Item> getList() {
            return list;
        }

        public void setList(List<SpecEntity.Item> list) {
            this.list = list;
        }
    }

}
