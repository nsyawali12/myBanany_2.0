package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

public class CompositeFileEntryParser extends FTPFileEntryParserImpl {
    private FTPFileEntryParser cachedFtpFileEntryParser = null;
    private final FTPFileEntryParser[] ftpFileEntryParsers;

    public CompositeFileEntryParser(FTPFileEntryParser[] ftpFileEntryParsers) {
        this.ftpFileEntryParsers = ftpFileEntryParsers;
    }

    public FTPFile parseFTPEntry(String listEntry) {
        FTPFile matched;
        if (this.cachedFtpFileEntryParser != null) {
            matched = this.cachedFtpFileEntryParser.parseFTPEntry(listEntry);
            if (matched != null) {
                return matched;
            }
        }
        for (FTPFileEntryParser ftpFileEntryParser : this.ftpFileEntryParsers) {
            matched = ftpFileEntryParser.parseFTPEntry(listEntry);
            if (matched != null) {
                this.cachedFtpFileEntryParser = ftpFileEntryParser;
                return matched;
            }
        }
        return null;
    }
}
