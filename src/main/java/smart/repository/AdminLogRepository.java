package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smart.entity.AdminLogEntity;

public interface AdminLogRepository extends JpaRepository<AdminLogEntity, Long> {
}
