package smart.storage;

import smart.config.AppConfig;
import smart.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalStorage implements Storage {

    public final static String UPLOAD_DIR = Paths.get(AppConfig.getAppDir(), "img").toString();


    @Override
    public boolean exists(String path) {
        return Files.exists(Paths.get(UPLOAD_DIR, path));
    }

    @Override
    public boolean noExists(String path) {
        return Files.notExists(Paths.get(UPLOAD_DIR, path));
    }

    @Override
    public void cleanFiles(CanClean canClean) {

    }

    @Override
    public UploadResult upload(InputStream inputStream, String path) {
        Path p = Paths.get(UPLOAD_DIR, path);
        if (Files.notExists(p)) {
            try {
                Files.createDirectories(p.getParent());
                Files.copy(inputStream, p);
            } catch (IOException e) {
                LogUtils.error(this.getClass(), "", e);
                return new UploadResult("服务器IO错误", null);
            }
        }
        return new UploadResult(null, "/img/" + path);
    }


    @Override
    public void close() {

    }
}
