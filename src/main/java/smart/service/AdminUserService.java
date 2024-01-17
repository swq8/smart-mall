package smart.service;

import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import smart.auth.UserToken;
import smart.dto.PaginationDto;
import smart.entity.AdminUserEntity;
import smart.lib.Pagination;
import smart.repository.AdminRoleRepository;
import smart.repository.AdminUserRepository;
import smart.repository.UserRepository;
import smart.util.DbUtils;

import java.util.Objects;


@Service
public class AdminUserService {


    @Resource
    UserRepository userRepository;
    @Resource
    UserService userService;

    @Resource
    AdminRoleRepository adminRoleRepository;
    @Resource
    AdminUserRepository adminUserRepository;


    public String deleteByUserId(Long userId) {
        var entity = adminUserRepository.findByUserIdForWrite(userId);
        if (entity == null) {
            return "管理账号不存在";
        }
        adminUserRepository.delete(entity);
        adminUserRepository.flush();
        return null;
    }


    public Pagination query(PaginationDto query) {
        var sql = """
                select t1.id,
                       t1.user_id,
                       t1.enable,
                       t2.name,
                       t1.true_name,
                       t1.roles_id,
                       (select group_concat(name) from t_admin_role where find_in_set(id, t1.roles_id) order by order_num)
                                                                      as roles_name,
                       date_format(t2.last_login_time, '%Y-%m-%d %T') as last_login_time,
                       t2.last_login_ip,
                       date_format(t1.create_time, '%Y-%m-%d %T')     as create_time
                from t_admin_user t1
                         left join t_user t2 on t1.user_id = t2.id
                order by t2.last_login_time desc
                                """;

        var builder = Pagination.newBuilder(sql);
        if (query.getPage() != null) {
            builder.page(query.getPage());
        }
        if (query.getPageSize() != null) {
            builder.pageSize(query.getPageSize());
        }
        return builder.build();

    }

    public LoginResult login(String name, String password, String ip) {

        LoginResult loginResult = new LoginResult();
        var result = userService.getUserWithLogin(name, password, ip);
        if (result.getUserEntity() == null) {
            return loginResult.setError(result.getError());
        }
        var adminUserEntity = adminUserRepository.findByUserIdForWrite(result.getUserEntity().getId());
        if (adminUserEntity == null) {
            return loginResult.setError("该账号不是管理员");
        }
        UserToken userToken = new UserToken(result.getUserEntity());
        return loginResult.setUserToken(userToken);
    }

    public String save(AdminUserEntity adminUserEntity) {
        var userEntity = userRepository.findByNameForWrite(adminUserEntity.getName());
        if (userEntity == null) {
            return "用户不存在";
        }
        adminUserEntity.setRolesId(adminRoleRepository.getAvailableRolesId(adminUserEntity.getRolesId()));
        if (adminUserEntity.getUserId() == null) {
            adminUserEntity.setUserId(userEntity.getId());
            try {
                DbUtils.insert(adminUserEntity);

            } catch (DuplicateKeyException ignored) {
                return "该管理员已存在";
            }
        } else if (Objects.equals(userEntity.getId(), adminUserEntity.getUserId())) {
            var entity = adminUserRepository.findByUserIdForWrite(userEntity.getId());
            if (entity == null) {
                return "管理账号不存在";
            }
            adminUserEntity.setId(entity.getId());
            DbUtils.update(adminUserEntity);
        }
        return null;
    }


    public static class LoginResult {
        String error;
        UserToken userToken;

        public String getError() {
            return error;
        }

        public LoginResult setError(String error) {
            this.error = error;
            return this;
        }

        public UserToken getUserToken() {
            return userToken;
        }

        public LoginResult setUserToken(UserToken userToken) {
            this.userToken = userToken;
            return this;
        }
    }
}
