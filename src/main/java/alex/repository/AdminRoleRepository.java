package alex.repository;

import alex.entity.AdminRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRoleRepository extends JpaRepository<AdminRoleEntity, Long> {
}
