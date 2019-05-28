package org.apache.commons.net.imap;

import java.io.IOException;
import org.apache.commons.net.imap.IMAP.IMAPState;

public class IMAPClient extends IMAP {
    private static final char DQUOTE = '\"';
    private static final String DQUOTE_S = "\"";

    public enum FETCH_ITEM_NAMES {
        ALL,
        FAST,
        FULL,
        BODY,
        BODYSTRUCTURE,
        ENVELOPE,
        FLAGS,
        INTERNALDATE,
        RFC822,
        UID
    }

    public enum SEARCH_CRITERIA {
        ALL,
        ANSWERED,
        BCC,
        BEFORE,
        BODY,
        CC,
        DELETED,
        DRAFT,
        FLAGGED,
        FROM,
        HEADER,
        KEYWORD,
        LARGER,
        NEW,
        NOT,
        OLD,
        ON,
        OR,
        RECENT,
        SEEN,
        SENTBEFORE,
        SENTON,
        SENTSINCE,
        SINCE,
        SMALLER,
        SUBJECT,
        TEXT,
        TO,
        UID,
        UNANSWERED,
        UNDELETED,
        UNDRAFT,
        UNFLAGGED,
        UNKEYWORD,
        UNSEEN
    }

    public enum STATUS_DATA_ITEMS {
        MESSAGES,
        RECENT,
        UIDNEXT,
        UIDVALIDITY,
        UNSEEN
    }

    public boolean capability() throws IOException {
        return doCommand(IMAPCommand.CAPABILITY);
    }

    public boolean noop() throws IOException {
        return doCommand(IMAPCommand.NOOP);
    }

    public boolean logout() throws IOException {
        return doCommand(IMAPCommand.LOGOUT);
    }

    public boolean login(String username, String password) throws IOException {
        if (getState() != IMAPState.NOT_AUTH_STATE || !doCommand(IMAPCommand.LOGIN, username + " " + password)) {
            return false;
        }
        setState(IMAPState.AUTH_STATE);
        return true;
    }

    public boolean select(String mailboxName) throws IOException {
        return doCommand(IMAPCommand.SELECT, mailboxName);
    }

    public boolean examine(String mailboxName) throws IOException {
        return doCommand(IMAPCommand.EXAMINE, mailboxName);
    }

    public boolean create(String mailboxName) throws IOException {
        return doCommand(IMAPCommand.CREATE, mailboxName);
    }

    public boolean delete(String mailboxName) throws IOException {
        return doCommand(IMAPCommand.DELETE, mailboxName);
    }

    public boolean rename(String oldMailboxName, String newMailboxName) throws IOException {
        return doCommand(IMAPCommand.RENAME, oldMailboxName + " " + newMailboxName);
    }

    public boolean subscribe(String mailboxName) throws IOException {
        return doCommand(IMAPCommand.SUBSCRIBE, mailboxName);
    }

    public boolean unsubscribe(String mailboxName) throws IOException {
        return doCommand(IMAPCommand.UNSUBSCRIBE, mailboxName);
    }

    public boolean list(String refName, String mailboxName) throws IOException {
        return doCommand(IMAPCommand.LIST, refName + " " + mailboxName);
    }

    public boolean lsub(String refName, String mailboxName) throws IOException {
        return doCommand(IMAPCommand.LSUB, refName + " " + mailboxName);
    }

    public boolean status(String mailboxName, String[] itemNames) throws IOException {
        if (itemNames == null || itemNames.length < 1) {
            throw new IllegalArgumentException("STATUS command requires at least one data item name");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mailboxName);
        sb.append(" (");
        for (int i = 0; i < itemNames.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(itemNames[i]);
        }
        sb.append(")");
        return doCommand(IMAPCommand.STATUS, sb.toString());
    }

    public boolean append(String mailboxName, String flags, String datetime, String message) throws IOException {
        StringBuilder args = new StringBuilder(mailboxName);
        if (flags != null) {
            args.append(" ").append(flags);
        }
        if (datetime != null) {
            args.append(" ");
            if (datetime.charAt(0) == DQUOTE) {
                args.append(datetime);
            } else {
                args.append(DQUOTE).append(datetime).append(DQUOTE);
            }
        }
        args.append(" ");
        if (message.startsWith(DQUOTE_S) && message.endsWith(DQUOTE_S)) {
            args.append(message);
            return doCommand(IMAPCommand.APPEND, args.toString());
        }
        args.append('{').append(message.length()).append('}');
        if (IMAPReply.isContinuation(sendCommand(IMAPCommand.APPEND, args.toString())) && IMAPReply.isSuccess(sendData(message))) {
            return true;
        }
        return false;
    }

    @Deprecated
    public boolean append(String mailboxName, String flags, String datetime) throws IOException {
        String args = mailboxName;
        if (flags != null) {
            args = args + " " + flags;
        }
        if (datetime != null) {
            if (datetime.charAt(0) == '{') {
                args = args + " " + datetime;
            } else {
                args = args + " {" + datetime + "}";
            }
        }
        return doCommand(IMAPCommand.APPEND, args);
    }

    @Deprecated
    public boolean append(String mailboxName) throws IOException {
        return append(mailboxName, null, null);
    }

    public boolean check() throws IOException {
        return doCommand(IMAPCommand.CHECK);
    }

    public boolean close() throws IOException {
        return doCommand(IMAPCommand.CLOSE);
    }

    public boolean expunge() throws IOException {
        return doCommand(IMAPCommand.EXPUNGE);
    }

    public boolean search(String charset, String criteria) throws IOException {
        String args = "";
        if (charset != null) {
            args = args + "CHARSET " + charset;
        }
        return doCommand(IMAPCommand.SEARCH, args + criteria);
    }

    public boolean search(String criteria) throws IOException {
        return search(null, criteria);
    }

    public boolean fetch(String sequenceSet, String itemNames) throws IOException {
        return doCommand(IMAPCommand.FETCH, sequenceSet + " " + itemNames);
    }

    public boolean store(String sequenceSet, String itemNames, String itemValues) throws IOException {
        return doCommand(IMAPCommand.STORE, sequenceSet + " " + itemNames + " " + itemValues);
    }

    public boolean copy(String sequenceSet, String mailboxName) throws IOException {
        return doCommand(IMAPCommand.COPY, sequenceSet + " " + mailboxName);
    }

    public boolean uid(String command, String commandArgs) throws IOException {
        return doCommand(IMAPCommand.UID, command + " " + commandArgs);
    }
}
