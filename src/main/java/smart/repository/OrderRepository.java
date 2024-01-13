package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smart.entity.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Modifying
    @Query(value = """
            update OrderEntity as a
            set a.payTime = now(), a.status = 1
            where a.status = 0 and a.no = :orderNo
            """)
    int adminPay(long orderNo);

    @Modifying
    @Query(value = """
            update OrderEntity as a
            set a.status = 4
            where a.status in (0, 1) and a.no = :orderNo
            """)
    int cancelOrder(long orderNo);

    /**
     * can ship
     *
     * @param orderNo order no
     * @return rows
     */
    @Modifying
    @Query(value = """
            update OrderEntity as a
            set a.expressId = 0, a.expressNo = '', a.shippingTime = null, a.status = 1
            where a.status = 2 and a.no = :orderNo
            """)
    int cancelShip(long orderNo);

    @Modifying
    @Query(value = """
            update OrderEntity as a
            set a.expressId = :expressId, a.expressNo = :expressNo, a.shippingTime = now(), a.status = 2
            where a.status = 1 and a.no = :orderNo
            """)
    int ship(long orderNo, long expressId, String expressNo);

    /**
     * 根据订单号返回订单
     *
     * @param no 订单号
     * @return 订单entity
     */

    OrderEntity findByNo(Long no);

    /**
     * 根据订单号返回订单(有锁)
     *
     * @param no 订单号
     * @return 订单entity
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from OrderEntity t where t.no = :no")
    OrderEntity findByNoForWrite(Long no);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from OrderEntity t where t.no = :no and t.userId = :uid")
    OrderEntity findByNoAndUserIdForWrite(Long no, Long uid);
}
