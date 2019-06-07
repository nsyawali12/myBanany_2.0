package org.apache.commons.net.smtp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.io.IOUtils;

public class SimpleSMTPHeader {
    private StringBuffer __cc;
    private final String __from;
    private final StringBuffer __headerFields;
    private final String __subject;
    private final String __to;
    private boolean hasHeaderDate;

    public SimpleSMTPHeader(String from, String to, String subject) {
        if (from == null) {
            throw new IllegalArgumentException("From cannot be null");
        }
        this.__to = to;
        this.__from = from;
        this.__subject = subject;
        this.__headerFields = new StringBuffer();
        this.__cc = null;
    }

    public void addHeaderField(String headerField, String value) {
        if (!this.hasHeaderDate && "Date".equals(headerField)) {
            this.hasHeaderDate = true;
        }
        this.__headerFields.append(headerField);
        this.__headerFields.append(": ");
        this.__headerFields.append(value);
        this.__headerFields.append('\n');
    }

    public void addCC(String address) {
        if (this.__cc == null) {
            this.__cc = new StringBuffer();
        } else {
            this.__cc.append(", ");
        }
        this.__cc.append(address);
    }

    public String toString() {
        StringBuilder header = new StringBuilder();
        String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        if (!this.hasHeaderDate) {
            addHeaderField("Date", format.format(new Date()));
        }
        if (this.__headerFields.length() > 0) {
            header.append(this.__headerFields.toString());
        }
        header.append("From: ").append(this.__from).append(IOUtils.LINE_SEPARATOR_UNIX);
        if (this.__to != null) {
            header.append("To: ").append(this.__to).append(IOUtils.LINE_SEPARATOR_UNIX);
        }
        if (this.__cc != null) {
            header.append("Cc: ").append(this.__cc.toString()).append(IOUtils.LINE_SEPARATOR_UNIX);
        }
        if (this.__subject != null) {
            header.append("Subject: ").append(this.__subject).append(IOUtils.LINE_SEPARATOR_UNIX);
        }
        header.append('\n');
        return header.toString();
    }
}
