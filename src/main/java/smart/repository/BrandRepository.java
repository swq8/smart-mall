package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import smart.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    BrandEntity findFirstByName(String name);
    BrandEntity findFirstByNameAndIdIsNot(String name, Long id);
}
