package org.apache.commons.net.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public final class DotTerminatedMessageReader extends BufferedReader {
    private static final char CR = '\r';
    private static final int DOT = 46;
    private static final char LF = '\n';
    private boolean atBeginning = true;
    private boolean eof = false;
    private boolean seenCR;

    public DotTerminatedMessageReader(Reader reader) {
        super(reader);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() throws java.io.IOException {
        /*
        r6 = this;
        r5 = 10;
        r2 = 46;
        r1 = -1;
        r3 = r6.lock;
        monitor-enter(r3);
        r4 = r6.eof;	 Catch:{ all -> 0x0038 }
        if (r4 == 0) goto L_0x000f;
    L_0x000c:
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        r0 = r1;
    L_0x000e:
        return r0;
    L_0x000f:
        r0 = super.read();	 Catch:{ all -> 0x0038 }
        if (r0 != r1) goto L_0x001b;
    L_0x0015:
        r2 = 1;
        r6.eof = r2;	 Catch:{ all -> 0x0038 }
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        r0 = r1;
        goto L_0x000e;
    L_0x001b:
        r4 = r6.atBeginning;	 Catch:{ all -> 0x0038 }
        if (r4 == 0) goto L_0x005c;
    L_0x001f:
        r4 = 0;
        r6.atBeginning = r4;	 Catch:{ all -> 0x0038 }
        if (r0 != r2) goto L_0x005c;
    L_0x0024:
        r4 = 2;
        r6.mark(r4);	 Catch:{ all -> 0x0038 }
        r0 = super.read();	 Catch:{ all -> 0x0038 }
        if (r0 != r1) goto L_0x0034;
    L_0x002e:
        r1 = 1;
        r6.eof = r1;	 Catch:{ all -> 0x0038 }
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        r0 = r2;
        goto L_0x000e;
    L_0x0034:
        if (r0 != r2) goto L_0x003b;
    L_0x0036:
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        goto L_0x000e;
    L_0x0038:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        throw r1;
    L_0x003b:
        r4 = 13;
        if (r0 != r4) goto L_0x0056;
    L_0x003f:
        r0 = super.read();	 Catch:{ all -> 0x0038 }
        if (r0 != r1) goto L_0x004b;
    L_0x0045:
        r6.reset();	 Catch:{ all -> 0x0038 }
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        r0 = r2;
        goto L_0x000e;
    L_0x004b:
        if (r0 != r5) goto L_0x0056;
    L_0x004d:
        r2 = 1;
        r6.atBeginning = r2;	 Catch:{ all -> 0x0038 }
        r2 = 1;
        r6.eof = r2;	 Catch:{ all -> 0x0038 }
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        r0 = r1;
        goto L_0x000e;
    L_0x0056:
        r6.reset();	 Catch:{ all -> 0x0038 }
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        r0 = r2;
        goto L_0x000e;
    L_0x005c:
        r1 = r6.seenCR;	 Catch:{ all -> 0x0038 }
        if (r1 == 0) goto L_0x0068;
    L_0x0060:
        r1 = 0;
        r6.seenCR = r1;	 Catch:{ all -> 0x0038 }
        if (r0 != r5) goto L_0x0068;
    L_0x0065:
        r1 = 1;
        r6.atBeginning = r1;	 Catch:{ all -> 0x0038 }
    L_0x0068:
        r1 = 13;
        if (r0 != r1) goto L_0x006f;
    L_0x006c:
        r1 = 1;
        r6.seenCR = r1;	 Catch:{ all -> 0x0038 }
    L_0x006f:
        monitor-exit(r3);	 Catch:{ all -> 0x0038 }
        goto L_0x000e;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.io.DotTerminatedMessageReader.read():int");
    }

    public int read(char[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    public int read(char[] buffer, int offset, int length) throws IOException {
        if (length < 1) {
            return 0;
        }
        synchronized (this.lock) {
            int ch = read();
            if (ch == -1) {
                return -1;
            }
            int off = offset;
            int i = offset;
            while (true) {
                offset = i + 1;
                buffer[i] = (char) ch;
                length--;
                if (length <= 0) {
                    break;
                }
                ch = read();
                if (ch == -1) {
                    break;
                }
                i = offset;
            }
            int i2 = offset - off;
            return i2;
        }
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            if (!this.eof) {
                do {
                } while (read() != -1);
            }
            this.eof = true;
            this.atBeginning = false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String readLine() throws java.io.IOException {
        /*
        r6 = this;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r4 = r6.lock;
        monitor-enter(r4);
    L_0x0008:
        r0 = r6.read();	 Catch:{ all -> 0x0029 }
        r3 = -1;
        if (r0 == r3) goto L_0x002c;
    L_0x000f:
        r3 = 10;
        if (r0 != r3) goto L_0x0024;
    L_0x0013:
        r3 = r6.atBeginning;	 Catch:{ all -> 0x0029 }
        if (r3 == 0) goto L_0x0024;
    L_0x0017:
        r3 = 0;
        r5 = r1.length();	 Catch:{ all -> 0x0029 }
        r5 = r5 + -1;
        r2 = r1.substring(r3, r5);	 Catch:{ all -> 0x0029 }
        monitor-exit(r4);	 Catch:{ all -> 0x0029 }
    L_0x0023:
        return r2;
    L_0x0024:
        r3 = (char) r0;	 Catch:{ all -> 0x0029 }
        r1.append(r3);	 Catch:{ all -> 0x0029 }
        goto L_0x0008;
    L_0x0029:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0029 }
        throw r3;
    L_0x002c:
        monitor-exit(r4);	 Catch:{ all -> 0x0029 }
        r2 = r1.toString();
        r3 = r2.length();
        if (r3 != 0) goto L_0x0023;
    L_0x0037:
        r2 = 0;
        goto L_0x0023;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.io.DotTerminatedMessageReader.readLine():java.lang.String");
    }
}
