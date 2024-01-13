package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import smart.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserEntity, Long> {

    AdminUserEntity findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select a from AdminUserEntity a where a.userId = :id")
    AdminUserEntity findByUserIdForWrite(long id);

    /**
     * update roles id after role deleted
     *
     * @param roleId update condition
     * @return the num of affected rows
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            update t_admin_user t1
            set t1.roles_id =
                    (select group_concat(t2.id)
                     from t_admin_role t2
                     where find_in_set(t2.id, t1.roles_id)
                     order by order_num, id)
            where find_in_set(:roleId, t1.roles_id)""")
    int updateRolesId(Long roleId);
}
