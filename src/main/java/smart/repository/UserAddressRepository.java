package smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smart.entity.UserAddressEntity;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {

    long countByUserId(long uid);

    List<UserAddressEntity> findAllByUserIdOrderByDftDesc(Long userId);

    UserAddressEntity findByIdAndUserId(long id, long uid);

}
