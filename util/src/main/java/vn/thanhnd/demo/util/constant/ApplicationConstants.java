package vn.thanhnd.demo.util.constant;

/**
 * Shared application-wide constants: cache keys, date/time formats, HTTP content types.
 * Entity-specific cache keys are added here as domain use cases are implemented.
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_CSV = "text/csv; charset=UTF-8";
}
