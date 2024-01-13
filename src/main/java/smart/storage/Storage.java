package smart.storage;

import java.io.IOException;
import java.io.InputStream;


/**
 * file storage
 */
public interface Storage extends AutoCloseable {

    boolean exists(String path);

    boolean noExists(String path);

    /**
     * 清除没有被引用到的文件
     *
     * @param canClean 清除检测
     */
    void cleanFiles(CanClean canClean);

    /* upload file */
    UploadResult upload(InputStream inputStream, String path) throws IOException;


}
