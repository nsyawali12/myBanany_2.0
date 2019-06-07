package org.apache.commons.net.tftp;

import android.support.v4.internal.view.SupportMenu;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;

public class TFTPClient extends TFTP {
    public static final int DEFAULT_MAX_TIMEOUTS = 5;
    private int __maxTimeouts = 5;

    public void setMaxTimeouts(int numTimeouts) {
        if (numTimeouts < 1) {
            this.__maxTimeouts = 1;
        } else {
            this.__maxTimeouts = numTimeouts;
        }
    }

    public int getMaxTimeouts() {
        return this.__maxTimeouts;
    }

    public int receiveFile(String filename, int mode, OutputStream output, InetAddress host, int port) throws IOException {
        TFTPPacket ack = new TFTPAckPacket(host, port, 0);
        beginBufferedOps();
        int bytesRead = 0;
        int hostPort = 0;
        int lastBlock = 0;
        int dataLength = 0;
        int block = 1;
        if (mode == 0) {
            output = new FromNetASCIIOutputStream(output);
        }
        TFTPPacket sent = new TFTPReadRequestPacket(host, port, filename, mode);
        do {
            bufferedSend(sent);
            do {
                try {
                    TFTPPacket received = bufferedReceive();
                    if (lastBlock == 0) {
                        hostPort = received.getPort();
                        ack.setPort(hostPort);
                        if (!host.equals(received.getAddress())) {
                            host = received.getAddress();
                            ack.setAddress(host);
                            sent.setAddress(host);
                        }
                    }
                    if (host.equals(received.getAddress()) && received.getPort() == hostPort) {
                        switch (received.getType()) {
                            case 3:
                                TFTPDataPacket data = (TFTPDataPacket) received;
                                dataLength = data.getDataLength();
                                lastBlock = data.getBlockNumber();
                                if (lastBlock != block) {
                                    discardPackets();
                                    break;
                                }
                                try {
                                    output.write(data.getData(), data.getDataOffset(), dataLength);
                                    block++;
                                    if (block > 65535) {
                                        block = 0;
                                    }
                                    ack.setBlockNumber(lastBlock);
                                    sent = ack;
                                    bytesRead += dataLength;
                                    break;
                                } catch (IOException e) {
                                    bufferedSend(new TFTPErrorPacket(host, hostPort, 3, "File write failed."));
                                    endBufferedOps();
                                    throw e;
                                }
                            case 5:
                                TFTPErrorPacket error = (TFTPErrorPacket) received;
                                endBufferedOps();
                                throw new IOException("Error code " + error.getError() + " received: " + error.getMessage());
                            default:
                                endBufferedOps();
                                throw new IOException("Received unexpected packet type.");
                        }
                    }
                    bufferedSend(new TFTPErrorPacket(received.getAddress(), received.getPort(), 5, "Unexpected host or port."));
                } catch (SocketException e2) {
                    if (0 + 1 >= this.__maxTimeouts) {
                        endBufferedOps();
                        throw new IOException("Connection timed out.");
                    }
                } catch (InterruptedIOException e3) {
                    if (0 + 1 >= this.__maxTimeouts) {
                        endBufferedOps();
                        throw new IOException("Connection timed out.");
                    }
                } catch (TFTPPacketException e4) {
                    endBufferedOps();
                    throw new IOException("Bad packet: " + e4.getMessage());
                }
            } while (lastBlock != (block == 0 ? SupportMenu.USER_MASK : block - 1));
        } while (dataLength == 512);
        bufferedSend(sent);
        endBufferedOps();
        return bytesRead;
    }

    public int receiveFile(String filename, int mode, OutputStream output, String hostname, int port) throws UnknownHostException, IOException {
        return receiveFile(filename, mode, output, InetAddress.getByName(hostname), port);
    }

    public int receiveFile(String filename, int mode, OutputStream output, InetAddress host) throws IOException {
        return receiveFile(filename, mode, output, host, 69);
    }

    public int receiveFile(String filename, int mode, OutputStream output, String hostname) throws UnknownHostException, IOException {
        return receiveFile(filename, mode, output, InetAddress.getByName(hostname), 69);
    }

    public void sendFile(String filename, int mode, InputStream input, InetAddress host, int port) throws IOException {
        TFTPPacket data = new TFTPDataPacket(host, port, 0, this._sendBuffer, 4, 0);
        boolean justStarted = true;
        beginBufferedOps();
        int totalThisPacket = 0;
        int bytesRead = 0;
        int hostPort = 0;
        int lastBlock = 0;
        int dataLength = 0;
        int block = 0;
        boolean lastAckWait = false;
        if (mode == 0) {
            input = new ToNetASCIIInputStream(input);
        }
        TFTPPacket tFTPWriteRequestPacket = new TFTPWriteRequestPacket(host, port, filename, mode);
        while (true) {
            TFTPPacket sent;
            bufferedSend(sent);
            while (true) {
                try {
                    TFTPPacket received = bufferedReceive();
                    if (justStarted) {
                        justStarted = false;
                        hostPort = received.getPort();
                        data.setPort(hostPort);
                        if (!host.equals(received.getAddress())) {
                            host = received.getAddress();
                            data.setAddress(host);
                            sent.setAddress(host);
                        }
                    }
                    if (host.equals(received.getAddress()) && received.getPort() == hostPort) {
                        switch (received.getType()) {
                            case 4:
                                if (((TFTPAckPacket) received).getBlockNumber() == block) {
                                    block++;
                                    if (block > SupportMenu.USER_MASK) {
                                        block = 0;
                                    }
                                    if (lastAckWait) {
                                        break;
                                    }
                                    dataLength = 512;
                                    int offset = 4;
                                    totalThisPacket = 0;
                                    while (dataLength > 0) {
                                        bytesRead = input.read(this._sendBuffer, offset, dataLength);
                                        if (bytesRead <= 0) {
                                            if (totalThisPacket < 512) {
                                                lastAckWait = true;
                                            }
                                            data.setBlockNumber(block);
                                            data.setData(this._sendBuffer, 4, totalThisPacket);
                                            sent = data;
                                            break;
                                        }
                                        offset += bytesRead;
                                        dataLength -= bytesRead;
                                        totalThisPacket += bytesRead;
                                    }
                                    if (totalThisPacket < 512) {
                                        lastAckWait = true;
                                    }
                                    data.setBlockNumber(block);
                                    data.setData(this._sendBuffer, 4, totalThisPacket);
                                    sent = data;
                                } else {
                                    discardPackets();
                                }
                            case 5:
                                TFTPErrorPacket error = (TFTPErrorPacket) received;
                                endBufferedOps();
                                throw new IOException("Error code " + error.getError() + " received: " + error.getMessage());
                            default:
                                endBufferedOps();
                                throw new IOException("Received unexpected packet type.");
                        }
                        if (totalThisPacket > 0) {
                        }
                    } else {
                        bufferedSend(new TFTPErrorPacket(received.getAddress(), received.getPort(), 5, "Unexpected host or port."));
                        if (totalThisPacket > 0 && !lastAckWait) {
                            endBufferedOps();
                            return;
                        }
                    }
                } catch (SocketException e) {
                    if (0 + 1 >= this.__maxTimeouts) {
                        endBufferedOps();
                        throw new IOException("Connection timed out.");
                    }
                } catch (InterruptedIOException e2) {
                    if (0 + 1 >= this.__maxTimeouts) {
                        endBufferedOps();
                        throw new IOException("Connection timed out.");
                    }
                } catch (TFTPPacketException e3) {
                    endBufferedOps();
                    throw new IOException("Bad packet: " + e3.getMessage());
                }
            }
        }
    }

    public void sendFile(String filename, int mode, InputStream input, String hostname, int port) throws UnknownHostException, IOException {
        sendFile(filename, mode, input, InetAddress.getByName(hostname), port);
    }

    public void sendFile(String filename, int mode, InputStream input, InetAddress host) throws IOException {
        sendFile(filename, mode, input, host, 69);
    }

    public void sendFile(String filename, int mode, InputStream input, String hostname) throws UnknownHostException, IOException {
        sendFile(filename, mode, input, InetAddress.getByName(hostname), 69);
    }
}
