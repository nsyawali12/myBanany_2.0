package org.apache.commons.net.nntp;

public class SimpleNNTPHeader {
    private final String __from;
    private final StringBuilder __headerFields = new StringBuilder();
    private int __newsgroupCount = 0;
    private final StringBuilder __newsgroups = new StringBuilder();
    private final String __subject;

    public SimpleNNTPHeader(String from, String subject) {
        this.__from = from;
        this.__subject = subject;
    }

    public void addNewsgroup(String newsgroup) {
        int i = this.__newsgroupCount;
        this.__newsgroupCount = i + 1;
        if (i > 0) {
            this.__newsgroups.append(',');
        }
        this.__newsgroups.append(newsgroup);
    }

    public void addHeaderField(String headerField, String value) {
        this.__headerFields.append(headerField);
        this.__headerFields.append(": ");
        this.__headerFields.append(value);
        this.__headerFields.append('\n');
    }

    public String getFromAddress() {
        return this.__from;
    }

    public String getSubject() {
        return this.__subject;
    }

    public String getNewsgroups() {
        return this.__newsgroups.toString();
    }

    public String toString() {
        StringBuilder header = new StringBuilder();
        header.append("From: ");
        header.append(this.__from);
        header.append("\nNewsgroups: ");
        header.append(this.__newsgroups.toString());
        header.append("\nSubject: ");
        header.append(this.__subject);
        header.append('\n');
        if (this.__headerFields.length() > 0) {
            header.append(this.__headerFields.toString());
        }
        header.append('\n');
        return header.toString();
    }
}
