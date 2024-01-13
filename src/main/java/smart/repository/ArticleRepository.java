package smart.repository;

import smart.entity.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {

    Long countByCateId(Long cateId);
    @Query("select a from ArticleEntity a where a.visible order by a.orderNum, a.releaseTime desc")
    List<ArticleEntity> findAllVisible();
}
