package smart.service;

import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import smart.entity.AdminRoleEntity;
import smart.repository.AdminMenuRepository;
import smart.repository.AdminRoleRepository;
import smart.repository.AdminUserRepository;
import smart.util.DbUtils;

import java.util.Arrays;

@Service
public class AdminRoleService {

    @Resource
    AdminMenuRepository adminMenuRepository;
    @Resource
    AdminUserRepository adminUserRepository;
    @Resource
    AdminRoleRepository adminRoleRepository;

    public String delete(Long id) {
        var entity = DbUtils.findByIdForWrite(id, AdminRoleEntity.class);
        if (entity == null) return "角色不存在";
        if (id == 1L) return "不能删除系统内置角色";
        DbUtils.delete(entity);
        adminUserRepository.updateRolesId(id);
        return null;
    }

    public String getAuthorizeIdsByRolesId(String rolesId) {
        var authorize = adminRoleRepository.getAuthorizeByRolesId(rolesId);
        var arr = Arrays.stream(authorize.split(",")).distinct().toArray(String[]::new);
        authorize = Arrays.toString(arr);
        return authorize.substring(1, authorize.length() - 1).replace(" ", "");
    }


    public String save(AdminRoleEntity adminRoleEntity) {
        adminRoleEntity.setAuthorize(
                adminMenuRepository.getAvailableAuthorize(adminRoleEntity.getAuthorize()));
        try {
            if (adminRoleEntity.getId() == null) DbUtils.insert(adminRoleEntity);
            else {
                if (adminRoleEntity.getId() == 1L) return "不能修改系统内置角色";
                if (DbUtils.update(adminRoleEntity) == 0) return "该角色不存在";
            }
        } catch (DuplicateKeyException ignored) {
            return "角色已存在";
        }
        return null;
    }
}
