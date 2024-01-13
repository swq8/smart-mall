package smart.service;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import smart.entity.OrderGoodsEntity;
import smart.lib.status.OrderGoodsStatus;
import smart.repository.OrderGoodsRepository;
import smart.util.Helper;

import java.util.List;

@Service
public class OrderGoodsService {
    @Resource
    JdbcClient jdbcClient;

    @Resource
    OrderGoodsRepository orderGoodsRepository;

    /**
     * 订单中的商品返回库存
     *
     * @param orderNo 订单号
     */
    public void backOrderGoods(long orderNo) {
        List<OrderGoodsEntity> goodsEntities = orderGoodsRepository.findAllByOrderNoForWrite(orderNo);
        for (var item : goodsEntities) {
            if (item.getSpecId() > 0) {
                jdbcClient.sql("update t_goods_spec set stock = stock + ? where id = ?")
                        .params(item.getNum(), item.getSpecId())
                        .update();
            } else {
                jdbcClient.sql("update t_goods set stock = stock + ? where id = ?")
                        .params(item.getNum(), item.getGoodsId())
                        .update();
            }
        }
    }

    public List<OrderGoodsEntity> getOrderGoods(long orderNo){
        var sql = """
                select b.imgs ->> '$[0]' as img, a.*
                from t_order_goods a
                         left join t_goods b
                                   on a.goods_id = b.id
                where a.order_no = ?
                """;
        var list = jdbcClient.sql(sql)
                .param(orderNo)
                .query(OrderGoodsEntity.class).
                list();
        list.forEach(row->{
            row.setPriceStr(Helper.priceFormat(row.getPrice()));
            row.setSumPriceStr(Helper.priceFormat(row.getPrice() * row.getNum()));
            row.setStatusStr(OrderGoodsStatus.getStatusName(row.getStatus()));
        });
        return list;
    }
}
