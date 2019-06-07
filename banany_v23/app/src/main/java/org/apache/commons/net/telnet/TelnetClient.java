package org.apache.commons.net.telnet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TelnetClient extends Telnet {
    private InputStream __input = null;
    private OutputStream __output = null;
    private TelnetInputListener inputListener;
    protected boolean readerThread = true;

    public TelnetClient() {
        super("VT100");
    }

    public TelnetClient(String termtype) {
        super(termtype);
    }

    void _flushOutputStream() throws IOException {
        this._output_.flush();
    }

    void _closeOutputStream() throws IOException {
        this._output_.close();
    }

    protected void _connectAction_() throws IOException {
        super._connectAction_();
        TelnetInputStream tmp = new TelnetInputStream(this._input_, this, this.readerThread);
        if (this.readerThread) {
            tmp._start();
        }
        this.__input = new BufferedInputStream(tmp);
        this.__output = new TelnetOutputStream(this);
    }

    public void disconnect() throws IOException {
        if (this.__input != null) {
            this.__input.close();
        }
        if (this.__output != null) {
            this.__output.close();
        }
        super.disconnect();
    }

    public OutputStream getOutputStream() {
        return this.__output;
    }

    public InputStream getInputStream() {
        return this.__input;
    }

    public boolean getLocalOptionState(int option) {
        return _stateIsWill(option) && _requestedWill(option);
    }

    public boolean getRemoteOptionState(int option) {
        return _stateIsDo(option) && _requestedDo(option);
    }

    public boolean sendAYT(long timeout) throws IOException, IllegalArgumentException, InterruptedException {
        return _sendAYT(timeout);
    }

    public void sendSubnegotiation(int[] message) throws IOException, IllegalArgumentException {
        if (message.length < 1) {
            throw new IllegalArgumentException("zero length message");
        }
        _sendSubnegotiation(message);
    }

    public void sendCommand(byte command) throws IOException, IllegalArgumentException {
        _sendCommand(command);
    }

    public void addOptionHandler(TelnetOptionHandler opthand) throws InvalidTelnetOptionException, IOException {
        super.addOptionHandler(opthand);
    }

    public void deleteOptionHandler(int optcode) throws InvalidTelnetOptionException, IOException {
        super.deleteOptionHandler(optcode);
    }

    public void registerSpyStream(OutputStream spystream) {
        super._registerSpyStream(spystream);
    }

    public void stopSpyStream() {
        super._stopSpyStream();
    }

    public void registerNotifHandler(TelnetNotificationHandler notifhand) {
        super.registerNotifHandler(notifhand);
    }

    public void unregisterNotifHandler() {
        super.unregisterNotifHandler();
    }

    public void setReaderThread(boolean flag) {
        this.readerThread = flag;
    }

    public boolean getReaderThread() {
        return this.readerThread;
    }

    public synchronized void registerInputListener(TelnetInputListener listener) {
        this.inputListener = listener;
    }

    public synchronized void unregisterInputListener() {
        this.inputListener = null;
    }

    void notifyInputListener() {
        synchronized (this) {
            TelnetInputListener listener = this.inputListener;
        }
        if (listener != null) {
            listener.telnetInputAvailable();
        }
    }
}
