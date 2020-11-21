package alex.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class SpecCache {
    private static JdbcTemplate jdbcTemplate;
    private static String json;

    @Autowired
    private void autowire(JdbcTemplate jdbcTemplate) {
        SpecCache.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public synchronized static void init() {
        var rows = jdbcTemplate.queryForList("select * from spec order by sort, name, note, id");
        ObjectMapper mapper = new ObjectMapper();
        rows.forEach(row -> {
            String json = (String) row.get("list");
            try {
                List<Map<String, String>> list = mapper.readValue(json, new TypeReference<>() {
                });
                row.put("list", list);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        try {
            json = mapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static String getJson() {
        return json;
    }

}
