package vn.thanhnd.demo.domain.utils;

import java.util.List;

/**
 * Framework-free helper methods usable from domain models.
 */
public final class DomainUtils {

    private DomainUtils() {
    }

    public static <T> T coalesce(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static <T> List<T> coalesceList(List<T> value, List<T> defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
