package smart.repository;

import org.springframework.data.jpa.repository.QueryHints;
import smart.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findAllByOrderByOrderNum();

    @Query(value = "select a from PaymentEntity a where a.enable  order by a.orderNum")
    List<PaymentEntity> findAvailable();

    PaymentEntity findByName(String name);

    @Query(value = "select * from t_payment where name = :name for update", nativeQuery = true)
    PaymentEntity findByNameForWrite(String name);

}
