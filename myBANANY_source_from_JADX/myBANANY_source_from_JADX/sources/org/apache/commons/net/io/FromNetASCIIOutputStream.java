package org.apache.commons.net.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class FromNetASCIIOutputStream extends FilterOutputStream {
    private boolean __lastWasCR = false;

    public FromNetASCIIOutputStream(OutputStream output) {
        super(output);
    }

    private void __write(int ch) throws IOException {
        switch (ch) {
            case 10:
                if (this.__lastWasCR) {
                    this.out.write(FromNetASCIIInputStream._lineSeparatorBytes);
                    this.__lastWasCR = false;
                    return;
                }
                this.__lastWasCR = false;
                this.out.write(10);
                return;
            case 13:
                this.__lastWasCR = true;
                return;
            default:
                if (this.__lastWasCR) {
                    this.out.write(13);
                    this.__lastWasCR = false;
                }
                this.out.write(ch);
                return;
        }
    }

    public synchronized void write(int ch) throws IOException {
        if (FromNetASCIIInputStream._noConversionRequired) {
            this.out.write(ch);
        } else {
            __write(ch);
        }
    }

    public synchronized void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    public synchronized void write(byte[] buffer, int offset, int length) throws IOException {
        if (FromNetASCIIInputStream._noConversionRequired) {
            this.out.write(buffer, offset, length);
        } else {
            int length2 = length;
            int offset2 = offset;
            while (true) {
                length = length2 - 1;
                if (length2 <= 0) {
                    break;
                }
                offset = offset2 + 1;
                __write(buffer[offset2]);
                length2 = length;
                offset2 = offset;
            }
            offset = offset2;
        }
    }

    public synchronized void close() throws IOException {
        if (FromNetASCIIInputStream._noConversionRequired) {
            super.close();
        } else {
            if (this.__lastWasCR) {
                this.out.write(13);
            }
            super.close();
        }
    }
}
