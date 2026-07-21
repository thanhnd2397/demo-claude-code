package vn.thanhnd.demo.domain.adapter;

import java.time.LocalDateTime;

/**
 * A single object storage entry key paired with its last-modified timestamp.
 */
public record ObjectKeyWithLastModified(String objectKey, LocalDateTime lastModified) {
}
