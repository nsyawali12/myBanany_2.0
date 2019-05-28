package org.apache.commons.net.telnet;

import java.io.IOException;
import java.io.OutputStream;

final class TelnetOutputStream extends OutputStream {
    private final TelnetClient __client;
    private final boolean __convertCRtoCRLF = true;
    private boolean __lastWasCR = false;

    TelnetOutputStream(TelnetClient client) {
        this.__client = client;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(int r6) throws java.io.IOException {
        /*
        r5 = this;
        r4 = 10;
        r3 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r1 = r5.__client;
        monitor-enter(r1);
        r6 = r6 & 255;
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 0;
        r0 = r0._requestedWont(r2);	 Catch:{ all -> 0x0031 }
        if (r0 == 0) goto L_0x0065;
    L_0x0012:
        r0 = r5.__lastWasCR;	 Catch:{ all -> 0x0031 }
        if (r0 == 0) goto L_0x0024;
    L_0x0016:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 10;
        r0._sendByte(r2);	 Catch:{ all -> 0x0031 }
        if (r6 != r4) goto L_0x0024;
    L_0x001f:
        r0 = 0;
        r5.__lastWasCR = r0;	 Catch:{ all -> 0x0031 }
        monitor-exit(r1);	 Catch:{ all -> 0x0031 }
    L_0x0023:
        return;
    L_0x0024:
        switch(r6) {
            case 10: goto L_0x003f;
            case 13: goto L_0x0034;
            case 255: goto L_0x0053;
            default: goto L_0x0027;
        };	 Catch:{ all -> 0x0031 }
    L_0x0027:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r0._sendByte(r6);	 Catch:{ all -> 0x0031 }
        r0 = 0;
        r5.__lastWasCR = r0;	 Catch:{ all -> 0x0031 }
    L_0x002f:
        monitor-exit(r1);	 Catch:{ all -> 0x0031 }
        goto L_0x0023;
    L_0x0031:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0031 }
        throw r0;
    L_0x0034:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 13;
        r0._sendByte(r2);	 Catch:{ all -> 0x0031 }
        r0 = 1;
        r5.__lastWasCR = r0;	 Catch:{ all -> 0x0031 }
        goto L_0x002f;
    L_0x003f:
        r0 = r5.__lastWasCR;	 Catch:{ all -> 0x0031 }
        if (r0 != 0) goto L_0x004a;
    L_0x0043:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 13;
        r0._sendByte(r2);	 Catch:{ all -> 0x0031 }
    L_0x004a:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r0._sendByte(r6);	 Catch:{ all -> 0x0031 }
        r0 = 0;
        r5.__lastWasCR = r0;	 Catch:{ all -> 0x0031 }
        goto L_0x002f;
    L_0x0053:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r0._sendByte(r2);	 Catch:{ all -> 0x0031 }
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r0._sendByte(r2);	 Catch:{ all -> 0x0031 }
        r0 = 0;
        r5.__lastWasCR = r0;	 Catch:{ all -> 0x0031 }
        goto L_0x002f;
    L_0x0065:
        if (r6 != r3) goto L_0x0074;
    L_0x0067:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r0._sendByte(r6);	 Catch:{ all -> 0x0031 }
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r2 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r0._sendByte(r2);	 Catch:{ all -> 0x0031 }
        goto L_0x002f;
    L_0x0074:
        r0 = r5.__client;	 Catch:{ all -> 0x0031 }
        r0._sendByte(r6);	 Catch:{ all -> 0x0031 }
        goto L_0x002f;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.telnet.TelnetOutputStream.write(int):void");
    }

    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        Throwable th;
        synchronized (this.__client) {
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

    public void flush() throws IOException {
        this.__client._flushOutputStream();
    }

    public void close() throws IOException {
        this.__client._closeOutputStream();
    }
}
