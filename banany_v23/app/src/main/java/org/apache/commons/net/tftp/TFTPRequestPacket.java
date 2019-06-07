package org.apache.commons.net.tftp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Locale;

public abstract class TFTPRequestPacket extends TFTPPacket {
    private static final byte[][] _modeBytes = new byte[][]{new byte[]{(byte) 110, (byte) 101, (byte) 116, (byte) 97, (byte) 115, (byte) 99, (byte) 105, (byte) 105, (byte) 0}, new byte[]{(byte) 111, (byte) 99, (byte) 116, (byte) 101, (byte) 116, (byte) 0}};
    static final String[] _modeStrings = new String[]{"netascii", "octet"};
    private final String _filename;
    private final int _mode;

    TFTPRequestPacket(InetAddress destination, int port, int type, String filename, int mode) {
        super(type, destination, port);
        this._filename = filename;
        this._mode = mode;
    }

    TFTPRequestPacket(int type, DatagramPacket datagram) throws TFTPPacketException {
        super(type, datagram.getAddress(), datagram.getPort());
        byte[] data = datagram.getData();
        if (getType() != data[1]) {
            throw new TFTPPacketException("TFTP operator code does not match type.");
        }
        StringBuilder buffer = new StringBuilder();
        int index = 2;
        int length = datagram.getLength();
        while (index < length && data[index] != (byte) 0) {
            buffer.append((char) data[index]);
            index++;
        }
        this._filename = buffer.toString();
        if (index >= length) {
            throw new TFTPPacketException("Bad filename and mode format.");
        }
        buffer.setLength(0);
        index++;
        while (index < length && data[index] != (byte) 0) {
            buffer.append((char) data[index]);
            index++;
        }
        String modeString = buffer.toString().toLowerCase(Locale.ENGLISH);
        length = _modeStrings.length;
        int mode = 0;
        index = 0;
        while (index < length) {
            if (modeString.equals(_modeStrings[index])) {
                mode = index;
                break;
            }
            index++;
        }
        this._mode = mode;
        if (index >= length) {
            throw new TFTPPacketException("Unrecognized TFTP transfer mode: " + modeString);
        }
    }

    final DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data) {
        int fileLength = this._filename.length();
        int modeLength = _modeBytes[this._mode].length;
        data[0] = (byte) 0;
        data[1] = (byte) this._type;
        System.arraycopy(this._filename.getBytes(), 0, data, 2, fileLength);
        data[fileLength + 2] = (byte) 0;
        System.arraycopy(_modeBytes[this._mode], 0, data, fileLength + 3, modeLength);
        datagram.setAddress(this._address);
        datagram.setPort(this._port);
        datagram.setData(data);
        datagram.setLength((fileLength + modeLength) + 3);
        return datagram;
    }

    public final DatagramPacket newDatagram() {
        int fileLength = this._filename.length();
        int modeLength = _modeBytes[this._mode].length;
        byte[] data = new byte[((fileLength + modeLength) + 4)];
        data[0] = (byte) 0;
        data[1] = (byte) this._type;
        System.arraycopy(this._filename.getBytes(), 0, data, 2, fileLength);
        data[fileLength + 2] = (byte) 0;
        System.arraycopy(_modeBytes[this._mode], 0, data, fileLength + 3, modeLength);
        return new DatagramPacket(data, data.length, this._address, this._port);
    }

    public final int getMode() {
        return this._mode;
    }

    public final String getFilename() {
        return this._filename;
    }
}
