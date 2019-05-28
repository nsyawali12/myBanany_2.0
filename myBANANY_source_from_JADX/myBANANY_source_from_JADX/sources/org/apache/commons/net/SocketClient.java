package org.apache.commons.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

public abstract class SocketClient {
    private static final int DEFAULT_CONNECT_TIMEOUT = 0;
    public static final String NETASCII_EOL = "\r\n";
    private static final ServerSocketFactory __DEFAULT_SERVER_SOCKET_FACTORY = ServerSocketFactory.getDefault();
    private static final SocketFactory __DEFAULT_SOCKET_FACTORY = SocketFactory.getDefault();
    private ProtocolCommandSupport __commandSupport;
    protected int _defaultPort_ = 0;
    protected String _hostname_ = null;
    protected InputStream _input_ = null;
    protected OutputStream _output_ = null;
    protected ServerSocketFactory _serverSocketFactory_ = __DEFAULT_SERVER_SOCKET_FACTORY;
    protected SocketFactory _socketFactory_ = __DEFAULT_SOCKET_FACTORY;
    protected Socket _socket_ = null;
    protected int _timeout_ = 0;
    private Charset charset = Charset.defaultCharset();
    private Proxy connProxy;
    protected int connectTimeout = 0;
    private int receiveBufferSize = -1;
    private int sendBufferSize = -1;

    protected void _connectAction_() throws IOException {
        this._socket_.setSoTimeout(this._timeout_);
        this._input_ = this._socket_.getInputStream();
        this._output_ = this._socket_.getOutputStream();
    }

    public void connect(InetAddress host, int port) throws SocketException, IOException {
        this._hostname_ = null;
        this._socket_ = this._socketFactory_.createSocket();
        if (this.receiveBufferSize != -1) {
            this._socket_.setReceiveBufferSize(this.receiveBufferSize);
        }
        if (this.sendBufferSize != -1) {
            this._socket_.setSendBufferSize(this.sendBufferSize);
        }
        this._socket_.connect(new InetSocketAddress(host, port), this.connectTimeout);
        _connectAction_();
    }

    public void connect(String hostname, int port) throws SocketException, IOException {
        connect(InetAddress.getByName(hostname), port);
        this._hostname_ = hostname;
    }

    public void connect(InetAddress host, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
        this._hostname_ = null;
        this._socket_ = this._socketFactory_.createSocket();
        if (this.receiveBufferSize != -1) {
            this._socket_.setReceiveBufferSize(this.receiveBufferSize);
        }
        if (this.sendBufferSize != -1) {
            this._socket_.setSendBufferSize(this.sendBufferSize);
        }
        this._socket_.bind(new InetSocketAddress(localAddr, localPort));
        this._socket_.connect(new InetSocketAddress(host, port), this.connectTimeout);
        _connectAction_();
    }

    public void connect(String hostname, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
        connect(InetAddress.getByName(hostname), port, localAddr, localPort);
        this._hostname_ = hostname;
    }

    public void connect(InetAddress host) throws SocketException, IOException {
        this._hostname_ = null;
        connect(host, this._defaultPort_);
    }

    public void connect(String hostname) throws SocketException, IOException {
        connect(hostname, this._defaultPort_);
        this._hostname_ = hostname;
    }

    public void disconnect() throws IOException {
        closeQuietly(this._socket_);
        closeQuietly(this._input_);
        closeQuietly(this._output_);
        this._socket_ = null;
        this._hostname_ = null;
        this._input_ = null;
        this._output_ = null;
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void closeQuietly(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException e) {
            }
        }
    }

    public boolean isConnected() {
        if (this._socket_ == null) {
            return false;
        }
        return this._socket_.isConnected();
    }

    public boolean isAvailable() {
        if (!isConnected()) {
            return false;
        }
        try {
            if (this._socket_.getInetAddress() == null || this._socket_.getPort() == 0 || this._socket_.getRemoteSocketAddress() == null || this._socket_.isClosed() || this._socket_.isInputShutdown() || this._socket_.isOutputShutdown()) {
                return false;
            }
            this._socket_.getInputStream();
            this._socket_.getOutputStream();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void setDefaultPort(int port) {
        this._defaultPort_ = port;
    }

    public int getDefaultPort() {
        return this._defaultPort_;
    }

    public void setDefaultTimeout(int timeout) {
        this._timeout_ = timeout;
    }

    public int getDefaultTimeout() {
        return this._timeout_;
    }

    public void setSoTimeout(int timeout) throws SocketException {
        this._socket_.setSoTimeout(timeout);
    }

    public void setSendBufferSize(int size) throws SocketException {
        this.sendBufferSize = size;
    }

    protected int getSendBufferSize() {
        return this.sendBufferSize;
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        this.receiveBufferSize = size;
    }

    protected int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    public int getSoTimeout() throws SocketException {
        return this._socket_.getSoTimeout();
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        this._socket_.setTcpNoDelay(on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return this._socket_.getTcpNoDelay();
    }

    public void setKeepAlive(boolean keepAlive) throws SocketException {
        this._socket_.setKeepAlive(keepAlive);
    }

    public boolean getKeepAlive() throws SocketException {
        return this._socket_.getKeepAlive();
    }

    public void setSoLinger(boolean on, int val) throws SocketException {
        this._socket_.setSoLinger(on, val);
    }

    public int getSoLinger() throws SocketException {
        return this._socket_.getSoLinger();
    }

    public int getLocalPort() {
        return this._socket_.getLocalPort();
    }

    public InetAddress getLocalAddress() {
        return this._socket_.getLocalAddress();
    }

    public int getRemotePort() {
        return this._socket_.getPort();
    }

    public InetAddress getRemoteAddress() {
        return this._socket_.getInetAddress();
    }

    public boolean verifyRemote(Socket socket) {
        return socket.getInetAddress().equals(getRemoteAddress());
    }

    public void setSocketFactory(SocketFactory factory) {
        if (factory == null) {
            this._socketFactory_ = __DEFAULT_SOCKET_FACTORY;
        } else {
            this._socketFactory_ = factory;
        }
        this.connProxy = null;
    }

    public void setServerSocketFactory(ServerSocketFactory factory) {
        if (factory == null) {
            this._serverSocketFactory_ = __DEFAULT_SERVER_SOCKET_FACTORY;
        } else {
            this._serverSocketFactory_ = factory;
        }
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public ServerSocketFactory getServerSocketFactory() {
        return this._serverSocketFactory_;
    }

    public void addProtocolCommandListener(ProtocolCommandListener listener) {
        getCommandSupport().addProtocolCommandListener(listener);
    }

    public void removeProtocolCommandListener(ProtocolCommandListener listener) {
        getCommandSupport().removeProtocolCommandListener(listener);
    }

    protected void fireReplyReceived(int replyCode, String reply) {
        if (getCommandSupport().getListenerCount() > 0) {
            getCommandSupport().fireReplyReceived(replyCode, reply);
        }
    }

    protected void fireCommandSent(String command, String message) {
        if (getCommandSupport().getListenerCount() > 0) {
            getCommandSupport().fireCommandSent(command, message);
        }
    }

    protected void createCommandSupport() {
        this.__commandSupport = new ProtocolCommandSupport(this);
    }

    protected ProtocolCommandSupport getCommandSupport() {
        return this.__commandSupport;
    }

    public void setProxy(Proxy proxy) {
        setSocketFactory(new DefaultSocketFactory(proxy));
        this.connProxy = proxy;
    }

    public Proxy getProxy() {
        return this.connProxy;
    }

    public String getCharsetName() {
        return this.charset.name();
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
