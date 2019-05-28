package org.apache.commons.net.ftp;

public interface FTPFileFilter {
    boolean accept(FTPFile fTPFile);
}
