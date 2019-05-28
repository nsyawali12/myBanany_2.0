package org.apache.commons.net.imap;

public enum IMAPCommand {
    CAPABILITY((String) 0),
    NOOP((String) 0),
    LOGOUT((String) 0),
    STARTTLS((String) 0),
    AUTHENTICATE((String) 1),
    LOGIN((String) 2),
    XOAUTH((String) 1),
    SELECT((String) 1),
    EXAMINE((String) 1),
    CREATE((String) 1),
    DELETE((String) 1),
    RENAME((String) 2),
    SUBSCRIBE((String) 1),
    UNSUBSCRIBE((String) 1),
    LIST((String) 2),
    LSUB((String) 2),
    STATUS((String) 2),
    APPEND((String) 2, 4),
    CHECK((String) 0),
    CLOSE((String) 0),
    EXPUNGE((String) 0),
    SEARCH((String) 1, Integer.MAX_VALUE),
    FETCH((String) 2),
    STORE((String) 3),
    COPY((String) 2),
    UID((String) 2, Integer.MAX_VALUE);
    
    private final String imapCommand;
    private final int maxParamCount;
    private final int minParamCount;

    private IMAPCommand(String name) {
        this(r2, r3, name, 0);
    }

    private IMAPCommand(int paramCount) {
        this(r7, r8, null, paramCount, paramCount);
    }

    private IMAPCommand(int minCount, int maxCount) {
        this(r7, r8, null, minCount, maxCount);
    }

    private IMAPCommand(String name, int paramCount) {
        this(r7, r8, name, paramCount, paramCount);
    }

    private IMAPCommand(String name, int minCount, int maxCount) {
        this.imapCommand = name;
        this.minParamCount = minCount;
        this.maxParamCount = maxCount;
    }

    public static final String getCommand(IMAPCommand command) {
        return command.getIMAPCommand();
    }

    public String getIMAPCommand() {
        return this.imapCommand != null ? this.imapCommand : name();
    }
}
