package smart.controller.adminApi.system;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smart.auth.Authorize;
import smart.config.AppConfig;
import smart.lib.ApiJsonResult;
import smart.service.SystemService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController(value = "adminApi/system/info")
@RequestMapping(path = "/adminApi/system/info", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Info {

    @Resource
    SystemService systemService;

    @PostMapping("apiList")
    public ApiJsonResult apiList() throws ClassNotFoundException {
        List<String> apiList = new ArrayList<>();
        var names = AppConfig.getContext().getBeanNamesForAnnotation(RestController.class);
        for (var name : names) {
            var bean = AppConfig.getContext().getBean(name);
            var className = bean.getClass().getName().split("\\$\\$")[0];
            var clazz = Class.forName(className);
            var mapping = clazz.getAnnotation(RequestMapping.class);
            if (mapping == null || mapping.path().length == 0) {
                continue;
            }
            var path = mapping.path()[0];
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (!path.startsWith("/adminApi")) {
                continue;
            }
            if (!path.endsWith("/")) {
                path += "/";
            }

            for (var method : clazz.getMethods()) {
                var postMapping = method.getAnnotation(PostMapping.class);
                if (postMapping == null) {
                    continue;
                }
                String action = null;
                if (postMapping.value().length > 0) {
                    action = postMapping.value()[0];
                } else if (postMapping.path().length > 0) {
                    action = postMapping.path()[0];
                }
                if (action == null) {
                    continue;
                }
                if (action.isEmpty()) {
                    apiList.add(path.substring(0, path.length() - 1));
                } else {
                    apiList.add(path + action);
                }
            }
        }
        Collections.sort(apiList);

        return ApiJsonResult.success().putDataItem("rows", apiList);
    }

    @Authorize("/system/info/query")
    @PostMapping(value = "get")
    public ApiJsonResult get() {
        return ApiJsonResult.success("").putDataItem("record", systemService.getInfo());
    }


}
