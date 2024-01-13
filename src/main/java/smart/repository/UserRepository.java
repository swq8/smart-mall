package smart.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smart.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select a from UserEntity a where a.id = :id")
    UserEntity findByIdForWrite(Long id);

    UserEntity findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from UserEntity t where t.name = :name")
    UserEntity findByNameForWrite(String name);

}
