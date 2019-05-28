package org.apache.commons.net.io;

import java.io.IOException;
import java.io.Writer;

public final class DotTerminatedMessageWriter extends Writer {
    private static final int __LAST_WAS_CR_STATE = 1;
    private static final int __LAST_WAS_NL_STATE = 2;
    private static final int __NOTHING_SPECIAL_STATE = 0;
    private Writer __output;
    private int __state = 0;

    public DotTerminatedMessageWriter(Writer output) {
        super(output);
        this.__output = output;
    }

    public void write(int ch) throws IOException {
        synchronized (this.lock) {
            switch (ch) {
                case 10:
                    if (this.__state != 1) {
                        this.__output.write(13);
                    }
                    this.__output.write(10);
                    this.__state = 2;
                    return;
                case 13:
                    this.__state = 1;
                    this.__output.write(13);
                    return;
                case 46:
                    if (this.__state == 2) {
                        this.__output.write(46);
                        break;
                    }
                    break;
            }
            this.__state = 0;
            this.__output.write(ch);
        }
    }

    public void write(char[] buffer, int offset, int length) throws IOException {
        Throwable th;
        synchronized (this.lock) {
            int length2 = length;
            int offset2 = offset;
            while (true) {
                length = length2 - 1;
                if (length2 > 0) {
                    offset = offset2 + 1;
                    try {
                        write(buffer[offset2]);
                        length2 = length;
                        offset2 = offset;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } else {
                    try {
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        offset = offset2;
                        throw th;
                    }
                }
            }
        }
    }

    public void write(char[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    public void write(String string) throws IOException {
        write(string.toCharArray());
    }

    public void write(String string, int offset, int length) throws IOException {
        write(string.toCharArray(), offset, length);
    }

    public void flush() throws IOException {
        synchronized (this.lock) {
            this.__output.flush();
        }
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            if (this.__output == null) {
                return;
            }
            if (this.__state == 1) {
                this.__output.write(10);
            } else if (this.__state != 2) {
                this.__output.write("\r\n");
            }
            this.__output.write(".\r\n");
            this.__output.flush();
            this.__output = null;
        }
    }
}
