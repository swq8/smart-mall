package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.simple.JdbcClient;
import smart.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SpecCache {

    private static JdbcClient jdbcClient;
    private static String json;

    @PostConstruct
    public synchronized static void update() {
        var rows = jdbcClient.sql("select * from t_spec order by name").query().listOfRows();

        rows.forEach(row -> {
            String json = (String) row.get("items");
            List<Map<String, String>> list = Json.parseStringMaps(json);
            row.put("items", list);
        });
        json = Json.stringify(rows);
    }

    public static String getJson() {
        return json;
    }

    @Autowired
    public void setJdbcClient(JdbcClient jdbcClient) {
        SpecCache.jdbcClient = jdbcClient;
    }
}
