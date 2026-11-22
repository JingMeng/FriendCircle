package razerdp.github.com.lib.utils;

import androidx.annotation.StringDef;
import android.text.TextUtils;
import android.util.Log;

import com.socks.library.KLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import razerdp.github.com.lib.R;

/**
 * Created by 澶х伅娉?on 2016/11/1.
 * <p>
 * 鏃堕棿宸ュ叿
 */

public class TimeUtil {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({YYYYMMDD, YYYYMM, HHMM, YYYYMD, YYYYMMDDHHMM, YYYYMMDDHHMMSS, YYYYMMDDHHMM_EX, MMDDHHMM_CHINESE_EX, YYYYMMLNDDHHMM, MMDDHHMM_CHINESE, YYYYMMDD_SLASH, YYYYMM_SLASH})
    public @interface FormateType {
    }

    public final static String YYYYMMDD = "yyyy-MM-dd";
    public final static String YYYYMMDDHHMM_EX = "yyyy_MM_dd_hh_mm";
    public final static String YYYYMD = "yyyy-M-d";
    public final static String YYYYMM = "yyyy-MM";
    public final static String HHMM = "hh:mm";
    public final static String YYYYMMDDHHMM = "yyyy-MM-dd hh:mm";
    public final static String YYYYMMLNDDHHMM = "yyyy-MM-dd\nhh:mm";
    public final static String YYYYMMDDHHMMSS = "yyyy-MM-dd hh:mm:ss";
    public final static String MMDDHHMM_CHINESE = "MM鏈坉d鏃hh鏃秏m鍒?;
    public final static String MMDDHHMM_CHINESE_EX = "MM鏈坉d鏃hh_mm";
    public final static String YYYYMMDD_SLASH = "yyyy/MM/dd";
    public final static String YYYYMM_SLASH = "yyyy/MM";

    private static SimpleDateFormat sdf = new SimpleDateFormat();
    public static final int YEAR = 365 * 24 * 60 * 60;// 骞?    public static final int MONTH = 30 * 24 * 60 * 60;// 鏈?    public static final int DAY = 24 * 60 * 60;// 澶?    public static final int HOUR = 60 * 60;// 灏忔椂
    public static final int MINUTE = 60;// 鍒嗛挓

    public static String getTimeString(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeGap = (currentTime - timestamp) / 1000;// 涓庣幇鍦ㄦ椂闂寸浉宸鏁?        String timeStr = null;
        if (timeGap > YEAR) {
            timeStr = formatTimeFromResource(R.string.format_time_year, (int) (timeGap / YEAR));
        } else if (timeGap > MONTH) {
            timeStr = formatTimeFromResource(R.string.format_time_month, (int) (timeGap / MONTH));

        } else if (timeGap > DAY) {// 1澶╀互涓?            timeStr = formatTimeFromResource(R.string.format_time_day, (int) (timeGap / DAY));
        } else if (timeGap > HOUR) {// 1灏忔椂-24灏忔椂
            timeStr = formatTimeFromResource(R.string.format_time_hour, (int) (timeGap / HOUR));
        } else if (timeGap > MINUTE) {// 1鍒嗛挓-59鍒嗛挓
            timeStr = formatTimeFromResource(R.string.format_time_minute, (int) (timeGap / MINUTE));
        } else {// 1绉掗挓-59绉掗挓
            timeStr = StringUtil.getResourceString(R.string.format_time_sec);
        }
        return timeStr;
    }

    public static String getTimeStringFromBmob(String time) {
        //鏍煎紡:2016-10-28 18:48:23
        try {
            Date date = dataFormate.parse(time);
            return getTimeString(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return getTimeString(System.currentTimeMillis());
        }
    }

    private static String formatTimeFromResource(int resource, int time) {
        return StringUtil.getResourceStringAndFormat(resource, time);
    }


    private static SimpleDateFormat dataFormate = new SimpleDateFormat(YYYYMMDDHHMMSS, Locale.getDefault());
    private static SimpleDateFormat yyyymmddFormate = new SimpleDateFormat(YYYYMMDD, Locale.getDefault());

    /**
     * 鑾峰彇鏈潵 绗?past 澶╃殑鏃ユ湡
     *
     * @param past
     * @return
     */
    public static String getFetureDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        Log.e(null, result);
        return result;
    }

    /**
     * 鑾峰彇鏈潵 绗?past 澶╃殑鏃ユ湡
     *
     * @param past
     * @return
     */
    public static String getFetureDate(int past, String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        String result = format.format(today);
        return result;
    }

    public static String getWhichDay(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        String result = format.format(today);
        return result;
    }

    public static String formatDate(String date, String dateFormat) throws ParseException {
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTime = inputSdf.parse(date);
        SimpleDateFormat ouputSdf = new SimpleDateFormat(dateFormat);
        return ouputSdf.format(dateTime);
    }

    /**
     * 鑾峰彇鏈潵 绗?past 澶╃殑鏃ユ湡
     *
     * @param date 20170412
     * @return 04-12
     */
    public static String formatDate(String date) {
        if (!TextUtils.isEmpty(date) && date.length() == 8) {
            return new StringBuilder(date.substring(4)).insert(2, "-").toString();
        }
        return date;
    }

    /**
     * 鏃堕棿鎴宠浆鏍煎紡鍖栨棩鏈?     *
     * @param timestamp 鍗曚綅姣
     * @param format    鏃ユ湡鏍煎紡
     * @return
     */
    public static String longToTimeStr(long timestamp, @FormateType String format) {
        return transferLongToDate(timestamp, format);
    }

    /**
     * 鏃堕棿鎴宠浆鏍煎紡鍖栨棩鏈?     *
     * @param timestamp 鍗曚綅姣
     * @param format    鏃ユ湡鏍煎紡
     * @return
     */
    private static String transferLongToDate(long timestamp, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = new Date(timestamp);
            return sdf.format(date);
        } catch (Exception e) {
            KLog.e(e);
            return "null";
        }
    }

    public static String dateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 鏍煎紡鍖栨棩鏈熻浆鏃堕棿鎴?     *
     * @param format 鏃ユ湡鏍煎紡
     * @return
     */
    public static long strToTimestamp(String date, String format) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(format).parse(date).getTime() / 1000;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 姣旇緝timeTwo鏄惁姣擳imeOne鏅氳秴杩嘾istanceMinute鍒嗛挓
     *
     * @param TimeOne        鍗曚綅 unixtime
     * @param timeTwo        鍗曚綅 unixtime
     * @param distanceMinute 鍗曚綅鍒嗛挓
     * @return
     */
    public static boolean isGreaterThan(long TimeOne, long timeTwo, int distanceMinute) {
        long timeDifference = timeTwo - TimeOne;// 涓よ€呯浉璺濈殑绉掓暟
        long oneMinute = 60;
        if (timeDifference > distanceMinute * oneMinute) {
            return true;
        } else {
            return false;
        }
    }

    public static String transferDateFromate(String originalDate, @FormateType String oldFormate, @FormateType String newFormate) {
        long time = stringToTimeStamp(originalDate, oldFormate);
        return transferDateFromate(time, newFormate);
    }

    public static String transferDateFromate(long originalDate, @FormateType String newFormate) {
        return longToTimeStr(originalDate, newFormate);
    }


    public static String formatLongToTimeStr(Long l) {
        String str = "";
        int hour = 0;
        int minute = 0;
        int second = 0;
        second = l.intValue() / 1000;
        if (second > 60) {
            minute = second / 60;
            second = second % 60;
        }

        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }

        String strtime = "";
        if (hour != 0) {
            strtime = hour + ":" + minute + ":" + second;
        } else {
            strtime = minute + ":" + (second >= 10 ? second : "0" + second);
        }
        return strtime;
    }

    public static long stringToTimeStamp(String timeString, @FormateType String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(timeString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long stringToTimeStamp(String timeString, @FormateType String format, TimeZone timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        sdf.setTimeZone(timeZone);
        try {
            return sdf.parse(timeString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isToday(long timeInMilis) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timeInMilis);
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == time.get(Calendar.DATE)) {
            return true;
        } else {
            return false;
        }
    }

    public static int getSubDay(long start, long end) {
        int result = Math.round((end - start) / (1000 * DAY));
        return result < 0 ? -1 : result;
    }



    public static String getWeek(String pTime) {
        String week = "";
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(yyyymmddFormate.parse(pTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            week = "鍛ㄦ棩";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            week = "鍛ㄤ竴";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            week = "鍛ㄤ簩";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            week = "鍛ㄤ笁";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            week = "鍛ㄥ洓";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            week = "鍛ㄤ簲";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            week = "鍛ㄥ叚";
        }
        return week;
    }

    public static long getNextTime(long curTime, long time) {
        return ((curTime / 1000) + time) * 1000;
    }

}

