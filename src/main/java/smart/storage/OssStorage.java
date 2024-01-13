package smart.storage;

import smart.cache.SystemCache;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;

import java.io.InputStream;
import java.util.List;

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
    public void cleanFiles(CanClean canClean) {
        final int maxKeys = 100;
        String nextMarker = null;
        ObjectListing objectListing;

        do {
            objectListing = ossClient.listObjects(
                    new ListObjectsRequest(SystemCache.getOssBucket()).withMarker(nextMarker).withMaxKeys(maxKeys));

            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
            for (OSSObjectSummary s : sums) {
                // 1小时内创建的文件不删除
                if (System.currentTimeMillis() - s.getLastModified().getTime() < 3600000) {
                    continue;
                }
                if (canClean.canClean(s.getKey())) {
                    System.out.println("delete:\t" + s.getKey());
                    // 删除文件。如需删除文件夹，请将ObjectName设置为对应的文件夹名称。如果文件夹非空，则需要将文件夹下的所有object删除后才能删除该文件夹。
                    ossClient.deleteObject(SystemCache.getOssBucket(), s.getKey());
                }


            }
            nextMarker = objectListing.getNextMarker();

        } while (objectListing.isTruncated());
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
