package smart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.math.BigDecimal;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_goods_spec")
public class GoodsSpecEntity extends AbstractEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long goodsId;

    @Digits(integer = 6, fraction = 2, groups = {Add.class, Edit.class})
    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private BigDecimal price;

    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private Long stock;
    @NotNull(groups = {Add.class, Edit.class})
    @PositiveOrZero(groups = {Add.class, Edit.class})
    private Long weight;
    @Null(groups = {Add.class, Edit.class})
    @Column(columnDefinition = "text")
    private String des;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
