package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import smart.entity.GoodsSpecEntity;

import java.util.Collection;
import java.util.List;

public interface GoodsSpecRepository extends JpaRepository<GoodsSpecEntity, Long> {

    void deleteByGoodsId(long goodsId);
    void deleteByGoodsIdAndIdNotIn(long goodsId, Collection<Long> notIn);

    List<GoodsSpecEntity> findByGoodsId(long goodsId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from GoodsSpecEntity t where t.goodsId = :goodsId")
    List<GoodsSpecEntity> findAllByGoodsIdForWrite(long goodsId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from GoodsSpecEntity t where t.id = :id")
    GoodsSpecEntity findByIdForWrite(long id);
}
