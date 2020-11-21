package alex.repository;

import alex.entity.SystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemRepository extends JpaRepository<SystemEntity, Long> {
    SystemEntity findByEntityAndAttribute(String entity, String attribute);
}
