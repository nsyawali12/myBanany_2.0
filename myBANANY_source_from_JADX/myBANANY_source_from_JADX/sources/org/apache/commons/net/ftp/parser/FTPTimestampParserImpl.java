package org.apache.commons.net.ftp.parser;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;

public class FTPTimestampParserImpl implements FTPTimestampParser, Configurable {
    private static final int[] CALENDAR_UNITS = new int[]{14, 13, 12, 11, 5, 2, 1};
    private SimpleDateFormat defaultDateFormat;
    private int defaultDateSmallestUnitIndex;
    private boolean lenientFutureDates = false;
    private SimpleDateFormat recentDateFormat;
    private int recentDateSmallestUnitIndex;

    private static int getEntry(SimpleDateFormat dateFormat) {
        if (dateFormat == null) {
            return 0;
        }
        String FORMAT_CHARS = "SsmHdM";
        String pattern = dateFormat.toPattern();
        for (char ch : "SsmHdM".toCharArray()) {
            if (pattern.indexOf(ch) != -1) {
                switch (ch) {
                    case 'H':
                        return indexOf(11);
                    case 'M':
                        return indexOf(2);
                    case 'S':
                        return indexOf(14);
                    case 'd':
                        return indexOf(5);
                    case 'm':
                        return indexOf(12);
                    case 's':
                        return indexOf(13);
                    default:
                        break;
                }
            }
        }
        return 0;
    }

    private static int indexOf(int calendarUnit) {
        for (int i = 0; i < CALENDAR_UNITS.length; i++) {
            if (calendarUnit == CALENDAR_UNITS[i]) {
                return i;
            }
        }
        return 0;
    }

    private static void setPrecision(int index, Calendar working) {
        if (index > 0) {
            int field = CALENDAR_UNITS[index - 1];
            if (working.get(field) == 0) {
                working.clear(field);
            }
        }
    }

    public FTPTimestampParserImpl() {
        setDefaultDateFormat(FTPTimestampParser.DEFAULT_SDF, null);
        setRecentDateFormat(FTPTimestampParser.DEFAULT_RECENT_SDF, null);
    }

    public Calendar parseTimestamp(String timestampStr) throws ParseException {
        return parseTimestamp(timestampStr, Calendar.getInstance());
    }

    public Calendar parseTimestamp(String timestampStr, Calendar serverTime) throws ParseException {
        ParsePosition pp;
        Date parsed;
        Calendar working = (Calendar) serverTime.clone();
        working.setTimeZone(getServerTimeZone());
        if (this.recentDateFormat != null) {
            Calendar now = (Calendar) serverTime.clone();
            now.setTimeZone(getServerTimeZone());
            if (this.lenientFutureDates) {
                now.add(5, 1);
            }
            String timeStampStrPlusYear = timestampStr + " " + Integer.toString(now.get(1));
            SimpleDateFormat hackFormatter = new SimpleDateFormat(this.recentDateFormat.toPattern() + " yyyy", this.recentDateFormat.getDateFormatSymbols());
            hackFormatter.setLenient(false);
            hackFormatter.setTimeZone(this.recentDateFormat.getTimeZone());
            pp = new ParsePosition(0);
            parsed = hackFormatter.parse(timeStampStrPlusYear, pp);
            if (parsed != null && pp.getIndex() == timeStampStrPlusYear.length()) {
                working.setTime(parsed);
                if (working.after(now)) {
                    working.add(1, -1);
                }
                setPrecision(this.recentDateSmallestUnitIndex, working);
                return working;
            }
        }
        pp = new ParsePosition(0);
        parsed = this.defaultDateFormat.parse(timestampStr, pp);
        if (parsed == null || pp.getIndex() != timestampStr.length()) {
            throw new ParseException("Timestamp '" + timestampStr + "' could not be parsed using a server time of " + serverTime.getTime().toString(), pp.getErrorIndex());
        }
        working.setTime(parsed);
        setPrecision(this.defaultDateSmallestUnitIndex, working);
        return working;
    }

    public SimpleDateFormat getDefaultDateFormat() {
        return this.defaultDateFormat;
    }

    public String getDefaultDateFormatString() {
        return this.defaultDateFormat.toPattern();
    }

    private void setDefaultDateFormat(String format, DateFormatSymbols dfs) {
        if (format != null) {
            if (dfs != null) {
                this.defaultDateFormat = new SimpleDateFormat(format, dfs);
            } else {
                this.defaultDateFormat = new SimpleDateFormat(format);
            }
            this.defaultDateFormat.setLenient(false);
        } else {
            this.defaultDateFormat = null;
        }
        this.defaultDateSmallestUnitIndex = getEntry(this.defaultDateFormat);
    }

    public SimpleDateFormat getRecentDateFormat() {
        return this.recentDateFormat;
    }

    public String getRecentDateFormatString() {
        return this.recentDateFormat.toPattern();
    }

    private void setRecentDateFormat(String format, DateFormatSymbols dfs) {
        if (format != null) {
            if (dfs != null) {
                this.recentDateFormat = new SimpleDateFormat(format, dfs);
            } else {
                this.recentDateFormat = new SimpleDateFormat(format);
            }
            this.recentDateFormat.setLenient(false);
        } else {
            this.recentDateFormat = null;
        }
        this.recentDateSmallestUnitIndex = getEntry(this.recentDateFormat);
    }

    public String[] getShortMonths() {
        return this.defaultDateFormat.getDateFormatSymbols().getShortMonths();
    }

    public TimeZone getServerTimeZone() {
        return this.defaultDateFormat.getTimeZone();
    }

    private void setServerTimeZone(String serverTimeZoneId) {
        TimeZone serverTimeZone = TimeZone.getDefault();
        if (serverTimeZoneId != null) {
            serverTimeZone = TimeZone.getTimeZone(serverTimeZoneId);
        }
        this.defaultDateFormat.setTimeZone(serverTimeZone);
        if (this.recentDateFormat != null) {
            this.recentDateFormat.setTimeZone(serverTimeZone);
        }
    }

    public void configure(FTPClientConfig config) {
        DateFormatSymbols dfs;
        String languageCode = config.getServerLanguageCode();
        String shortmonths = config.getShortMonthNames();
        if (shortmonths != null) {
            dfs = FTPClientConfig.getDateFormatSymbols(shortmonths);
        } else if (languageCode != null) {
            dfs = FTPClientConfig.lookupDateFormatSymbols(languageCode);
        } else {
            dfs = FTPClientConfig.lookupDateFormatSymbols("en");
        }
        setRecentDateFormat(config.getRecentDateFormatStr(), dfs);
        String defaultFormatString = config.getDefaultDateFormatStr();
        if (defaultFormatString == null) {
            throw new IllegalArgumentException("defaultFormatString cannot be null");
        }
        setDefaultDateFormat(defaultFormatString, dfs);
        setServerTimeZone(config.getServerTimeZoneId());
        this.lenientFutureDates = config.isLenientFutureDates();
    }

    boolean isLenientFutureDates() {
        return this.lenientFutureDates;
    }

    void setLenientFutureDates(boolean lenientFutureDates) {
        this.lenientFutureDates = lenientFutureDates;
    }
}
