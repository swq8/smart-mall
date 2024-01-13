package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import smart.entity.ExpressCompanyEntity;

import java.util.List;

public interface ExpressCompanyRepository extends JpaRepository<ExpressCompanyEntity, Long> {

    @Query("select a from ExpressCompanyEntity a where a.enable order by a.orderNum")
    List<ExpressCompanyEntity> findAllAvailable();
    List<ExpressCompanyEntity> findAllByOrderByOrderNum();
}
