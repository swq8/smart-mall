package alex.storage;

import alex.cache.SystemCache;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import java.io.InputStream;

public class OssStorage implements Storage {
    private final OSS ossClient;

    public OssStorage() {
        ossClient = new OSSClientBuilder().build(
                SystemCache.getOssEndpoint(),
                SystemCache.getOssAk(),
                SystemCache.getOssAks()
        );
    }

    @Override
    public boolean exists(String path) {
        return ossClient.doesObjectExist(SystemCache.getOssBucket(), path);
    }

    @Override
    public boolean noExists(String path) {
        return !exists(path);
    }

    @Override
    public UploadResult upload(InputStream inputStream, String path) {
        if (noExists(path)) {
            ossClient.putObject(SystemCache.getOssBucket(), path, inputStream);
        }
        return new UploadResult(null, SystemCache.getOssBucketUrl() + "/" + path);
    }

    @Override
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
