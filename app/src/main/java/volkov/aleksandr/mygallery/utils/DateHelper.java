package volkov.aleksandr.mygallery.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Comparator;

/**
 * Created by Alexandr Volkov on 10.04.2018.
 */

public class DateHelper {
    public static final DateTimeFormatter LONG_DATE = DateTimeFormat.forPattern("d MMMM yyyy");

    public static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

    public static DateTime parseTime(String time) {
        return DATE_FORMAT.parseDateTime(time);
    }

    public static final Comparator<DateTime> DATE_COMPARATOR = (o1, o2) -> {
        if (o1.getYear() == o2.getYear()) {
            if (o1.getMonthOfYear() == o2.getMonthOfYear()) {
                return o1.getDayOfMonth() - o2.getDayOfMonth();
            } else {
                return o1.getMonthOfYear() - o2.getMonthOfYear();
            }
        } else {
            return o1.getYear() - o2.getYear();
        }
    };


}
