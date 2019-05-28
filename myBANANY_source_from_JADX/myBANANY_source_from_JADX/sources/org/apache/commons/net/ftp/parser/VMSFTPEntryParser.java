package org.apache.commons.net.ftp.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.StringTokenizer;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

public class VMSFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {
    private static final String DEFAULT_DATE_FORMAT = "d-MMM-yyyy HH:mm:ss";
    private static final String REGEX = "(.*?;[0-9]+)\\s*(\\d+)/\\d+\\s*(\\S+)\\s+(\\S+)\\s+\\[(([0-9$A-Za-z_]+)|([0-9$A-Za-z_]+),([0-9$a-zA-Z_]+))\\]?\\s*\\([a-zA-Z]*,([a-zA-Z]*),([a-zA-Z]*),([a-zA-Z]*)\\)";

    public VMSFTPEntryParser() {
        this(null);
    }

    public VMSFTPEntryParser(FTPClientConfig config) {
        super(REGEX);
        configure(config);
    }

    public FTPFile parseFTPEntry(String entry) {
        if (!matches(entry)) {
            return null;
        }
        String grp;
        String user;
        FTPFile fTPFile = new FTPFile();
        fTPFile.setRawListing(entry);
        String name = group(1);
        String size = group(2);
        String datestr = group(3) + " " + group(4);
        String owner = group(5);
        String[] permissions = new String[]{group(9), group(10), group(11)};
        try {
            fTPFile.setTimestamp(super.parseTimestamp(datestr));
        } catch (ParseException e) {
        }
        StringTokenizer t = new StringTokenizer(owner, ",");
        switch (t.countTokens()) {
            case 1:
                grp = null;
                user = t.nextToken();
                break;
            case 2:
                grp = t.nextToken();
                user = t.nextToken();
                break;
            default:
                grp = null;
                user = null;
                break;
        }
        if (name.lastIndexOf(".DIR") != -1) {
            fTPFile.setType(1);
        } else {
            fTPFile.setType(0);
        }
        if (isVersioning()) {
            fTPFile.setName(name);
        } else {
            fTPFile.setName(name.substring(0, name.lastIndexOf(";")));
        }
        fTPFile.setSize(Long.parseLong(size) * 512);
        fTPFile.setGroup(grp);
        fTPFile.setUser(user);
        for (int access = 0; access < 3; access++) {
            boolean z;
            String permission = permissions[access];
            fTPFile.setPermission(access, 0, permission.indexOf(82) >= 0);
            fTPFile.setPermission(access, 1, permission.indexOf(87) >= 0);
            if (permission.indexOf(69) >= 0) {
                z = true;
            } else {
                z = false;
            }
            fTPFile.setPermission(access, 2, z);
        }
        return fTPFile;
    }

    public String readNextEntry(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        StringBuilder entry = new StringBuilder();
        while (line != null) {
            if (line.startsWith("Directory") || line.startsWith("Total")) {
                line = reader.readLine();
            } else {
                entry.append(line);
                if (line.trim().endsWith(")")) {
                    break;
                }
                line = reader.readLine();
            }
        }
        return entry.length() == 0 ? null : entry.toString();
    }

    protected boolean isVersioning() {
        return false;
    }

    protected FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig(FTPClientConfig.SYST_VMS, DEFAULT_DATE_FORMAT, null, null, null, null);
    }

    @Deprecated
    public FTPFile[] parseFileList(InputStream listStream) throws IOException {
        FTPListParseEngine engine = new FTPListParseEngine(this);
        engine.readServerList(listStream, null);
        return engine.getFiles();
    }
}
