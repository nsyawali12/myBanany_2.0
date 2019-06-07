package org.apache.commons.net.pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ProtocolCommandSupport;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;

public class POP3 extends SocketClient {
    public static final int AUTHORIZATION_STATE = 0;
    public static final int DEFAULT_PORT = 110;
    public static final int DISCONNECTED_STATE = -1;
    public static final int TRANSACTION_STATE = 1;
    public static final int UPDATE_STATE = 2;
    static final String _DEFAULT_ENCODING = "ISO-8859-1";
    static final String _ERROR = "-ERR";
    static final String _OK = "+OK";
    static final String _OK_INT = "+ ";
    private int __popState = -1;
    protected ProtocolCommandSupport _commandSupport_ = new ProtocolCommandSupport(this);
    String _lastReplyLine;
    BufferedReader _reader = null;
    int _replyCode;
    List<String> _replyLines = new ArrayList();
    BufferedWriter _writer = null;

    public POP3() {
        setDefaultPort(110);
    }

    private void __getReply() throws IOException {
        this._replyLines.clear();
        String line = this._reader.readLine();
        if (line == null) {
            throw new EOFException("Connection closed without indication.");
        }
        if (line.startsWith(_OK)) {
            this._replyCode = 0;
        } else if (line.startsWith(_ERROR)) {
            this._replyCode = 1;
        } else if (line.startsWith(_OK_INT)) {
            this._replyCode = 2;
        } else {
            throw new MalformedServerReplyException("Received invalid POP3 protocol response from server." + line);
        }
        this._replyLines.add(line);
        this._lastReplyLine = line;
        fireReplyReceived(this._replyCode, getReplyString());
    }

    protected void _connectAction_() throws IOException {
        super._connectAction_();
        this._reader = new CRLFLineReader(new InputStreamReader(this._input_, "ISO-8859-1"));
        this._writer = new BufferedWriter(new OutputStreamWriter(this._output_, "ISO-8859-1"));
        __getReply();
        setState(0);
    }

    public void setState(int state) {
        this.__popState = state;
    }

    public int getState() {
        return this.__popState;
    }

    public void getAdditionalReply() throws IOException {
        String line = this._reader.readLine();
        while (line != null) {
            this._replyLines.add(line);
            if (!line.equals(".")) {
                line = this._reader.readLine();
            } else {
                return;
            }
        }
    }

    public void disconnect() throws IOException {
        super.disconnect();
        this._reader = null;
        this._writer = null;
        this._lastReplyLine = null;
        this._replyLines.clear();
        setState(-1);
    }

    public int sendCommand(String command, String args) throws IOException {
        if (this._writer == null) {
            throw new IllegalStateException("Socket is not connected");
        }
        StringBuilder __commandBuffer = new StringBuilder();
        __commandBuffer.append(command);
        if (args != null) {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append("\r\n");
        String message = __commandBuffer.toString();
        this._writer.write(message);
        this._writer.flush();
        fireCommandSent(command, message);
        __getReply();
        return this._replyCode;
    }

    public int sendCommand(String command) throws IOException {
        return sendCommand(command, null);
    }

    public int sendCommand(int command, String args) throws IOException {
        return sendCommand(POP3Command._commands[command], args);
    }

    public int sendCommand(int command) throws IOException {
        return sendCommand(POP3Command._commands[command], null);
    }

    public String[] getReplyStrings() {
        return (String[]) this._replyLines.toArray(new String[this._replyLines.size()]);
    }

    public String getReplyString() {
        StringBuilder buffer = new StringBuilder(256);
        for (String entry : this._replyLines) {
            buffer.append(entry);
            buffer.append("\r\n");
        }
        return buffer.toString();
    }

    public void removeProtocolCommandistener(ProtocolCommandListener listener) {
        removeProtocolCommandListener(listener);
    }

    protected ProtocolCommandSupport getCommandSupport() {
        return this._commandSupport_;
    }
}
