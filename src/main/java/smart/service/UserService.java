package smart.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import smart.auth.UserToken;
import smart.config.RedisConfig;
import smart.dto.GeneralQueryDto;
import smart.entity.UserBalanceLogEntity;
import smart.entity.UserEntity;
import smart.lib.Pagination;
import smart.lib.session.Session;
import smart.lib.status.AccountStatus;
import smart.repository.UserBalanceLogRepository;
import smart.repository.UserRepository;
import smart.util.DbUtils;
import smart.util.LogUtils;
import smart.util.SqlBuilder;
import smart.util.ValidatorUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserService {

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    UserBalanceLogRepository userBalanceLogRepository;
    @Resource
    private UserRepository userRepository;

    public String changeBalance(UserEntity userEntity, BigDecimal amount, String note) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        if (!StringUtils.hasText(note)) {
            return "备注不得为空";
        }
        BigDecimal newBalance = userEntity.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return "余额不足";
        }
        userEntity.setBalance(newBalance);
        userRepository.save(userEntity);
        var logEntity = new UserBalanceLogEntity();
        logEntity.setAmount(amount);
        logEntity.setUid(userEntity.getId());
        logEntity.setBalance(userEntity.getBalance());
        logEntity.setNote(note);
        logEntity.setTime(new Timestamp(System.currentTimeMillis()));
        userBalanceLogRepository.saveAndFlush(logEntity);
        return null;

    }

    /**
     * change password
     *
     * @param uid      user id
     * @param password password
     * @return null: success, else error msg
     */
    public String changePassword(long uid, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(uid);
        userEntity.setPassword(passwordEncoder.encode(password));
        if (DbUtils.update(userEntity, "password") == 0) {
            return "用户不存在";
        }
        return null;
    }

    /**
     * delete all sessions by user id
     *
     * @param userId user id
     */
    public void deleteSessionsByUserId(long userId) {
        var redis = RedisConfig.getStringRedisTemplate();
        var items = redis.opsForSet().members(UserToken.REDIS_USER_PREFIX + userId);
        assert items != null;
        redis.delete(items);
    }

    public Result getUserWithLogin(String name, String password, String ip) {
        Result result = new Result();
        if (ValidatorUtils.validateNotNameAndPassword(name, password)) {
            return result.setError(ValidatorUtils.ID_OR_PWD_ERROR);
        }
        var userEntity = userRepository.findByNameForWrite(name);
        if (userEntity == null) {
            return result.setError(ValidatorUtils.ID_OR_PWD_ERROR);
        }
        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            return result.setError(ValidatorUtils.ID_OR_PWD_ERROR);
        }
        long status = userEntity.getStatus();
        if (status > 0) {
            return result.setError(AccountStatus.getStatusName(status));
        }
        if (ip != null) {
            userEntity.setLastLoginIp(ip);
            userEntity.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
            userRepository.save(userEntity);
        }
        return result.setUserEntity(userEntity);


    }

    public Result login(String name, String password, String ip) {
        Result result = getUserWithLogin(name, password, ip);
        if (result.getUserEntity() == null) {
            result.errors.put("name", result.getError());
            result.errors.put("password", result.getError());
            return result;
        }

        return result;
    }

    /**
     * 退出登录
     *
     * @param request http request
     */
    public void logout(HttpServletRequest request) {
        Session session = Session.from(request);
        if (session != null) {
            session.delete(StringUtils.uncapitalize(UserToken.class.getSimpleName()));
            request.removeAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()));
        }

    }

    /**
     * register user
     *
     * @param name       name
     * @param password   password
     * @param registerIp register ip
     * @return error msg
     */

    public String register(String name, String password, String registerIp) {
        if (userRepository.findByName(name) != null) {
            return "用户名已被注册";
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setRegisterTime(new Timestamp(System.currentTimeMillis()));
        userEntity.setRegisterIp(registerIp);
        try {
            DbUtils.insert(userEntity);
        } catch (DuplicateKeyException ex) {
            DbUtils.rollback();
            return "用户名已被注册";
        } catch (Exception ex) {
            DbUtils.rollback();
            LogUtils.error(this.getClass(), "", ex);
            return "未知错误,请联系管理员.";
        }
        return null;
    }

    public Pagination query(GeneralQueryDto query) {
        String[] sortableColumns = new String[]{
                "balance", "id", "lastLoginTime", "name"
        };
        List<Object> sqlParams = new ArrayList<>();
        String sql =
                """
                        select id,
                               name,
                               level,
                               phone,
                               gender,
                               email,
                               balance,
                               date_format(last_login_time, '%Y-%m-%d %T') as last_login_time,
                               last_login_ip,
                               status,
                               date_format(register_time, '%Y-%m-%d %T')   as register_time,
                               register_ip
                        from t_user
                        """;
        SqlBuilder sqlBuilder = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("name", query.getName())
                .orderBy(sortableColumns, query.getSort(), "id,desc");
        return Pagination.newBuilder(sqlBuilder.buildSql(), sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }


    public static class Result {
        public final Map<String, String> errors = new LinkedHashMap<>();
        public String error;
        public UserEntity userEntity;

        public String getError() {
            return error;
        }

        public Result setError(String error) {
            this.error = error;
            return this;
        }

        public UserEntity getUserEntity() {
            return userEntity;
        }

        public Result setUserEntity(UserEntity userEntity) {
            this.userEntity = userEntity;
            return this;

        }
    }
}
