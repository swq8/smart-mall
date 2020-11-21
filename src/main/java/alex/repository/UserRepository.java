package alex.repository;

import alex.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByName(String name);

    @Modifying
    @Query(value = "update users set lastLoginIp = :lastLoginIp, lastLoginTime=now() where id = :id", nativeQuery = true)
    void updateForLogin(@Param("id") long id, @Param("lastLoginIp") String ip);

    @Modifying
    @Query(value = "update users set password = :password, salt = :salt where id = :id", nativeQuery = true)
    int updateForPassword(@Param("id") long id, @Param("password") String password, @Param("salt") String salt);

    @Modifying
    @Query(value = "update users set phone = :phone where id = :id", nativeQuery = true)
    int updateInfo(@Param("id") long id, @Param("phone") String phone);

}
