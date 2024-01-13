package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import smart.entity.GoodsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GoodsRepository extends JpaRepository<GoodsEntity, Long> {
    long countByCateId(long cateId);
}
