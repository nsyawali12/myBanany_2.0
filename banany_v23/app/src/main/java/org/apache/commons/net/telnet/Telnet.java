package org.apache.commons.net.telnet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.SocketClient;

class Telnet extends SocketClient {
    static final int DEFAULT_PORT = 23;
    protected static final int TERMINAL_TYPE = 24;
    protected static final int TERMINAL_TYPE_IS = 0;
    protected static final int TERMINAL_TYPE_SEND = 1;
    static final byte[] _COMMAND_AYT = new byte[]{(byte) -1, (byte) -10};
    static final byte[] _COMMAND_DO = new byte[]{(byte) -1, (byte) -3};
    static final byte[] _COMMAND_DONT = new byte[]{(byte) -1, (byte) -2};
    static final byte[] _COMMAND_IS = new byte[]{(byte) 24, (byte) 0};
    static final byte[] _COMMAND_SB = new byte[]{(byte) -1, (byte) -6};
    static final byte[] _COMMAND_SE = new byte[]{(byte) -1, (byte) -16};
    static final byte[] _COMMAND_WILL = new byte[]{(byte) -1, (byte) -5};
    static final byte[] _COMMAND_WONT = new byte[]{(byte) -1, (byte) -4};
    static final int _DO_MASK = 2;
    static final int _REQUESTED_DO_MASK = 8;
    static final int _REQUESTED_WILL_MASK = 4;
    static final int _WILL_MASK = 1;
    static final boolean debug = false;
    static final boolean debugoptions = false;
    private TelnetNotificationHandler __notifhand = null;
    int[] _doResponse;
    int[] _options;
    int[] _willResponse;
    private volatile boolean aytFlag = true;
    private final Object aytMonitor = new Object();
    private final TelnetOptionHandler[] optionHandlers;
    private volatile OutputStream spyStream = null;
    private String terminalType = null;

    Telnet() {
        setDefaultPort(23);
        this._doResponse = new int[256];
        this._willResponse = new int[256];
        this._options = new int[256];
        this.optionHandlers = new TelnetOptionHandler[256];
    }

    Telnet(String termtype) {
        setDefaultPort(23);
        this._doResponse = new int[256];
        this._willResponse = new int[256];
        this._options = new int[256];
        this.terminalType = termtype;
        this.optionHandlers = new TelnetOptionHandler[256];
    }

    boolean _stateIsWill(int option) {
        return (this._options[option] & 1) != 0;
    }

    boolean _stateIsWont(int option) {
        return !_stateIsWill(option);
    }

    boolean _stateIsDo(int option) {
        return (this._options[option] & 2) != 0;
    }

    boolean _stateIsDont(int option) {
        return !_stateIsDo(option);
    }

    boolean _requestedWill(int option) {
        return (this._options[option] & 4) != 0;
    }

    boolean _requestedWont(int option) {
        return !_requestedWill(option);
    }

    boolean _requestedDo(int option) {
        return (this._options[option] & 8) != 0;
    }

    boolean _requestedDont(int option) {
        return !_requestedDo(option);
    }

    void _setWill(int option) throws IOException {
        int[] iArr = this._options;
        iArr[option] = iArr[option] | 1;
        if (_requestedWill(option) && this.optionHandlers[option] != null) {
            this.optionHandlers[option].setWill(true);
            int[] subneg = this.optionHandlers[option].startSubnegotiationLocal();
            if (subneg != null) {
                _sendSubnegotiation(subneg);
            }
        }
    }

    void _setDo(int option) throws IOException {
        int[] iArr = this._options;
        iArr[option] = iArr[option] | 2;
        if (_requestedDo(option) && this.optionHandlers[option] != null) {
            this.optionHandlers[option].setDo(true);
            int[] subneg = this.optionHandlers[option].startSubnegotiationRemote();
            if (subneg != null) {
                _sendSubnegotiation(subneg);
            }
        }
    }

    void _setWantWill(int option) {
        int[] iArr = this._options;
        iArr[option] = iArr[option] | 4;
    }

    void _setWantDo(int option) {
        int[] iArr = this._options;
        iArr[option] = iArr[option] | 8;
    }

    void _setWont(int option) {
        int[] iArr = this._options;
        iArr[option] = iArr[option] & -2;
        if (this.optionHandlers[option] != null) {
            this.optionHandlers[option].setWill(false);
        }
    }

    void _setDont(int option) {
        int[] iArr = this._options;
        iArr[option] = iArr[option] & -3;
        if (this.optionHandlers[option] != null) {
            this.optionHandlers[option].setDo(false);
        }
    }

    void _setWantWont(int option) {
        int[] iArr = this._options;
        iArr[option] = iArr[option] & -5;
    }

    void _setWantDont(int option) {
        int[] iArr = this._options;
        iArr[option] = iArr[option] & -9;
    }

    void _processCommand(int command) {
        if (this.__notifhand != null) {
            this.__notifhand.receivedNegotiation(5, command);
        }
    }

    void _processDo(int option) throws IOException {
        int[] iArr;
        if (this.__notifhand != null) {
            this.__notifhand.receivedNegotiation(1, option);
        }
        boolean acceptNewState = false;
        if (this.optionHandlers[option] != null) {
            acceptNewState = this.optionHandlers[option].getAcceptLocal();
        } else if (option == 24 && this.terminalType != null && this.terminalType.length() > 0) {
            acceptNewState = true;
        }
        if (this._willResponse[option] > 0) {
            iArr = this._willResponse;
            iArr[option] = iArr[option] - 1;
            if (this._willResponse[option] > 0 && _stateIsWill(option)) {
                iArr = this._willResponse;
                iArr[option] = iArr[option] - 1;
            }
        }
        if (this._willResponse[option] == 0 && _requestedWont(option)) {
            if (acceptNewState) {
                _setWantWill(option);
                _sendWill(option);
            } else {
                iArr = this._willResponse;
                iArr[option] = iArr[option] + 1;
                _sendWont(option);
            }
        }
        _setWill(option);
    }

    void _processDont(int option) throws IOException {
        if (this.__notifhand != null) {
            this.__notifhand.receivedNegotiation(2, option);
        }
        if (this._willResponse[option] > 0) {
            int[] iArr = this._willResponse;
            iArr[option] = iArr[option] - 1;
            if (this._willResponse[option] > 0 && _stateIsWont(option)) {
                iArr = this._willResponse;
                iArr[option] = iArr[option] - 1;
            }
        }
        if (this._willResponse[option] == 0 && _requestedWill(option)) {
            if (_stateIsWill(option) || _requestedWill(option)) {
                _sendWont(option);
            }
            _setWantWont(option);
        }
        _setWont(option);
    }

    void _processWill(int option) throws IOException {
        int[] iArr;
        if (this.__notifhand != null) {
            this.__notifhand.receivedNegotiation(3, option);
        }
        boolean acceptNewState = false;
        if (this.optionHandlers[option] != null) {
            acceptNewState = this.optionHandlers[option].getAcceptRemote();
        }
        if (this._doResponse[option] > 0) {
            iArr = this._doResponse;
            iArr[option] = iArr[option] - 1;
            if (this._doResponse[option] > 0 && _stateIsDo(option)) {
                iArr = this._doResponse;
                iArr[option] = iArr[option] - 1;
            }
        }
        if (this._doResponse[option] == 0 && _requestedDont(option)) {
            if (acceptNewState) {
                _setWantDo(option);
                _sendDo(option);
            } else {
                iArr = this._doResponse;
                iArr[option] = iArr[option] + 1;
                _sendDont(option);
            }
        }
        _setDo(option);
    }

    void _processWont(int option) throws IOException {
        if (this.__notifhand != null) {
            this.__notifhand.receivedNegotiation(4, option);
        }
        if (this._doResponse[option] > 0) {
            int[] iArr = this._doResponse;
            iArr[option] = iArr[option] - 1;
            if (this._doResponse[option] > 0 && _stateIsDont(option)) {
                iArr = this._doResponse;
                iArr[option] = iArr[option] - 1;
            }
        }
        if (this._doResponse[option] == 0 && _requestedDo(option)) {
            if (_stateIsDo(option) || _requestedDo(option)) {
                _sendDont(option);
            }
            _setWantDont(option);
        }
        _setDont(option);
    }

    void _processSuboption(int[] suboption, int suboptionLength) throws IOException {
        if (suboptionLength <= 0) {
            return;
        }
        if (this.optionHandlers[suboption[0]] != null) {
            _sendSubnegotiation(this.optionHandlers[suboption[0]].answerSubnegotiation(suboption, suboptionLength));
        } else if (suboptionLength > 1 && suboption[0] == 24 && suboption[1] == 1) {
            _sendTerminalType();
        }
    }

    final synchronized void _sendTerminalType() throws IOException {
        if (this.terminalType != null) {
            this._output_.write(_COMMAND_SB);
            this._output_.write(_COMMAND_IS);
            this._output_.write(this.terminalType.getBytes(getCharsetName()));
            this._output_.write(_COMMAND_SE);
            this._output_.flush();
        }
    }

    final synchronized void _sendSubnegotiation(int[] subn) throws IOException {
        if (subn != null) {
            this._output_.write(_COMMAND_SB);
            for (int element : subn) {
                byte b = (byte) element;
                if (b == (byte) -1) {
                    this._output_.write(b);
                }
                this._output_.write(b);
            }
            this._output_.write(_COMMAND_SE);
            this._output_.flush();
        }
    }

    final synchronized void _sendCommand(byte cmd) throws IOException {
        this._output_.write(255);
        this._output_.write(cmd);
        this._output_.flush();
    }

    final synchronized void _processAYTResponse() {
        if (!this.aytFlag) {
            synchronized (this.aytMonitor) {
                this.aytFlag = true;
                this.aytMonitor.notifyAll();
            }
        }
    }

    protected void _connectAction_() throws IOException {
        int ii;
        for (ii = 0; ii < 256; ii++) {
            this._doResponse[ii] = 0;
            this._willResponse[ii] = 0;
            this._options[ii] = 0;
            if (this.optionHandlers[ii] != null) {
                this.optionHandlers[ii].setDo(false);
                this.optionHandlers[ii].setWill(false);
            }
        }
        super._connectAction_();
        this._input_ = new BufferedInputStream(this._input_);
        this._output_ = new BufferedOutputStream(this._output_);
        for (ii = 0; ii < 256; ii++) {
            if (this.optionHandlers[ii] != null) {
                if (this.optionHandlers[ii].getInitLocal()) {
                    _requestWill(this.optionHandlers[ii].getOptionCode());
                }
                if (this.optionHandlers[ii].getInitRemote()) {
                    _requestDo(this.optionHandlers[ii].getOptionCode());
                }
            }
        }
    }

    final synchronized void _sendDo(int option) throws IOException {
        this._output_.write(_COMMAND_DO);
        this._output_.write(option);
        this._output_.flush();
    }

    final synchronized void _requestDo(int option) throws IOException {
        if (!((this._doResponse[option] == 0 && _stateIsDo(option)) || _requestedDo(option))) {
            _setWantDo(option);
            int[] iArr = this._doResponse;
            iArr[option] = iArr[option] + 1;
            _sendDo(option);
        }
    }

    final synchronized void _sendDont(int option) throws IOException {
        this._output_.write(_COMMAND_DONT);
        this._output_.write(option);
        this._output_.flush();
    }

    final synchronized void _requestDont(int option) throws IOException {
        if (!((this._doResponse[option] == 0 && _stateIsDont(option)) || _requestedDont(option))) {
            _setWantDont(option);
            int[] iArr = this._doResponse;
            iArr[option] = iArr[option] + 1;
            _sendDont(option);
        }
    }

    final synchronized void _sendWill(int option) throws IOException {
        this._output_.write(_COMMAND_WILL);
        this._output_.write(option);
        this._output_.flush();
    }

    final synchronized void _requestWill(int option) throws IOException {
        if (!((this._willResponse[option] == 0 && _stateIsWill(option)) || _requestedWill(option))) {
            _setWantWill(option);
            int[] iArr = this._doResponse;
            iArr[option] = iArr[option] + 1;
            _sendWill(option);
        }
    }

    final synchronized void _sendWont(int option) throws IOException {
        this._output_.write(_COMMAND_WONT);
        this._output_.write(option);
        this._output_.flush();
    }

    final synchronized void _requestWont(int option) throws IOException {
        if (!((this._willResponse[option] == 0 && _stateIsWont(option)) || _requestedWont(option))) {
            _setWantWont(option);
            int[] iArr = this._doResponse;
            iArr[option] = iArr[option] + 1;
            _sendWont(option);
        }
    }

    final synchronized void _sendByte(int b) throws IOException {
        this._output_.write(b);
        _spyWrite(b);
    }

    final boolean _sendAYT(long timeout) throws IOException, IllegalArgumentException, InterruptedException {
        boolean retValue;
        synchronized (this.aytMonitor) {
            synchronized (this) {
                this.aytFlag = false;
                this._output_.write(_COMMAND_AYT);
                this._output_.flush();
            }
            this.aytMonitor.wait(timeout);
            if (this.aytFlag) {
                retValue = true;
            } else {
                retValue = false;
                this.aytFlag = true;
            }
        }
        return retValue;
    }

    void addOptionHandler(TelnetOptionHandler opthand) throws InvalidTelnetOptionException, IOException {
        int optcode = opthand.getOptionCode();
        if (!TelnetOption.isValidOption(optcode)) {
            throw new InvalidTelnetOptionException("Invalid Option Code", optcode);
        } else if (this.optionHandlers[optcode] == null) {
            this.optionHandlers[optcode] = opthand;
            if (isConnected()) {
                if (opthand.getInitLocal()) {
                    _requestWill(optcode);
                }
                if (opthand.getInitRemote()) {
                    _requestDo(optcode);
                }
            }
        } else {
            throw new InvalidTelnetOptionException("Already registered option", optcode);
        }
    }

    void deleteOptionHandler(int optcode) throws InvalidTelnetOptionException, IOException {
        if (!TelnetOption.isValidOption(optcode)) {
            throw new InvalidTelnetOptionException("Invalid Option Code", optcode);
        } else if (this.optionHandlers[optcode] == null) {
            throw new InvalidTelnetOptionException("Unregistered option", optcode);
        } else {
            TelnetOptionHandler opthand = this.optionHandlers[optcode];
            this.optionHandlers[optcode] = null;
            if (opthand.getWill()) {
                _requestWont(optcode);
            }
            if (opthand.getDo()) {
                _requestDont(optcode);
            }
        }
    }

    void _registerSpyStream(OutputStream spystream) {
        this.spyStream = spystream;
    }

    void _stopSpyStream() {
        this.spyStream = null;
    }

    void _spyRead(int ch) {
        OutputStream spy = this.spyStream;
        if (spy != null && ch != 13) {
            if (ch == 10) {
                try {
                    spy.write(13);
                } catch (IOException e) {
                    this.spyStream = null;
                    return;
                }
            }
            spy.write(ch);
            spy.flush();
        }
    }

    void _spyWrite(int ch) {
        if (!_stateIsDo(1) || !_requestedDo(1)) {
            OutputStream spy = this.spyStream;
            if (spy != null) {
                try {
                    spy.write(ch);
                    spy.flush();
                } catch (IOException e) {
                    this.spyStream = null;
                }
            }
        }
    }

    public void registerNotifHandler(TelnetNotificationHandler notifhand) {
        this.__notifhand = notifhand;
    }

    public void unregisterNotifHandler() {
        this.__notifhand = null;
    }
}
