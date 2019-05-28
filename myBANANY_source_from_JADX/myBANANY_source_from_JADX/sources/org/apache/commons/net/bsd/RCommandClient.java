package org.apache.commons.net.bsd;

import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.commons.net.io.SocketInputStream;

public class RCommandClient extends RExecClient {
    public static final int DEFAULT_PORT = 514;
    public static final int MAX_CLIENT_PORT = 1023;
    public static final int MIN_CLIENT_PORT = 512;

    InputStream _createErrorStream() throws IOException {
        ServerSocket server = null;
        int localPort = MAX_CLIENT_PORT;
        while (localPort >= 512) {
            try {
                server = this._serverSocketFactory_.createServerSocket(localPort, 1, getLocalAddress());
                break;
            } catch (SocketException e) {
                localPort--;
            }
        }
        if (server == null) {
            throw new BindException("All ports in use.");
        }
        this._output_.write(Integer.toString(server.getLocalPort()).getBytes("UTF-8"));
        this._output_.write(0);
        this._output_.flush();
        Socket socket = server.accept();
        server.close();
        if (!isRemoteVerificationEnabled() || verifyRemote(socket)) {
            return new SocketInputStream(socket, socket.getInputStream());
        }
        socket.close();
        throw new IOException("Security violation: unexpected connection attempt by " + socket.getInetAddress().getHostAddress());
    }

    public RCommandClient() {
        setDefaultPort(DEFAULT_PORT);
    }

    public void connect(InetAddress host, int port, InetAddress localAddr) throws SocketException, BindException, IOException {
        int localPort = MAX_CLIENT_PORT;
        while (localPort >= 512) {
            try {
                this._socket_ = this._socketFactory_.createSocket(host, port, localAddr, localPort);
                break;
            } catch (BindException e) {
            } catch (SocketException e2) {
            }
        }
        if (localPort < 512) {
            throw new BindException("All ports in use or insufficient permssion.");
        }
        _connectAction_();
        return;
        localPort--;
    }

    public void connect(InetAddress host, int port) throws SocketException, IOException {
        connect(host, port, InetAddress.getLocalHost());
    }

    public void connect(String hostname, int port) throws SocketException, IOException, UnknownHostException {
        connect(InetAddress.getByName(hostname), port, InetAddress.getLocalHost());
    }

    public void connect(String hostname, int port, InetAddress localAddr) throws SocketException, IOException {
        connect(InetAddress.getByName(hostname), port, localAddr);
    }

    public void connect(InetAddress host, int port, InetAddress localAddr, int localPort) throws SocketException, IOException, IllegalArgumentException {
        if (localPort < 512 || localPort > MAX_CLIENT_PORT) {
            throw new IllegalArgumentException("Invalid port number " + localPort);
        }
        super.connect(host, port, localAddr, localPort);
    }

    public void connect(String hostname, int port, InetAddress localAddr, int localPort) throws SocketException, IOException, IllegalArgumentException, UnknownHostException {
        if (localPort < 512 || localPort > MAX_CLIENT_PORT) {
            throw new IllegalArgumentException("Invalid port number " + localPort);
        }
        super.connect(hostname, port, localAddr, localPort);
    }

    public void rcommand(String localUsername, String remoteUsername, String command, boolean separateErrorStream) throws IOException {
        rexec(localUsername, remoteUsername, command, separateErrorStream);
    }

    public void rcommand(String localUsername, String remoteUsername, String command) throws IOException {
        rcommand(localUsername, remoteUsername, command, false);
    }
}
