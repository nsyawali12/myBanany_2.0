package org.apache.commons.net.io;

import java.io.BufferedReader;
import java.io.Reader;

public final class CRLFLineReader extends BufferedReader {
    private static final char CR = '\r';
    private static final char LF = '\n';

    public CRLFLineReader(Reader reader) {
        super(reader);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String readLine() throws java.io.IOException {
        /*
        r7 = this;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r1 = 0;
        r5 = r7.lock;
        monitor-enter(r5);
    L_0x0009:
        r0 = r7.read();	 Catch:{ all -> 0x002d }
        r4 = -1;
        if (r0 == r4) goto L_0x0032;
    L_0x0010:
        if (r1 == 0) goto L_0x0023;
    L_0x0012:
        r4 = 10;
        if (r0 != r4) goto L_0x0023;
    L_0x0016:
        r4 = 0;
        r6 = r2.length();	 Catch:{ all -> 0x002d }
        r6 = r6 + -1;
        r3 = r2.substring(r4, r6);	 Catch:{ all -> 0x002d }
        monitor-exit(r5);	 Catch:{ all -> 0x002d }
    L_0x0022:
        return r3;
    L_0x0023:
        r4 = 13;
        if (r0 != r4) goto L_0x0030;
    L_0x0027:
        r1 = 1;
    L_0x0028:
        r4 = (char) r0;	 Catch:{ all -> 0x002d }
        r2.append(r4);	 Catch:{ all -> 0x002d }
        goto L_0x0009;
    L_0x002d:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x002d }
        throw r4;
    L_0x0030:
        r1 = 0;
        goto L_0x0028;
    L_0x0032:
        monitor-exit(r5);	 Catch:{ all -> 0x002d }
        r3 = r2.toString();
        r4 = r3.length();
        if (r4 != 0) goto L_0x0022;
    L_0x003d:
        r3 = 0;
        goto L_0x0022;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.io.CRLFLineReader.readLine():java.lang.String");
    }
}
