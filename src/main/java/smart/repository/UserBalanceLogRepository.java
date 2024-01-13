package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smart.entity.UserBalanceLogEntity;

public interface UserBalanceLogRepository extends JpaRepository<UserBalanceLogEntity, Long> {
}
