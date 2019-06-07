package org.apache.commons.net.ntp;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.io.FilenameUtils;

public class TimeStamp implements Serializable, Comparable<TimeStamp> {
    public static final String NTP_DATE_FORMAT = "EEE, MMM dd yyyy HH:mm:ss.SSS";
    protected static final long msb0baseTime = 2085978496000L;
    protected static final long msb1baseTime = -2208988800000L;
    private static final long serialVersionUID = 8139806907588338737L;
    private final long ntpTime;
    private DateFormat simpleFormatter;
    private DateFormat utcFormatter;

    public TimeStamp(long ntpTime) {
        this.ntpTime = ntpTime;
    }

    public TimeStamp(String hexStamp) throws NumberFormatException {
        this.ntpTime = decodeNtpHexString(hexStamp);
    }

    public TimeStamp(Date d) {
        this.ntpTime = d == null ? 0 : toNtpTime(d.getTime());
    }

    public long ntpValue() {
        return this.ntpTime;
    }

    public long getSeconds() {
        return (this.ntpTime >>> 32) & 4294967295L;
    }

    public long getFraction() {
        return this.ntpTime & 4294967295L;
    }

    public long getTime() {
        return getTime(this.ntpTime);
    }

    public Date getDate() {
        return new Date(getTime(this.ntpTime));
    }

    public static long getTime(long ntpTimeValue) {
        long seconds = (ntpTimeValue >>> 32) & 4294967295L;
        long fraction = Math.round((1000.0d * ((double) (ntpTimeValue & 4294967295L))) / 4.294967296E9d);
        if ((seconds & 2147483648L) == 0) {
            return (msb0baseTime + (seconds * 1000)) + fraction;
        }
        return (msb1baseTime + (seconds * 1000)) + fraction;
    }

    public static TimeStamp getNtpTime(long date) {
        return new TimeStamp(toNtpTime(date));
    }

    public static TimeStamp getCurrentTime() {
        return getNtpTime(System.currentTimeMillis());
    }

    protected static long decodeNtpHexString(String hexString) throws NumberFormatException {
        if (hexString == null) {
            throw new NumberFormatException("null");
        }
        int ind = hexString.indexOf(46);
        if (ind != -1) {
            return (Long.parseLong(hexString.substring(0, ind), 16) << 32) | Long.parseLong(hexString.substring(ind + 1), 16);
        }
        if (hexString.length() == 0) {
            return 0;
        }
        return Long.parseLong(hexString, 16) << 32;
    }

    public static TimeStamp parseNtpString(String s) throws NumberFormatException {
        return new TimeStamp(decodeNtpHexString(s));
    }

    protected static long toNtpTime(long t) {
        long baseTime;
        boolean useBase1 = t < msb0baseTime;
        if (useBase1) {
            baseTime = t - msb1baseTime;
        } else {
            baseTime = t - msb0baseTime;
        }
        long seconds = baseTime / 1000;
        long fraction = ((baseTime % 1000) * 4294967296L) / 1000;
        if (useBase1) {
            seconds |= 2147483648L;
        }
        return (seconds << 32) | fraction;
    }

    public int hashCode() {
        return (int) (this.ntpTime ^ (this.ntpTime >>> 32));
    }

    public boolean equals(Object obj) {
        if ((obj instanceof TimeStamp) && this.ntpTime == ((TimeStamp) obj).ntpValue()) {
            return true;
        }
        return false;
    }

    public String toString() {
        return toString(this.ntpTime);
    }

    private static void appendHexString(StringBuilder buf, long l) {
        String s = Long.toHexString(l);
        for (int i = s.length(); i < 8; i++) {
            buf.append('0');
        }
        buf.append(s);
    }

    public static String toString(long ntpTime) {
        StringBuilder buf = new StringBuilder();
        appendHexString(buf, (ntpTime >>> 32) & 4294967295L);
        buf.append(FilenameUtils.EXTENSION_SEPARATOR);
        appendHexString(buf, ntpTime & 4294967295L);
        return buf.toString();
    }

    public String toDateString() {
        if (this.simpleFormatter == null) {
            this.simpleFormatter = new SimpleDateFormat(NTP_DATE_FORMAT, Locale.US);
            this.simpleFormatter.setTimeZone(TimeZone.getDefault());
        }
        return this.simpleFormatter.format(getDate());
    }

    public String toUTCString() {
        if (this.utcFormatter == null) {
            this.utcFormatter = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss.SSS 'UTC'", Locale.US);
            this.utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return this.utcFormatter.format(getDate());
    }

    public int compareTo(TimeStamp anotherTimeStamp) {
        long thisVal = this.ntpTime;
        long anotherVal = anotherTimeStamp.ntpTime;
        if (thisVal < anotherVal) {
            return -1;
        }
        return thisVal == anotherVal ? 0 : 1;
    }
}
