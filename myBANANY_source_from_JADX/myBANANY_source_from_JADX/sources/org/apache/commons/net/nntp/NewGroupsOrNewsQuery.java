package org.apache.commons.net.nntp;

import java.util.Calendar;

public final class NewGroupsOrNewsQuery {
    private final String __date;
    private StringBuffer __distributions = null;
    private final boolean __isGMT;
    private StringBuffer __newsgroups = null;
    private final String __time;

    public NewGroupsOrNewsQuery(Calendar date, boolean gmt) {
        this.__isGMT = gmt;
        StringBuilder buffer = new StringBuilder();
        String str = Integer.toString(date.get(1));
        int num = str.length();
        if (num >= 2) {
            buffer.append(str.substring(num - 2));
        } else {
            buffer.append("00");
        }
        str = Integer.toString(date.get(2) + 1);
        num = str.length();
        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("01");
        }
        str = Integer.toString(date.get(5));
        num = str.length();
        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("01");
        }
        this.__date = buffer.toString();
        buffer.setLength(0);
        str = Integer.toString(date.get(11));
        num = str.length();
        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("00");
        }
        str = Integer.toString(date.get(12));
        num = str.length();
        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("00");
        }
        str = Integer.toString(date.get(13));
        num = str.length();
        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("00");
        }
        this.__time = buffer.toString();
    }

    public void addNewsgroup(String newsgroup) {
        if (this.__newsgroups != null) {
            this.__newsgroups.append(',');
        } else {
            this.__newsgroups = new StringBuffer();
        }
        this.__newsgroups.append(newsgroup);
    }

    public void omitNewsgroup(String newsgroup) {
        addNewsgroup("!" + newsgroup);
    }

    public void addDistribution(String distribution) {
        if (this.__distributions != null) {
            this.__distributions.append(',');
        } else {
            this.__distributions = new StringBuffer();
        }
        this.__distributions.append(distribution);
    }

    public String getDate() {
        return this.__date;
    }

    public String getTime() {
        return this.__time;
    }

    public boolean isGMT() {
        return this.__isGMT;
    }

    public String getDistributions() {
        return this.__distributions == null ? null : this.__distributions.toString();
    }

    public String getNewsgroups() {
        return this.__newsgroups == null ? null : this.__newsgroups.toString();
    }
}
