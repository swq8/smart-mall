package smart.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import smart.config.AppProperties;
import smart.util.Helper;
import smart.util.Json;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Api {

    @Resource
    AppProperties appProperties;

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
        if (!appProperties.isDevMode()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Thread.sleep(t);
        return Integer.toString(t);
    }

    @GetMapping("test")
    public String getTest() {
        return Json.stringify(new Object());
    }
}
