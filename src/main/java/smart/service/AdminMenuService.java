package smart.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import smart.cache.AdminMenuCache;
import smart.entity.AdminMenuEntity;
import smart.repository.AdminMenuRepository;
import smart.util.DbUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AdminMenuService {


    @Resource
    AdminMenuRepository adminMenuRepository;

    /**
     * delete entity
     *
     * @param id id
     * @return null: success or error message
     */
    public String delete(Long id) {
        var entity = DbUtils.findByIdForWrite(id, AdminMenuEntity.class);
        if (entity == null) return null;
        if (adminMenuRepository.countByParentId(id) > 0) return "请先删除子项";
        adminMenuRepository.deleteById(id);
        adminMenuRepository.flush();
        AdminMenuCache.update();
        return null;
    }

    public List<Menu> getMenu(String[] authorize, boolean skipAuthorize) {
        List<Menu> menus = new ArrayList<>();
        // home page
        var menu = new Menu(null, new MenuMeta(null, null, "House", null, false), "/");
        menu.addChild(new Menu("main/home", new MenuMeta(true, "首页", "House", true, false), "home"));
        menus.add(menu);

        var rows = AdminMenuCache.getRows();
        // directory
        rows.stream().filter(row -> row.getParentId() == 0L).forEach(item -> {
            var menuItem = new Menu(item.getComponent(),
                    new MenuMeta(item.getCache(), item.getName(), item.getIcon(), null, item.getVisible()), item.getRoute());
            // menu
            rows.stream().filter(row -> Objects.equals(row.getParentId(), item.getId())).forEach(item1 -> {

                // check authorize
                if (skipAuthorize || ObjectUtils.containsElement(authorize, item1.getFullRoute() + "/query"))
                    menuItem.addChild(new Menu(item1.getComponent(),
                            new MenuMeta(item1.getCache(), item1.getName(), item1.getIcon(), null, item1.getVisible()), item1.getRoute()));
            });
            if (menuItem.children != null) menus.add(menuItem);
        });

        return menus;
    }


    public String save(AdminMenuEntity adminMenuEntity) {
        switch (adminMenuEntity.getType()) {
            case 1 -> {
                if (adminMenuEntity.getParentId() != 0) {
                    return "目录的上级需为空";
                }
                if (!adminMenuEntity.getRoute().startsWith("/")) {
                    return "目录路由地址必须以 / 开头";
                }
                adminMenuEntity.setComponent("");
            }
            case 2 -> {
                if (adminMenuEntity.getParentId() == 0L) return "菜单的上级不能为空";
                if (Strings.isBlank(adminMenuEntity.getComponent())) return "组件路径不能为空";
            }
            case 3 -> {
                if (adminMenuEntity.getParentId() == 0L) return "按钮的上级不能为空";
                adminMenuEntity.setIcon("");
                adminMenuEntity.setComponent("");
                adminMenuEntity.setVisible(false);
            }
        }

        if (adminMenuEntity.getParentId() != 0L) {
            if (Objects.equals(adminMenuEntity.getId(), adminMenuEntity.getParentId()))
                return "上级不能选择自己";
            var parentEntity = DbUtils.findByIdForWrite(adminMenuEntity.getParentId(), AdminMenuEntity.class);
            if (parentEntity == null) return "上级不存在";
            if (adminMenuEntity.getType() == 2 && parentEntity.getType() != 1)
                return "菜单的上级类型必须为目录";
            if (adminMenuEntity.getType() == 3 && parentEntity.getType() != 2)
                return "按钮的上级类型必须为菜单";
        }
        if (adminMenuEntity.getId() == null) DbUtils.insert(adminMenuEntity);
        else if (DbUtils.update(adminMenuEntity) == 0) return "该记录不存在";
        AdminMenuCache.update();
        return null;
    }


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Menu {
        public List<Menu> children;
        public String component;
        public MenuMeta meta;
        public String path;
        public String redirect;

        public Menu(String component, MenuMeta meta, String path) {
            this.component = component;
            this.meta = meta;
            this.path = path;
        }

        void addChild(Menu child) {
            if (children == null) children = new ArrayList<>();
            children.add(child);
        }


    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record MenuMeta(
            Boolean cache, String title,
            String icon, Boolean hideClose,
            Boolean visible
    ) {
    }
}
