package alex.repository;

import alex.entity.ExpressCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpressCompanyRepository extends JpaRepository<ExpressCompanyEntity, Long> {
    @Query(value = "select * from expressCompanies order by recommend desc,id asc", nativeQuery = true)
    List<ExpressCompanyEntity> findAll();
}
