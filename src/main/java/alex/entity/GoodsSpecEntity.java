package alex.entity;

import javax.persistence.*;

@Entity
@Table(name = "goodsSpec")
public class GoodsSpecEntity {
    private long id;
    private long goodsId;
    private long idx;
    private long price;
    private long stock;
    private long weight;
    private String des;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }


    public long getIdx() {
        return idx;
    }

    public void setIdx(long idx) {
        this.idx = idx;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }


    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }


    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Column(columnDefinition = "text")
    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
