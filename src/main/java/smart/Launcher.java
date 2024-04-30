package smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import smart.cache.GoodsCache;
import smart.config.AppConfig;
import smart.util.Helper;
import smart.util.LogUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


@DependsOn({"appConfig", "json", "security"})
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Launcher {
    public static void main(String[] args) {
        Locale.setDefault(Locale.CHINA);
        TimeZone.setDefault(TimeZone.getTimeZone(AppConfig.TIME_ZONE));
        SpringApplication.run(Launcher.class, args);
        try {
            hot();
        } catch (Exception ignore) {
        }
    }

    /**
     * 启动后，通过调用 自我访问等方式热机
     */
    @SuppressWarnings("unchecked")
    private static void hot() {
        Helper.getQRCodePng("https://hot.test", 100);
        ConfigurableEnvironment environment = AppConfig.getContext().getEnvironment();
        String addr = environment.getProperty("server.address");
        if (addr == null || addr.startsWith("0.")) {
            addr = "localhost";
        }
        int port = Helper.parseNumber(environment.getProperty("server.port"), Integer.class);
        if (port == 0) {
            port = 8080;
        }
        Set<String> links = new LinkedHashSet<>();
        links.add("/");
        links.add("/captcha");
        links.add("/cart/json");
        links.add("/favicon.ico");
        links.add("/list");
        for (var recommend : GoodsCache.getRecommend()) {
            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) recommend.get("goodsList");
            if (!goodsList.isEmpty()) {
                long goodsId = Helper.parseNumber(goodsList.getFirst().get("id"), Long.class);
                links.add(String.format("/goods/%d.html", goodsId));
                break;
            }
        }
        for (String link : links) {
            sendRequest(String.format("http://%s:%d%s", addr, port, link));
        }
    }

    private static void sendRequest(String uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        try (var client = HttpClient.newHttpClient()) {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            LogUtils.warn(Launcher.class, ex);
        }
    }
}
