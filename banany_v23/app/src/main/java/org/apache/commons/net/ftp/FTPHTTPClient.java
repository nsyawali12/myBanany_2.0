package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.util.Base64;

public class FTPHTTPClient extends FTPClient {
    private static final byte[] CRLF = new byte[]{(byte) 13, (byte) 10};
    private final Base64 base64;
    private final String proxyHost;
    private final String proxyPassword;
    private final int proxyPort;
    private final String proxyUsername;
    private String tunnelHost;

    public FTPHTTPClient(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this.base64 = new Base64();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUser;
        this.proxyPassword = proxyPass;
        this.tunnelHost = null;
    }

    public FTPHTTPClient(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null, null);
    }

    @Deprecated
    protected Socket _openDataConnection_(int command, String arg) throws IOException {
        return super._openDataConnection_(command, arg);
    }

    protected Socket _openDataConnection_(String command, String arg) throws IOException {
        if (getDataConnectionMode() != 2) {
            throw new IllegalStateException("Only passive connection mode supported");
        }
        boolean attemptEPSV;
        String passiveHost;
        boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;
        if (isUseEPSVwithIPv4() || isInet6Address) {
            attemptEPSV = true;
        } else {
            attemptEPSV = false;
        }
        if (attemptEPSV && epsv() == FTPReply.ENTERING_EPSV_MODE) {
            _parseExtendedPassiveModeReply((String) this._replyLines.get(0));
            passiveHost = this.tunnelHost;
        } else if (isInet6Address) {
            return null;
        } else {
            if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
                return null;
            }
            _parsePassiveModeReply((String) this._replyLines.get(0));
            passiveHost = getPassiveHost();
        }
        Socket socket = this._socketFactory_.createSocket(this.proxyHost, this.proxyPort);
        tunnelHandshake(passiveHost, getPassivePort(), socket.getInputStream(), socket.getOutputStream());
        if (getRestartOffset() > 0 && !restart(getRestartOffset())) {
            socket.close();
            return null;
        } else if (FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
            return socket;
        } else {
            socket.close();
            return null;
        }
    }

    public void connect(String host, int port) throws SocketException, IOException {
        this._socket_ = this._socketFactory_.createSocket(this.proxyHost, this.proxyPort);
        this._input_ = this._socket_.getInputStream();
        this._output_ = this._socket_.getOutputStream();
        try {
            super._connectAction_(tunnelHandshake(host, port, this._input_, this._output_));
        } catch (Exception e) {
            IOException ioe = new IOException("Could not connect to " + host + " using port " + port);
            ioe.initCause(e);
            throw ioe;
        }
    }

    private BufferedReader tunnelHandshake(String host, int port, InputStream input, OutputStream output) throws IOException, UnsupportedEncodingException {
        String connectString = "CONNECT " + host + ":" + port + " HTTP/1.1";
        String hostString = "Host: " + host + ":" + port;
        this.tunnelHost = host;
        output.write(connectString.getBytes("UTF-8"));
        output.write(CRLF);
        output.write(hostString.getBytes("UTF-8"));
        output.write(CRLF);
        if (!(this.proxyUsername == null || this.proxyPassword == null)) {
            OutputStream outputStream = output;
            outputStream.write(("Proxy-Authorization: Basic " + this.base64.encodeToString((this.proxyUsername + ":" + this.proxyPassword).getBytes("UTF-8"))).getBytes("UTF-8"));
        }
        output.write(CRLF);
        List<String> response = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, getCharsetName()));
        String line = reader.readLine();
        while (line != null && line.length() > 0) {
            response.add(line);
            line = reader.readLine();
        }
        if (response.size() == 0) {
            throw new IOException("No response from proxy");
        }
        String resp = (String) response.get(0);
        if (!resp.startsWith("HTTP/") || resp.length() < 12) {
            throw new IOException("Invalid response from proxy: " + resp);
        }
        if ("200".equals(resp.substring(9, 12))) {
            return reader;
        }
        StringBuilder msg = new StringBuilder();
        msg.append("HTTPTunnelConnector: connection failed\r\n");
        msg.append("Response received from the proxy:\r\n");
        for (String line2 : response) {
            msg.append(line2);
            msg.append("\r\n");
        }
        throw new IOException(msg.toString());
    }
}
