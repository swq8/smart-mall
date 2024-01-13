package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smart.entity.AdminMenuEntity;

import java.util.List;

@Repository
public interface AdminMenuRepository extends JpaRepository<AdminMenuEntity, Long> {


    /**
     * get available authorize in str
     * @param str authorize str
     * @return available authorize str
     */
    @Query(nativeQuery = true, value = """
            select ifnull(group_concat(id), '')
            from t_admin_menu
            where type = 3
              and find_in_set(id, :str)""")
    String getAvailableAuthorize(String str);

    long countByParentId(Long parent);
    List<AdminMenuEntity> findAllByOrderByOrderNumAscIdAsc();
}
