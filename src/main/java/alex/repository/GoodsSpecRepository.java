package alex.repository;

import alex.entity.GoodsSpecEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface GoodsSpecRepository extends JpaRepository<GoodsSpecEntity, Long> {

    long deleteByGoodsIdAndIdNotIn(long goodsId, Collection<Long> notIn);

    @Query(value = "select * from goodsSpec where goodsId = ? order by idx for update", nativeQuery = true)
    List<GoodsSpecEntity> findAllByGoodsIdForUpdate(long goodsId);

    List<GoodsSpecEntity> findAllByGoodsIdOrderByIdxAsc(long goodsId);
}
