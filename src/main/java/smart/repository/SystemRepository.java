package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smart.entity.SystemEntity;


@Repository
public interface SystemRepository extends JpaRepository<SystemEntity, Long> {

    @Query(value = "select * from t_system where entity = :entity and attribute = :attribute",
            nativeQuery = true)
    SystemEntity findByEaForWrite(String entity, String attribute);

    /**
     * update value by entity and attribute
     *
     * @param val       value
     * @param entity    entity
     * @param attribute attribute
     */
    @Modifying
    @Query("update SystemEntity t set t.value = :val where t.entity = :entity and t.attribute = :attribute")
    void updateValueByEa(Object val, String entity, String attribute);
}
