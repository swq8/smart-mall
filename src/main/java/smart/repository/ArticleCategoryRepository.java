package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smart.entity.ArticleCategoryEntity;

import java.util.List;

@Repository
public interface ArticleCategoryRepository extends JpaRepository<ArticleCategoryEntity, Long> {
    List<ArticleCategoryEntity> findAllByOrderByOrderNum();
}
