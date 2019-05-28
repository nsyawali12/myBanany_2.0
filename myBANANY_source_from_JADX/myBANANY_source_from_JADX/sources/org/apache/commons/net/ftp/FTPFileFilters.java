package org.apache.commons.net.ftp;

public class FTPFileFilters {
    public static final FTPFileFilter ALL = new C04111();
    public static final FTPFileFilter DIRECTORIES = new C04133();
    public static final FTPFileFilter NON_NULL = new C04122();

    /* renamed from: org.apache.commons.net.ftp.FTPFileFilters$1 */
    static class C04111 implements FTPFileFilter {
        C04111() {
        }

        public boolean accept(FTPFile file) {
            return true;
        }
    }

    /* renamed from: org.apache.commons.net.ftp.FTPFileFilters$2 */
    static class C04122 implements FTPFileFilter {
        C04122() {
        }

        public boolean accept(FTPFile file) {
            return file != null;
        }
    }

    /* renamed from: org.apache.commons.net.ftp.FTPFileFilters$3 */
    static class C04133 implements FTPFileFilter {
        C04133() {
        }

        public boolean accept(FTPFile file) {
            return file != null && file.isDirectory();
        }
    }
}
