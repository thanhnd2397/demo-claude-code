package vn.thanhnd.demo.util.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Date/time parsing and timezone conversion helper. Inject this interface, not the implementation.
 */
public interface DateTimeParser {

    /**
     * Get the current date-time in UTC.
     *
     * @return The current UTC date-time
     */
    LocalDateTime getCurrentUTCDateTime();

    /**
     * Get the current date-time in the Asia/Tokyo timezone.
     *
     * @return The current Japan date-time
     */
    LocalDateTime getCurrentJapanDateTime();

    /**
     * Get the first instant of the given date (00:00:00).
     *
     * @param date The date
     * @return The date at start of day
     */
    LocalDateTime getStartOfDay(LocalDate date);

    /**
     * Get the last instant of the given date (23:59:59.999999999).
     *
     * @param date The date
     * @return The date at end of day
     */
    LocalDateTime getEndOfDay(LocalDate date);

    /**
     * Format a date-time using the application's standard pattern.
     *
     * @param dateTime The date-time to format
     * @return The formatted string
     */
    String format(LocalDateTime dateTime);
}
