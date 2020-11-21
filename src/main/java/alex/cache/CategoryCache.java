package alex.cache;

import alex.entity.CategoryEntity;
import alex.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CategoryCache {
    private static Map<Long, List<CategoryEntity>> categoryPaths;
    private static Map<Long, List<CategoryEntity>> childrenMap;
    private static CategoryService categoryService;
    private static String jsonStr;
    private static List<Map<String, Object>> list;
    private static List<CategoryNode> nodes;
    private static List<CategoryEntity> rows;

    @PostConstruct
    public synchronized static void init() {
        rows = categoryService.findAll();
        list = initList(0, 0);
        initNodes();

        Map<Long, List<CategoryEntity>> categoryPaths1 = new HashMap<>();
        long parentId = 0;
        for (CategoryEntity row : rows) {
            List<CategoryEntity> list = new ArrayList<>();
            list.add(row);
            parentId = row.getParentId();
            while (parentId != 0) {
                var lastSize = list.size();
                if (lastSize > 5) {
                    LoggerFactory.getLogger(CategoryCache.class).error(
                            String.format("商品类别超过5级,已截断.category id: %d", list.get(list.size() - 1).getId()));
                    break;
                }
                for (var item : rows) {
                    if (parentId == item.getId()) {
                        list.add(0, item);
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
            List<CategoryEntity> list1 = rows.stream().filter(item -> item.getParentId() == row.getId()).collect(Collectors.toList());
            List<CategoryEntity> list2 = new ArrayList<>();
            List<CategoryEntity> list3 = new ArrayList<>();
            if (list1.size() > 0) {
                list.addAll(list1);
                for (var item : list1) {
                    list2.addAll(rows.stream().filter(item1 -> item1.getParentId() == item.getId()).collect(Collectors.toList()));
                }
            }
            if (list2.size() > 0) {
                list.addAll(list2);
                for (var item : list2) {
                    list3.addAll(rows.stream().filter(item2 -> item2.getParentId() == item.getId()).collect(Collectors.toList()));
                }
            }
            list.addAll(list3);
            childrenMap1.put(row.getId(), list);
        }
        childrenMap = childrenMap1;
    }

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
                if (row1.getParentId() == row.getId()) {
                    CategoryNode node1 = new CategoryNode();
                    node1.setId(row1.getId());
                    node1.setName(row1.getName());
                    node.childNodes.add(node1);
                }
            }
            nodes.add(node);
        }
        CategoryCache.nodes = nodes;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonStr = mapper.writeValueAsString(nodes);
        } catch (JsonProcessingException e) {
            jsonStr = "[]";
            e.printStackTrace();
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
            map.put("i", row.getI());
            map.put("sort", row.getSort());
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
