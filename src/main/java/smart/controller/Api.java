package smart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import smart.entity.AdminLogEntity;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Json;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Api {

    /**
     * get client ip, port and request header
     *
     * @param request http request
     * @return json
     */
    @GetMapping(path = "header")
    public String getHeader(HttpServletRequest request) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x-ip", Helper.getClientIp(request));
        var names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            var name = names.nextElement();
            StringBuilder val = new StringBuilder();
            var vars = request.getHeaders(name);
            while (vars.hasMoreElements()) {
                val.append(vars.nextElement()).append(", ");
            }
            if (val.length() > 2) {
                val.delete(val.length() - 2, val.length());
            }
            map.put(name, val.toString());
        }
        return Json.stringify(map);
    }

    @GetMapping(path = "now")
    public String getNow() {
        Map<String, Object> map = new HashMap<>();
        map.put("now", new Date().toString());
        return Json.stringify(map);
    }

    @PostMapping(path = "post")
    public String postPost(@RequestBody String str) {
        return str;
    }


    @GetMapping("sleep")
    public String getSleep(@RequestParam(defaultValue = "1") int t) throws InterruptedException {
        Thread.sleep(t);
        return Integer.toString(t);
    }

    @GetMapping("test")
    public String getTest(String idStr) {
        AdminLogEntity entity = new AdminLogEntity();
        entity.setTitle("title");
        entity.setTime( new Timestamp(System.currentTimeMillis()));
        var json = Json.stringify(entity);
        var obj = Json.parse(json, AdminLogEntity.class);



        return Json.stringify(Map.of("json", json,
                "obj", Json.stringify(obj)));
    }

    public static class Pojo {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
