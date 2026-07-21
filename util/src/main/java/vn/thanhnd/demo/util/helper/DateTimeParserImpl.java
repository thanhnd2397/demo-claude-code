package vn.thanhnd.demo.util.helper;

import org.springframework.stereotype.Component;
import vn.thanhnd.demo.util.constant.ApplicationConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeParserImpl implements DateTimeParser {

    private static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(ApplicationConstants.DATE_TIME_FORMAT);

    @Override
    public LocalDateTime getCurrentUTCDateTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime getCurrentJapanDateTime() {
        return LocalDateTime.now(JAPAN_ZONE);
    }

    @Override
    public LocalDateTime getStartOfDay(LocalDate date) {
        return date.atTime(LocalTime.MIN);
    }

    @Override
    public LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    @Override
    public String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(FORMATTER);
    }
}
