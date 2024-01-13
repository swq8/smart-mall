package smart.storage;

import smart.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalStorage implements Storage {

    public final static String UPLOAD_DIR = AppConfig.getAppDir() + "img" + File.separator;


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
                e.printStackTrace();
                return new UploadResult("服务器IO错误", null);
            }
        }
        return new UploadResult(null, "/img/" + path);
    }


    @Override
    public void close() {

    }
}
