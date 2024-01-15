package smart.service;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import smart.entity.UserAddressEntity;
import smart.repository.UserAddressRepository;
import smart.util.DbUtils;

import java.util.List;

@Service
public class UserAddressService {

    @Resource
    JdbcClient jdbcClient;
    @Resource
    UserAddressRepository userAddressRepository;

    public String addAddress(UserAddressEntity entity) {
        long maxAddress = 10;
        if (userAddressRepository.countByUserId(entity.getUserId()) >= maxAddress) {
            return "新增地址失败，现有地址已达到上限(" + maxAddress + "条)";
        }
        DbUtils.insert(entity);
        entity.setId(DbUtils.getLastInsertId());
        if (entity.getDft() > 0) {
            jdbcClient.sql("update t_user_address set dft=0 where user_id=? and id != ?").params(entity.getUserId(), entity.getId()).update();
        }
        return null;
    }

    public List<UserAddressEntity> findByUserId(Long userId) {
        return userAddressRepository.findAllByUserIdOrderByDftDesc(userId);
    }

    public void setDefault(UserAddressEntity entity) {
        jdbcClient.sql("update t_user_address set dft=1 where id=?").param(entity.getId()).update();
        jdbcClient.sql("update t_user_address set dft=0 where user_id=? and id != ?").params(entity.getUserId(), entity.getId()).update();
    }

    public void updateAddress(UserAddressEntity entity) {
        DbUtils.update(entity);
        if (entity.getDft() > 0) {
            jdbcClient.sql("update t_user_address set dft=0 where user_id=? and id != ?").params(entity.getUserId(), entity.getId()).update();
        }
    }
}
