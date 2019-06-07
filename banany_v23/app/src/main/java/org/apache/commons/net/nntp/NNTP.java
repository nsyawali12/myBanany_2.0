package org.apache.commons.net.nntp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ProtocolCommandSupport;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;

public class NNTP extends SocketClient {
    public static final int DEFAULT_PORT = 119;
    private static final String __DEFAULT_ENCODING = "ISO-8859-1";
    protected ProtocolCommandSupport _commandSupport_ = new ProtocolCommandSupport(this);
    boolean _isAllowedToPost = false;
    protected BufferedReader _reader_ = null;
    int _replyCode;
    String _replyString = null;
    protected BufferedWriter _writer_ = null;

    public NNTP() {
        setDefaultPort(DEFAULT_PORT);
    }

    private void __getReply() throws IOException {
        this._replyString = this._reader_.readLine();
        if (this._replyString == null) {
            throw new NNTPConnectionClosedException("Connection closed without indication.");
        } else if (this._replyString.length() < 3) {
            throw new MalformedServerReplyException("Truncated server reply: " + this._replyString);
        } else {
            try {
                this._replyCode = Integer.parseInt(this._replyString.substring(0, 3));
                fireReplyReceived(this._replyCode, this._replyString + "\r\n");
                if (this._replyCode == NNTPReply.SERVICE_DISCONTINUED) {
                    throw new NNTPConnectionClosedException("NNTP response 400 received.  Server closed connection.");
                }
            } catch (NumberFormatException e) {
                throw new MalformedServerReplyException("Could not parse response code.\nServer Reply: " + this._replyString);
            }
        }
    }

    protected void _connectAction_() throws IOException {
        super._connectAction_();
        this._reader_ = new CRLFLineReader(new InputStreamReader(this._input_, "ISO-8859-1"));
        this._writer_ = new BufferedWriter(new OutputStreamWriter(this._output_, "ISO-8859-1"));
        __getReply();
        this._isAllowedToPost = this._replyCode == 200;
    }

    public void disconnect() throws IOException {
        super.disconnect();
        this._reader_ = null;
        this._writer_ = null;
        this._replyString = null;
        this._isAllowedToPost = false;
    }

    public boolean isAllowedToPost() {
        return this._isAllowedToPost;
    }

    public int sendCommand(String command, String args) throws IOException {
        StringBuilder __commandBuffer = new StringBuilder();
        __commandBuffer.append(command);
        if (args != null) {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append("\r\n");
        BufferedWriter bufferedWriter = this._writer_;
        String message = __commandBuffer.toString();
        bufferedWriter.write(message);
        this._writer_.flush();
        fireCommandSent(command, message);
        __getReply();
        return this._replyCode;
    }

    public int sendCommand(int command, String args) throws IOException {
        return sendCommand(NNTPCommand.getCommand(command), args);
    }

    public int sendCommand(String command) throws IOException {
        return sendCommand(command, null);
    }

    public int sendCommand(int command) throws IOException {
        return sendCommand(command, null);
    }

    public int getReplyCode() {
        return this._replyCode;
    }

    public int getReply() throws IOException {
        __getReply();
        return this._replyCode;
    }

    public String getReplyString() {
        return this._replyString;
    }

    public int article(String messageId) throws IOException {
        return sendCommand(0, messageId);
    }

    public int article(long articleNumber) throws IOException {
        return sendCommand(0, Long.toString(articleNumber));
    }

    public int article() throws IOException {
        return sendCommand(0);
    }

    public int body(String messageId) throws IOException {
        return sendCommand(1, messageId);
    }

    public int body(long articleNumber) throws IOException {
        return sendCommand(1, Long.toString(articleNumber));
    }

    public int body() throws IOException {
        return sendCommand(1);
    }

    public int head(String messageId) throws IOException {
        return sendCommand(3, messageId);
    }

    public int head(long articleNumber) throws IOException {
        return sendCommand(3, Long.toString(articleNumber));
    }

    public int head() throws IOException {
        return sendCommand(3);
    }

    public int stat(String messageId) throws IOException {
        return sendCommand(14, messageId);
    }

    public int stat(long articleNumber) throws IOException {
        return sendCommand(14, Long.toString(articleNumber));
    }

    public int stat() throws IOException {
        return sendCommand(14);
    }

    public int group(String newsgroup) throws IOException {
        return sendCommand(2, newsgroup);
    }

    public int help() throws IOException {
        return sendCommand(4);
    }

    public int ihave(String messageId) throws IOException {
        return sendCommand(5, messageId);
    }

    public int last() throws IOException {
        return sendCommand(6);
    }

    public int list() throws IOException {
        return sendCommand(7);
    }

    public int next() throws IOException {
        return sendCommand(10);
    }

    public int newgroups(String date, String time, boolean GMT, String distributions) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);
        if (GMT) {
            buffer.append(' ');
            buffer.append("GMT");
        }
        if (distributions != null) {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }
        return sendCommand(8, buffer.toString());
    }

    public int newnews(String newsgroups, String date, String time, boolean GMT, String distributions) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(newsgroups);
        buffer.append(' ');
        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);
        if (GMT) {
            buffer.append(' ');
            buffer.append("GMT");
        }
        if (distributions != null) {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }
        return sendCommand(9, buffer.toString());
    }

    public int post() throws IOException {
        return sendCommand(11);
    }

    public int quit() throws IOException {
        return sendCommand(12);
    }

    public int authinfoUser(String username) throws IOException {
        return sendCommand(15, "USER " + username);
    }

    public int authinfoPass(String password) throws IOException {
        return sendCommand(15, "PASS " + password);
    }

    public int xover(String selectedArticles) throws IOException {
        return sendCommand(16, selectedArticles);
    }

    public int xhdr(String header, String selectedArticles) throws IOException {
        StringBuilder command = new StringBuilder(header);
        command.append(" ");
        command.append(selectedArticles);
        return sendCommand(17, command.toString());
    }

    public int listActive(String wildmat) throws IOException {
        StringBuilder command = new StringBuilder("ACTIVE ");
        command.append(wildmat);
        return sendCommand(7, command.toString());
    }

    @Deprecated
    public int article(int a) throws IOException {
        return article((long) a);
    }

    @Deprecated
    public int body(int a) throws IOException {
        return body((long) a);
    }

    @Deprecated
    public int head(int a) throws IOException {
        return head((long) a);
    }

    @Deprecated
    public int stat(int a) throws IOException {
        return stat((long) a);
    }

    protected ProtocolCommandSupport getCommandSupport() {
        return this._commandSupport_;
    }
}
