package org.apache.commons.net.ftp.parser;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;
import org.apache.commons.net.nntp.NNTP;

public class MLSxEntryParser extends FTPFileEntryParserImpl {
    private static final MLSxEntryParser PARSER = new MLSxEntryParser();
    private static final HashMap<String, Integer> TYPE_TO_INT = new HashMap();
    private static int[] UNIX_GROUPS = new int[]{0, 1, 2};
    private static int[][] UNIX_PERMS;

    static {
        TYPE_TO_INT.put("file", Integer.valueOf(0));
        TYPE_TO_INT.put("cdir", Integer.valueOf(1));
        TYPE_TO_INT.put("pdir", Integer.valueOf(1));
        TYPE_TO_INT.put("dir", Integer.valueOf(1));
        r0 = new int[8][];
        r0[1] = new int[]{2};
        r0[2] = new int[]{1};
        r0[3] = new int[]{2, 1};
        r0[4] = new int[]{0};
        r0[5] = new int[]{0, 2};
        r0[6] = new int[]{0, 1};
        r0[7] = new int[]{0, 1, 2};
        UNIX_PERMS = r0;
    }

    public FTPFile parseFTPEntry(String entry) {
        FTPFile file;
        if (!entry.startsWith(" ")) {
            String[] parts = entry.split(" ", 2);
            if (parts.length != 2 || parts[1].length() == 0) {
                return null;
            }
            String factList = parts[0];
            if (!factList.endsWith(";")) {
                return null;
            }
            file = new FTPFile();
            file.setRawListing(entry);
            file.setName(parts[1]);
            String[] facts = factList.split(";");
            boolean hasUnixMode = parts[0].toLowerCase(Locale.ENGLISH).contains("unix.mode=");
            for (String fact : facts) {
                String[] factparts = fact.split("=", -1);
                if (factparts.length != 2) {
                    return null;
                }
                String factname = factparts[0].toLowerCase(Locale.ENGLISH);
                String factvalue = factparts[1];
                if (factvalue.length() != 0) {
                    String valueLowerCase = factvalue.toLowerCase(Locale.ENGLISH);
                    if ("size".equals(factname)) {
                        file.setSize(Long.parseLong(factvalue));
                    } else if ("sizd".equals(factname)) {
                        file.setSize(Long.parseLong(factvalue));
                    } else if ("modify".equals(factname)) {
                        Calendar parsed = parseGMTdateTime(factvalue);
                        if (parsed == null) {
                            return null;
                        }
                        file.setTimestamp(parsed);
                    } else if ("type".equals(factname)) {
                        Integer intType = (Integer) TYPE_TO_INT.get(valueLowerCase);
                        if (intType == null) {
                            file.setType(3);
                        } else {
                            file.setType(intType.intValue());
                        }
                    } else if (factname.startsWith("unix.")) {
                        String unixfact = factname.substring("unix.".length()).toLowerCase(Locale.ENGLISH);
                        if ("group".equals(unixfact)) {
                            file.setGroup(factvalue);
                        } else if ("owner".equals(unixfact)) {
                            file.setUser(factvalue);
                        } else if ("mode".equals(unixfact)) {
                            int off = factvalue.length() - 3;
                            for (int i = 0; i < 3; i++) {
                                int ch = factvalue.charAt(off + i) - 48;
                                if (ch >= 0 && ch <= 7) {
                                    for (int p : UNIX_PERMS[ch]) {
                                        file.setPermission(UNIX_GROUPS[i], p, true);
                                    }
                                }
                            }
                        }
                    } else if (!hasUnixMode && "perm".equals(factname)) {
                        doUnixPerms(file, valueLowerCase);
                    }
                }
            }
            return file;
        } else if (entry.length() <= 1) {
            return null;
        } else {
            file = new FTPFile();
            file.setRawListing(entry);
            file.setName(entry.substring(1));
            return file;
        }
    }

    public static Calendar parseGMTdateTime(String timestamp) {
        SimpleDateFormat sdf;
        boolean hasMillis;
        if (timestamp.contains(".")) {
            sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
            hasMillis = true;
        } else {
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            hasMillis = false;
        }
        TimeZone GMT = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(GMT);
        GregorianCalendar gc = new GregorianCalendar(GMT);
        ParsePosition pos = new ParsePosition(0);
        sdf.setLenient(false);
        Date parsed = sdf.parse(timestamp, pos);
        if (pos.getIndex() != timestamp.length()) {
            return null;
        }
        gc.setTime(parsed);
        if (hasMillis) {
            return gc;
        }
        gc.clear(14);
        return gc;
    }

    private void doUnixPerms(FTPFile file, String valueLowerCase) {
        for (char c : valueLowerCase.toCharArray()) {
            switch (c) {
                case 'a':
                    file.setPermission(0, 1, true);
                    break;
                case 'c':
                    file.setPermission(0, 1, true);
                    break;
                case 'd':
                    file.setPermission(0, 1, true);
                    break;
                case 'e':
                    file.setPermission(0, 0, true);
                    break;
                case 'l':
                    file.setPermission(0, 2, true);
                    break;
                case 'm':
                    file.setPermission(0, 1, true);
                    break;
                case 'p':
                    file.setPermission(0, 1, true);
                    break;
                case 'r':
                    file.setPermission(0, 0, true);
                    break;
                case NNTP.DEFAULT_PORT /*119*/:
                    file.setPermission(0, 1, true);
                    break;
                default:
                    break;
            }
        }
    }

    public static FTPFile parseEntry(String entry) {
        return PARSER.parseFTPEntry(entry);
    }

    public static MLSxEntryParser getInstance() {
        return PARSER;
    }
}
