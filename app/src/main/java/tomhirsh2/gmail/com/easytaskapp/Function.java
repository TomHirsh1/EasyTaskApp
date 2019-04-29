package tomhirsh2.gmail.com.easytaskapp;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.time.format.DecimalStyle;

/**
 * Created by Ferdousur Rahman Sarker on 3/21/2018.
 */

public class Function {

    public static String Epoch2DateString(String epochSeconds, String formatString) {
        Date updatedate = new Date(Long.parseLong(epochSeconds));
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        return format.format(updatedate);
    }

    public static Calendar Epoch2Calender(String epochSeconds) {
        Date updatedate = new Date(Long.parseLong(epochSeconds));
        Calendar cal = Calendar.getInstance();
        cal.setTime(updatedate);
        return cal;
    }

    public static String Epoch2TimeString(String epochSeconds, String formatString) {
        Date updatetime = new Date(Long.parseLong(epochSeconds));
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        return format.format(updatetime);
    }
}
