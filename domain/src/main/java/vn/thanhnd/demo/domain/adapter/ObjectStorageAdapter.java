package vn.thanhnd.demo.domain.adapter;

import java.io.InputStream;
import java.util.List;

/**
 * Port for object storage operations (backed by MinIO in infrastructure).
 * A null/empty {@code bucket} means the implementation uses its configured default bucket.
 * {@code prefix} must not contain {@code ..}.
 */
public interface ObjectStorageAdapter {

    void put(String bucket, String objectKey, InputStream inputStream, long contentLength, String contentType);

    InputStream get(String bucket, String objectKey);

    void delete(String bucket, String objectKey);

    boolean exists(String bucket, String objectKey);

    List<String> listObjectKeys(String bucket, String prefix);

    List<ObjectKeyWithLastModified> listObjectKeysWithLastModified(String bucket, String prefix);

    String getPresignedUrl(String bucket, String objectKey, int expirySeconds);
}
