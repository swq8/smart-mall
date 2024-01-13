package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import smart.entity.AdminRoleEntity;

import java.util.List;

public interface AdminRoleRepository extends JpaRepository<AdminRoleEntity, Long> {

    /**
     * get available authorize in str
     *
     * @param rolesId roles id
     * @return available authorize str
     */
    @Query(nativeQuery = true, value = """
            select ifnull(group_concat(id), '') from t_admin_role where find_in_set(id, :rolesId) order by order_num, id""")
    String getAvailableRolesId(String rolesId);

    /**
     * get authorize by roles id
     *
     * @param rolesId roles id
     * @return authorize id, like "1,2,3"
     */
    @Query(nativeQuery = true, value = """
            select ifnull(group_concat(authorize), '') from t_admin_role where find_in_set(id, :rolesId);""")
    String getAuthorizeByRolesId(String rolesId);

    List<AdminRoleEntity> findAllByOrderByOrderNum();
}
