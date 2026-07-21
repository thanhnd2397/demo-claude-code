package vn.thanhnd.demo.util.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Date/time parsing and timezone conversion helper. Inject this interface, not the implementation.
 */
public interface DateTimeParser {

    LocalDateTime getCurrentUTCDateTime();

    LocalDateTime getCurrentJapanDateTime();

    LocalDateTime getStartOfDay(LocalDate date);

    LocalDateTime getEndOfDay(LocalDate date);

    String format(LocalDateTime dateTime);
}
