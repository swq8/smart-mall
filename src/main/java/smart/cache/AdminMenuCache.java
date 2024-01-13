package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smart.entity.AdminMenuEntity;
import smart.repository.AdminMenuRepository;
import smart.util.Helper;

import java.util.*;

@Component
public class AdminMenuCache {
    private static AdminMenuRepository adminMenuRepository;
    private static List<AdminMenuEntity> rows;

    @PostConstruct
    public synchronized static void update() {
        rows = adminMenuRepository.findAllByOrderByOrderNumAscIdAsc();
        for (var row : rows) {
            if (row.getParentId() == 0L) {
                row.setFullRoute(row.getRoute());
                recursion(row);
            }
        }
    }

    private static void recursion(AdminMenuEntity entity) {
        for (var row : rows) {
            if (Objects.equals(row.getParentId(), entity.getId())) {
                row.setFullRoute(entity.getFullRoute() + "/" + row.getRoute());
                recursion(row);
            }
        }
    }

    public static String[] getEnabledAuthorizeByIds(String ids) {
        Set<String> set = new TreeSet<>();
        for (var idStr : ids.split(",")) {
            var id = Helper.parseNumber(idStr, Long.class);
            var row = getRowById(id);
            if (row != null && row.getType() == 3 && row.getEnable()) set.add(row.getFullRoute());
        }
        return set.toArray(String[]::new);
    }

    public static AdminMenuEntity getRowById(Long id) {
        for (var row : rows) {
            if (Objects.equals(row.getId(), id)) return row;
        }
        return null;
    }

    public static List<AdminMenuEntity> getRows() {
        return rows;
    }

    @Autowired
    private void autowire(AdminMenuRepository adminMenuRepository1) {
        adminMenuRepository = adminMenuRepository1;
    }

}
