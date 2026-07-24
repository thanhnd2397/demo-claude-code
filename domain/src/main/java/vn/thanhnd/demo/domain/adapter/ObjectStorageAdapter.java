package vn.thanhnd.demo.domain.adapter;

import java.io.InputStream;
import java.util.List;

/**
 * Port for object storage operations (backed by MinIO in infrastructure).
 * A null/empty {@code bucket} means the implementation uses its configured default bucket.
 * {@code prefix} must not contain {@code ..}.
 */
public interface ObjectStorageAdapter {

    /**
     * Upload an object.
     *
     * @param bucket The target bucket, or null/empty for the default bucket
     * @param objectKey The object key to store under
     * @param inputStream The object content
     * @param contentLength The content length in bytes
     * @param contentType The MIME content type
     */
    void put(String bucket, String objectKey, InputStream inputStream, long contentLength, String contentType);

    /**
     * Download an object.
     *
     * @param bucket The source bucket, or null/empty for the default bucket
     * @param objectKey The object key to read
     * @return A stream over the object content (caller closes it)
     */
    InputStream get(String bucket, String objectKey);

    /**
     * Delete an object.
     *
     * @param bucket The bucket, or null/empty for the default bucket
     * @param objectKey The object key to delete
     */
    void delete(String bucket, String objectKey);

    /**
     * Check whether an object exists.
     *
     * @param bucket The bucket, or null/empty for the default bucket
     * @param objectKey The object key to check
     * @return true if the object exists
     */
    boolean exists(String bucket, String objectKey);

    /**
     * List object keys under a prefix.
     *
     * @param bucket The bucket, or null/empty for the default bucket
     * @param prefix The key prefix to list under (must not contain {@code ..})
     * @return The matching object keys; empty list if none
     */
    List<String> listObjectKeys(String bucket, String prefix);

    /**
     * List object keys under a prefix together with each object's last-modified timestamp.
     *
     * @param bucket The bucket, or null/empty for the default bucket
     * @param prefix The key prefix to list under (must not contain {@code ..})
     * @return The matching keys with last-modified timestamps; empty list if none
     */
    List<ObjectKeyWithLastModified> listObjectKeysWithLastModified(String bucket, String prefix);

    /**
     * Create a time-limited presigned download URL for an object.
     *
     * @param bucket The bucket, or null/empty for the default bucket
     * @param objectKey The object key
     * @param expirySeconds How long the URL stays valid, in seconds
     * @return The presigned URL
     */
    String getPresignedUrl(String bucket, String objectKey, int expirySeconds);
}
