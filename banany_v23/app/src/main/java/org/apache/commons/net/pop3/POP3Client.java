package org.apache.commons.net.pop3;

import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ListIterator;
import java.util.StringTokenizer;
import org.apache.commons.net.io.DotTerminatedMessageReader;

public class POP3Client extends POP3 {
    private static POP3MessageInfo __parseStatus(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        if (!tokenizer.hasMoreElements()) {
            return null;
        }
        int num = 0;
        try {
            num = Integer.parseInt(tokenizer.nextToken());
            if (tokenizer.hasMoreElements()) {
                return new POP3MessageInfo(num, Integer.parseInt(tokenizer.nextToken()));
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static POP3MessageInfo __parseUID(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        if (!tokenizer.hasMoreElements()) {
            return null;
        }
        try {
            int num = Integer.parseInt(tokenizer.nextToken());
            if (tokenizer.hasMoreElements()) {
                return new POP3MessageInfo(num, tokenizer.nextToken());
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean capa() throws IOException {
        if (sendCommand(12) != 0) {
            return false;
        }
        getAdditionalReply();
        return true;
    }

    public boolean login(String username, String password) throws IOException {
        if (getState() != 0 || sendCommand(0, username) != 0 || sendCommand(1, password) != 0) {
            return false;
        }
        setState(1);
        return true;
    }

    public boolean login(String username, String timestamp, String secret) throws IOException, NoSuchAlgorithmException {
        if (getState() != 0) {
            return false;
        }
        byte[] digest = MessageDigest.getInstance("MD5").digest((timestamp + secret).getBytes(getCharsetName()));
        StringBuilder digestBuffer = new StringBuilder(128);
        for (byte b : digest) {
            int digit = b & 255;
            if (digit <= 15) {
                digestBuffer.append("0");
            }
            digestBuffer.append(Integer.toHexString(digit));
        }
        StringBuilder buffer = new StringBuilder(256);
        buffer.append(username);
        buffer.append(' ');
        buffer.append(digestBuffer.toString());
        if (sendCommand(9, buffer.toString()) != 0) {
            return false;
        }
        setState(1);
        return true;
    }

    public boolean logout() throws IOException {
        if (getState() == 1) {
            setState(2);
        }
        sendCommand(2);
        if (this._replyCode == 0) {
            return true;
        }
        return false;
    }

    public boolean noop() throws IOException {
        if (getState() != 1) {
            return false;
        }
        if (sendCommand(7) == 0) {
            return true;
        }
        return false;
    }

    public boolean deleteMessage(int messageId) throws IOException {
        if (getState() != 1) {
            return false;
        }
        if (sendCommand(6, Integer.toString(messageId)) == 0) {
            return true;
        }
        return false;
    }

    public boolean reset() throws IOException {
        if (getState() != 1) {
            return false;
        }
        if (sendCommand(8) == 0) {
            return true;
        }
        return false;
    }

    public POP3MessageInfo status() throws IOException {
        if (getState() == 1 && sendCommand(3) == 0) {
            return __parseStatus(this._lastReplyLine.substring(3));
        }
        return null;
    }

    public POP3MessageInfo listMessage(int messageId) throws IOException {
        if (getState() == 1 && sendCommand(4, Integer.toString(messageId)) == 0) {
            return __parseStatus(this._lastReplyLine.substring(3));
        }
        return null;
    }

    public POP3MessageInfo[] listMessages() throws IOException {
        POP3MessageInfo[] pOP3MessageInfoArr = null;
        if (getState() == 1 && sendCommand(4) == 0) {
            getAdditionalReply();
            pOP3MessageInfoArr = new POP3MessageInfo[(this._replyLines.size() - 2)];
            ListIterator<String> en = this._replyLines.listIterator(1);
            for (int line = 0; line < pOP3MessageInfoArr.length; line++) {
                pOP3MessageInfoArr[line] = __parseStatus((String) en.next());
            }
        }
        return pOP3MessageInfoArr;
    }

    public POP3MessageInfo listUniqueIdentifier(int messageId) throws IOException {
        if (getState() == 1 && sendCommand(11, Integer.toString(messageId)) == 0) {
            return __parseUID(this._lastReplyLine.substring(3));
        }
        return null;
    }

    public POP3MessageInfo[] listUniqueIdentifiers() throws IOException {
        POP3MessageInfo[] pOP3MessageInfoArr = null;
        if (getState() == 1 && sendCommand(11) == 0) {
            getAdditionalReply();
            pOP3MessageInfoArr = new POP3MessageInfo[(this._replyLines.size() - 2)];
            ListIterator<String> en = this._replyLines.listIterator(1);
            for (int line = 0; line < pOP3MessageInfoArr.length; line++) {
                pOP3MessageInfoArr[line] = __parseUID((String) en.next());
            }
        }
        return pOP3MessageInfoArr;
    }

    public Reader retrieveMessage(int messageId) throws IOException {
        if (getState() == 1 && sendCommand(5, Integer.toString(messageId)) == 0) {
            return new DotTerminatedMessageReader(this._reader);
        }
        return null;
    }

    public Reader retrieveMessageTop(int messageId, int numLines) throws IOException {
        if (numLines >= 0 && getState() == 1 && sendCommand(10, Integer.toString(messageId) + " " + Integer.toString(numLines)) == 0) {
            return new DotTerminatedMessageReader(this._reader);
        }
        return null;
    }
}
