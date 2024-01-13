package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smart.entity.OrderGoodsEntity;

import java.util.List;

public interface OrderGoodsRepository extends JpaRepository<OrderGoodsEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from OrderGoodsEntity t where t.orderNo = :orderNo")
    List<OrderGoodsEntity> findAllByOrderNoForWrite(@Param("orderNo") long orderNo);
}
