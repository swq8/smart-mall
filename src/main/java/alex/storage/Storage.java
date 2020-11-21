package alex.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * file storage
 */
public interface Storage extends AutoCloseable {

    public boolean exists(String path);
    public boolean noExists(String path);

    /* upload file */
    public UploadResult upload(InputStream inputStream, String path) throws IOException;
}
