package smart.cache;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smart.entity.CategoryEntity;
import smart.util.Json;
import smart.service.CategoryService;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 商品类别缓存
 */
@Component
public class CategoryCache {
    private static Map<Long, List<CategoryEntity>> categoryPaths;
    private static Map<Long, List<CategoryEntity>> childrenMap;
    private static CategoryService categoryService;
    private static String jsonStr;
    private static List<Map<String, Object>> list;
    private static List<CategoryNode> nodes;
    private static List<CategoryEntity> rows;

    /**
     * 初始化
     */
    @PostConstruct
    public synchronized static void update() {
        rows = categoryService.findAll();
        list = initList(0, 0);
        initNodes();

        Map<Long, List<CategoryEntity>> categoryPaths1 = new HashMap<>();
        long parentId;
        for (CategoryEntity row : rows) {
            List<CategoryEntity> list = new ArrayList<>();
            list.add(row);
            parentId = row.getParentId();
            while (parentId != 0) {
                var lastSize = list.size();
                if (lastSize > 5) {
                    LoggerFactory.getLogger(CategoryCache.class).error(
                            String.format("商品类别超过5级,已截断.category id: %d", list.getLast().getId()));
                    break;
                }
                for (var item : rows) {
                    if (parentId == item.getId()) {
                        list.addFirst(item);
                        parentId = item.getParentId();
                        break;
                    }
                }
                if (lastSize == list.size()) {
                    break;
                }

            }
            categoryPaths1.put(row.getId(), list);
        }
        categoryPaths = categoryPaths1;


        Map<Long, List<CategoryEntity>> childrenMap1 = new HashMap<>();
        for (var row : rows) {
            List<CategoryEntity> list = new ArrayList<>();
            List<CategoryEntity> list1 = rows.stream().filter(item -> Objects.equals(item.getParentId(), row.getId())).toList();
            List<CategoryEntity> list2 = new ArrayList<>();
            List<CategoryEntity> list3 = new ArrayList<>();
            if (!list1.isEmpty()) {
                list.addAll(list1);
                for (var item : list1) {
                    list2.addAll(rows.stream().filter(item1 -> Objects.equals(item1.getParentId(), item.getId())).toList());
                }
            }
            if (!list2.isEmpty()) {
                list.addAll(list2);
                for (var item : list2) {
                    list3.addAll(rows.stream().filter(item2 -> Objects.equals(item2.getParentId(), item.getId())).toList());
                }
            }
            list.addAll(list3);
            childrenMap1.put(row.getId(), list);
        }
        childrenMap = childrenMap1;
    }

    /**
     * 初始化节点
     */
    private static void initNodes() {
        var nodes = new LinkedList<CategoryNode>();
        for (var row : rows) {
            if (row.getParentId() > 0L) {
                continue;
            }
            CategoryNode node = new CategoryNode();
            node.setId(row.getId());
            node.setName(row.getName());
            for (var row1 : rows) {
                if (Objects.equals(row1.getParentId(), row.getId())) {
                    CategoryNode node1 = new CategoryNode();
                    node1.setId(row1.getId());
                    node1.setName(row1.getName());
                    node.childNodes.add(node1);
                }
            }
            nodes.add(node);
        }
        CategoryCache.nodes = nodes;
        jsonStr = Json.stringify(nodes);
        if (jsonStr == null) {
            jsonStr = "[]";
        }
    }

    private static List<Map<String, Object>> initList(long parentId, int depth) {
        List<Map<String, Object>> list = new LinkedList<>();
        String strPad = "-".repeat(depth * 5);
        if (depth++ > 0) {
            strPad = "|" + strPad;
        }
        for (var row : rows.stream().filter(row -> row.getParentId() == parentId).toArray(CategoryEntity[]::new)) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row.getId());
            map.put("name", strPad + row.getName());
            map.put("homepage", row.getHomepage());
            map.put("orderNum", row.getOrderNum());
            map.put("parentId", row.getParentId());
            list.add(map);
            list.addAll(initList(row.getId(), depth));
        }
        return list;
    }

    public static List<CategoryEntity> getCategoryPath(long categoryId) {
        return categoryPaths.get(categoryId);
    }

    public static List<CategoryEntity> getChildren(long categoryId) {
        return childrenMap.get(categoryId);
    }

    public static String getJsonStr() {
        return jsonStr;
    }

    public static List<Map<String, Object>> getList() {
        return list;
    }

    public static List<CategoryNode> getNodes() {
        return nodes;
    }

    public static CategoryEntity[] getEntitiesByParentId(long id) {
        return rows.stream().filter(row -> row.getParentId() == id).toArray(CategoryEntity[]::new);
    }

    public static CategoryEntity getEntityById(long id) {
        return rows.stream().filter(row -> row.getId() == id).findFirst().orElse(null);
    }

    public static List<CategoryEntity> getEntities() {
        return rows;
    }

    @Autowired
    private void autowire(CategoryService service) {
        categoryService = service;
    }

    /**
     * 商品类别单个节点
     */
    public static class CategoryNode {
        private final List<CategoryNode> childNodes = new LinkedList<>();
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<CategoryNode> getChildNodes() {
            return childNodes;
        }
    }
}
