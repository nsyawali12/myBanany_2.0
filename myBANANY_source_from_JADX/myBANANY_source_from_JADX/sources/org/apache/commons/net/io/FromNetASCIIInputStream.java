package org.apache.commons.net.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;

public final class FromNetASCIIInputStream extends PushbackInputStream {
    static final String _lineSeparator = System.getProperty("line.separator");
    static final byte[] _lineSeparatorBytes;
    static final boolean _noConversionRequired = _lineSeparator.equals("\r\n");
    private int __length = 0;

    static {
        try {
            _lineSeparatorBytes = _lineSeparator.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Broken JVM - cannot find US-ASCII charset!", e);
        }
    }

    public static final boolean isConversionRequired() {
        return !_noConversionRequired;
    }

    public FromNetASCIIInputStream(InputStream input) {
        super(input, _lineSeparatorBytes.length + 1);
    }

    private int __read() throws IOException {
        int ch = super.read();
        if (ch == 13) {
            ch = super.read();
            if (ch == 10) {
                unread(_lineSeparatorBytes);
                ch = super.read();
                this.__length--;
            } else if (ch == -1) {
                return 13;
            } else {
                unread(ch);
                return 13;
            }
        }
        return ch;
    }

    public int read() throws IOException {
        if (_noConversionRequired) {
            return super.read();
        }
        return __read();
    }

    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (_noConversionRequired) {
            return super.read(buffer, offset, length);
        }
        if (length < 1) {
            return 0;
        }
        int ch = available();
        if (length <= ch) {
            ch = length;
        }
        this.__length = ch;
        if (this.__length < 1) {
            this.__length = 1;
        }
        ch = __read();
        if (ch == -1) {
            return -1;
        }
        int offset2;
        int off = offset;
        while (true) {
            offset2 = offset + 1;
            buffer[offset] = (byte) ch;
            int i = this.__length - 1;
            this.__length = i;
            if (i <= 0) {
                break;
            }
            ch = __read();
            if (ch == -1) {
                break;
            }
            offset = offset2;
        }
        offset = offset2;
        return offset2 - off;
    }

    public int available() throws IOException {
        if (this.in != null) {
            return (this.buf.length - this.pos) + this.in.available();
        }
        throw new IOException("Stream closed");
    }
}
