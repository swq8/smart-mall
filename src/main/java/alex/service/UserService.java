package alex.service;

import alex.Application;
import alex.entity.UserEntity;
import alex.lib.Crypto;
import alex.lib.Database;
import alex.lib.Helper;
import alex.lib.Validate;
import alex.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class UserService {

    @Resource
    private UserRepository userRepository;

    @Transactional
    public String editPassword(Long uid, String password, String ip) {
        String salt = Helper.randomString(4);
        if (userRepository.updateForPassword(uid, Crypto.sha3_256(password + salt), salt) == 0) {
            return null;
        }
        Database.insertUserLog(uid, 4, "", ip);
        return salt;
    }

    @Transactional
    public Result login(String name, String password, String ip) {
        Result result = new Result();
        String defaultErr = "账号或密码错误";
        if (name != null) {
            name = name.toLowerCase();
        }
        result.errors.put("name", defaultErr);
        result.errors.put("password", defaultErr);
        if (Validate.name(name, "") != null || Validate.password(password, "") != null) {
            return result;
        }
        UserEntity userEntity = userRepository.findByName(name);
        if (userEntity == null) {
            return result;
        }
        long uid = userEntity.getId();
        password = Crypto.sha3_256(password + userEntity.getSalt());
        if (!password.equals(userEntity.getPassword())) {
            Database.insertUserLog(uid, 2, "密码错误", ip);
            return result;
        }
        result.errors.clear();
        int status = userEntity.getStatus();
        if (status != 1) {
            result.errors.put("name", "用户被锁定");
            Database.insertUserLog(uid, 2, "用户被锁定", ip);
            return result;
        }
        userRepository.updateForLogin(uid, ip);
        Database.insertUserLog(uid, 1, "", ip);
        result.userEntity = userEntity;
        return result;
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
        String salt = Helper.randomString(4);
        if (Application.JDBC_TEMPLATE.queryForObject("select count(*) from users where name=?", new String[]{name}, Integer.class) > 0) {
            return "此用户已被注册";
        }
        try {
            Application.JDBC_TEMPLATE.update("insert into users (name, password, salt, registerTime, registerIp) values (?,?,?,now(),?)",
                    name, Crypto.sha3_256(password + salt), salt, registerIp);
        } catch (DuplicateKeyException ex) {
            return "此用户已被注册";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "未知错误,请联系管理员.";
        }
        return null;
    }

    public static class Result {
        public final Map<String, String> errors = new LinkedHashMap<>();
        public String error;
        public UserEntity userEntity;
    }
}
