package smart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@SpringBootTest
public class JsonTest {
    @Test
    public void test() {
        var obj = new TestObject();
        obj.date = new Date();
        obj.localDateTime = LocalDateTime.now();
        obj.timestamp = new Timestamp(System.currentTimeMillis());
        obj.setMap(Map.of("name", "test"));
        var json = Json.stringify(obj);
        obj = Json.parse(json, TestObject.class);
        var json1 = Json.stringify(obj);
        Assertions.assertEquals(json, json1);

    }

    static class TestObject {
        Date date;
        LocalDateTime localDateTime;
        Timestamp timestamp;
        Map<String, String> map;

        public Date getDate() {
            return date;
        }

        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }
    }
}
