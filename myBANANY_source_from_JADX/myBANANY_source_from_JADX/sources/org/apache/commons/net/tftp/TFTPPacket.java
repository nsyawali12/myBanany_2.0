package org.apache.commons.net.tftp;

import java.net.DatagramPacket;
import java.net.InetAddress;

public abstract class TFTPPacket {
    public static final int ACKNOWLEDGEMENT = 4;
    public static final int DATA = 3;
    public static final int ERROR = 5;
    static final int MIN_PACKET_SIZE = 4;
    public static final int READ_REQUEST = 1;
    public static final int SEGMENT_SIZE = 512;
    public static final int WRITE_REQUEST = 2;
    InetAddress _address;
    int _port;
    int _type;

    abstract DatagramPacket _newDatagram(DatagramPacket datagramPacket, byte[] bArr);

    public abstract DatagramPacket newDatagram();

    public static final TFTPPacket newTFTPPacket(DatagramPacket datagram) throws TFTPPacketException {
        if (datagram.getLength() < 4) {
            throw new TFTPPacketException("Bad packet. Datagram data length is too short.");
        }
        switch (datagram.getData()[1]) {
            case (byte) 1:
                return new TFTPReadRequestPacket(datagram);
            case (byte) 2:
                return new TFTPWriteRequestPacket(datagram);
            case (byte) 3:
                return new TFTPDataPacket(datagram);
            case (byte) 4:
                return new TFTPAckPacket(datagram);
            case (byte) 5:
                return new TFTPErrorPacket(datagram);
            default:
                throw new TFTPPacketException("Bad packet.  Invalid TFTP operator code.");
        }
    }

    TFTPPacket(int type, InetAddress address, int port) {
        this._type = type;
        this._address = address;
        this._port = port;
    }

    public final int getType() {
        return this._type;
    }

    public final InetAddress getAddress() {
        return this._address;
    }

    public final int getPort() {
        return this._port;
    }

    public final void setPort(int port) {
        this._port = port;
    }

    public final void setAddress(InetAddress address) {
        this._address = address;
    }
}
