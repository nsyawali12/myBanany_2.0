package org.apache.commons.net.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class ToNetASCIIOutputStream extends FilterOutputStream {
    private boolean __lastWasCR = false;

    public ToNetASCIIOutputStream(OutputStream output) {
        super(output);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void write(int r3) throws java.io.IOException {
        /*
        r2 = this;
        monitor-enter(r2);
        switch(r3) {
            case 10: goto L_0x001c;
            case 11: goto L_0x0004;
            case 12: goto L_0x0004;
            case 13: goto L_0x000e;
            default: goto L_0x0004;
        };
    L_0x0004:
        r0 = 0;
        r2.__lastWasCR = r0;	 Catch:{ all -> 0x0019 }
        r0 = r2.out;	 Catch:{ all -> 0x0019 }
        r0.write(r3);	 Catch:{ all -> 0x0019 }
    L_0x000c:
        monitor-exit(r2);
        return;
    L_0x000e:
        r0 = 1;
        r2.__lastWasCR = r0;	 Catch:{ all -> 0x0019 }
        r0 = r2.out;	 Catch:{ all -> 0x0019 }
        r1 = 13;
        r0.write(r1);	 Catch:{ all -> 0x0019 }
        goto L_0x000c;
    L_0x0019:
        r0 = move-exception;
        monitor-exit(r2);
        throw r0;
    L_0x001c:
        r0 = r2.__lastWasCR;	 Catch:{ all -> 0x0019 }
        if (r0 != 0) goto L_0x0004;
    L_0x0020:
        r0 = r2.out;	 Catch:{ all -> 0x0019 }
        r1 = 13;
        r0.write(r1);	 Catch:{ all -> 0x0019 }
        goto L_0x0004;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.io.ToNetASCIIOutputStream.write(int):void");
    }

    public synchronized void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    public synchronized void write(byte[] buffer, int offset, int length) throws IOException {
        int length2 = length;
        int offset2 = offset;
        while (true) {
            length = length2 - 1;
            if (length2 > 0) {
                offset = offset2 + 1;
                write(buffer[offset2]);
                length2 = length;
                offset2 = offset;
            }
        }
    }
}
