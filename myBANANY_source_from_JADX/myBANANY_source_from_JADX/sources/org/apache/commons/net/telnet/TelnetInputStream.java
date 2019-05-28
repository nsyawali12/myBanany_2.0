package org.apache.commons.net.telnet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

final class TelnetInputStream extends BufferedInputStream implements Runnable {
    private static final int EOF = -1;
    private static final int WOULD_BLOCK = -2;
    static final int _STATE_CR = 8;
    static final int _STATE_DATA = 0;
    static final int _STATE_DO = 4;
    static final int _STATE_DONT = 5;
    static final int _STATE_IAC = 1;
    static final int _STATE_IAC_SB = 9;
    static final int _STATE_SB = 6;
    static final int _STATE_SE = 7;
    static final int _STATE_WILL = 2;
    static final int _STATE_WONT = 3;
    private int __bytesAvailable;
    private final TelnetClient __client;
    private boolean __hasReachedEOF;
    private IOException __ioException;
    private volatile boolean __isClosed;
    private final int[] __queue;
    private int __queueHead;
    private int __queueTail;
    private boolean __readIsWaiting;
    private int __receiveState;
    private final int[] __suboption;
    private int __suboption_count;
    private final Thread __thread;
    private volatile boolean __threaded;

    TelnetInputStream(InputStream input, TelnetClient client, boolean readerThread) {
        super(input);
        this.__suboption = new int[512];
        this.__suboption_count = 0;
        this.__client = client;
        this.__receiveState = 0;
        this.__isClosed = true;
        this.__hasReachedEOF = false;
        this.__queue = new int[2049];
        this.__queueHead = 0;
        this.__queueTail = 0;
        this.__bytesAvailable = 0;
        this.__ioException = null;
        this.__readIsWaiting = false;
        this.__threaded = false;
        if (readerThread) {
            this.__thread = new Thread(this);
        } else {
            this.__thread = null;
        }
    }

    TelnetInputStream(InputStream input, TelnetClient client) {
        this(input, client, true);
    }

    void _start() {
        if (this.__thread != null) {
            this.__isClosed = false;
            int priority = Thread.currentThread().getPriority() + 1;
            if (priority > 10) {
                priority = 10;
            }
            this.__thread.setPriority(priority);
            this.__thread.setDaemon(true);
            this.__thread.start();
            this.__threaded = true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int __read(boolean r8) throws java.io.IOException {
        /*
        r7 = this;
        r6 = 6;
        r5 = 0;
    L_0x0002:
        if (r8 != 0) goto L_0x000c;
    L_0x0004:
        r1 = super.available();
        if (r1 != 0) goto L_0x000c;
    L_0x000a:
        r0 = -2;
    L_0x000b:
        return r0;
    L_0x000c:
        r0 = super.read();
        if (r0 >= 0) goto L_0x0014;
    L_0x0012:
        r0 = -1;
        goto L_0x000b;
    L_0x0014:
        r0 = r0 & 255;
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x0032 }
        r1._processAYTResponse();	 Catch:{ all -> 0x0032 }
        monitor-exit(r2);	 Catch:{ all -> 0x0032 }
        r1 = r7.__client;
        r1._spyRead(r0);
        r1 = r7.__receiveState;
        switch(r1) {
            case 0: goto L_0x002a;
            case 1: goto L_0x0058;
            case 2: goto L_0x007e;
            case 3: goto L_0x0093;
            case 4: goto L_0x00a8;
            case 5: goto L_0x00bd;
            case 6: goto L_0x00d2;
            case 7: goto L_0x0029;
            case 8: goto L_0x0035;
            case 9: goto L_0x00f0;
            default: goto L_0x0029;
        };
    L_0x0029:
        goto L_0x000b;
    L_0x002a:
        r1 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r0 != r1) goto L_0x0038;
    L_0x002e:
        r1 = 1;
        r7.__receiveState = r1;
        goto L_0x0002;
    L_0x0032:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0032 }
        throw r1;
    L_0x0035:
        if (r0 != 0) goto L_0x002a;
    L_0x0037:
        goto L_0x0002;
    L_0x0038:
        r1 = 13;
        if (r0 != r1) goto L_0x0055;
    L_0x003c:
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x004e }
        r3 = 0;
        r1 = r1._requestedDont(r3);	 Catch:{ all -> 0x004e }
        if (r1 == 0) goto L_0x0051;
    L_0x0048:
        r1 = 8;
        r7.__receiveState = r1;	 Catch:{ all -> 0x004e }
    L_0x004c:
        monitor-exit(r2);	 Catch:{ all -> 0x004e }
        goto L_0x000b;
    L_0x004e:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x004e }
        throw r1;
    L_0x0051:
        r1 = 0;
        r7.__receiveState = r1;	 Catch:{ all -> 0x004e }
        goto L_0x004c;
    L_0x0055:
        r7.__receiveState = r5;
        goto L_0x000b;
    L_0x0058:
        switch(r0) {
            case 240: goto L_0x007b;
            case 241: goto L_0x005b;
            case 242: goto L_0x005b;
            case 243: goto L_0x005b;
            case 244: goto L_0x005b;
            case 245: goto L_0x005b;
            case 246: goto L_0x005b;
            case 247: goto L_0x005b;
            case 248: goto L_0x005b;
            case 249: goto L_0x005b;
            case 250: goto L_0x0073;
            case 251: goto L_0x0063;
            case 252: goto L_0x0067;
            case 253: goto L_0x006b;
            case 254: goto L_0x006f;
            case 255: goto L_0x0078;
            default: goto L_0x005b;
        };
    L_0x005b:
        r7.__receiveState = r5;
        r1 = r7.__client;
        r1._processCommand(r0);
        goto L_0x0002;
    L_0x0063:
        r1 = 2;
        r7.__receiveState = r1;
        goto L_0x0002;
    L_0x0067:
        r1 = 3;
        r7.__receiveState = r1;
        goto L_0x0002;
    L_0x006b:
        r1 = 4;
        r7.__receiveState = r1;
        goto L_0x0002;
    L_0x006f:
        r1 = 5;
        r7.__receiveState = r1;
        goto L_0x0002;
    L_0x0073:
        r7.__suboption_count = r5;
        r7.__receiveState = r6;
        goto L_0x0002;
    L_0x0078:
        r7.__receiveState = r5;
        goto L_0x000b;
    L_0x007b:
        r7.__receiveState = r5;
        goto L_0x0002;
    L_0x007e:
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x0090 }
        r1._processWill(r0);	 Catch:{ all -> 0x0090 }
        r1 = r7.__client;	 Catch:{ all -> 0x0090 }
        r1._flushOutputStream();	 Catch:{ all -> 0x0090 }
        monitor-exit(r2);	 Catch:{ all -> 0x0090 }
        r7.__receiveState = r5;
        goto L_0x0002;
    L_0x0090:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0090 }
        throw r1;
    L_0x0093:
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x00a5 }
        r1._processWont(r0);	 Catch:{ all -> 0x00a5 }
        r1 = r7.__client;	 Catch:{ all -> 0x00a5 }
        r1._flushOutputStream();	 Catch:{ all -> 0x00a5 }
        monitor-exit(r2);	 Catch:{ all -> 0x00a5 }
        r7.__receiveState = r5;
        goto L_0x0002;
    L_0x00a5:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x00a5 }
        throw r1;
    L_0x00a8:
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x00ba }
        r1._processDo(r0);	 Catch:{ all -> 0x00ba }
        r1 = r7.__client;	 Catch:{ all -> 0x00ba }
        r1._flushOutputStream();	 Catch:{ all -> 0x00ba }
        monitor-exit(r2);	 Catch:{ all -> 0x00ba }
        r7.__receiveState = r5;
        goto L_0x0002;
    L_0x00ba:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x00ba }
        throw r1;
    L_0x00bd:
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x00cf }
        r1._processDont(r0);	 Catch:{ all -> 0x00cf }
        r1 = r7.__client;	 Catch:{ all -> 0x00cf }
        r1._flushOutputStream();	 Catch:{ all -> 0x00cf }
        monitor-exit(r2);	 Catch:{ all -> 0x00cf }
        r7.__receiveState = r5;
        goto L_0x0002;
    L_0x00cf:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x00cf }
        throw r1;
    L_0x00d2:
        switch(r0) {
            case 255: goto L_0x00ea;
            default: goto L_0x00d5;
        };
    L_0x00d5:
        r1 = r7.__suboption_count;
        r2 = r7.__suboption;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x00e6;
    L_0x00dc:
        r1 = r7.__suboption;
        r2 = r7.__suboption_count;
        r3 = r2 + 1;
        r7.__suboption_count = r3;
        r1[r2] = r0;
    L_0x00e6:
        r7.__receiveState = r6;
        goto L_0x0002;
    L_0x00ea:
        r1 = 9;
        r7.__receiveState = r1;
        goto L_0x0002;
    L_0x00f0:
        switch(r0) {
            case 240: goto L_0x00f7;
            case 255: goto L_0x0110;
            default: goto L_0x00f3;
        };
    L_0x00f3:
        r7.__receiveState = r6;
        goto L_0x0002;
    L_0x00f7:
        r2 = r7.__client;
        monitor-enter(r2);
        r1 = r7.__client;	 Catch:{ all -> 0x010d }
        r3 = r7.__suboption;	 Catch:{ all -> 0x010d }
        r4 = r7.__suboption_count;	 Catch:{ all -> 0x010d }
        r1._processSuboption(r3, r4);	 Catch:{ all -> 0x010d }
        r1 = r7.__client;	 Catch:{ all -> 0x010d }
        r1._flushOutputStream();	 Catch:{ all -> 0x010d }
        monitor-exit(r2);	 Catch:{ all -> 0x010d }
        r7.__receiveState = r5;
        goto L_0x0002;
    L_0x010d:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x010d }
        throw r1;
    L_0x0110:
        r1 = r7.__suboption_count;
        r2 = r7.__suboption;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x00f3;
    L_0x0117:
        r1 = r7.__suboption;
        r2 = r7.__suboption_count;
        r3 = r2 + 1;
        r7.__suboption_count = r3;
        r1[r2] = r0;
        goto L_0x00f3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.telnet.TelnetInputStream.__read(boolean):int");
    }

    private boolean __processChar(int ch) throws InterruptedException {
        boolean bufferWasEmpty = false;
        synchronized (this.__queue) {
            if (this.__bytesAvailable == 0) {
                bufferWasEmpty = true;
            }
            while (this.__bytesAvailable >= this.__queue.length - 1) {
                if (this.__threaded) {
                    this.__queue.notify();
                    try {
                        this.__queue.wait();
                    } catch (InterruptedException e) {
                        throw e;
                    }
                }
                throw new IllegalStateException("Queue is full! Cannot process another character.");
            }
            if (this.__readIsWaiting && this.__threaded) {
                this.__queue.notify();
            }
            this.__queue[this.__queueTail] = ch;
            this.__bytesAvailable++;
            int i = this.__queueTail + 1;
            this.__queueTail = i;
            if (i >= this.__queue.length) {
                this.__queueTail = 0;
            }
        }
        return bufferWasEmpty;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() throws java.io.IOException {
        /*
        r10 = this;
        r7 = -2;
        r3 = -1;
        r4 = r10.__queue;
        monitor-enter(r4);
    L_0x0005:
        r5 = r10.__ioException;	 Catch:{ all -> 0x000f }
        if (r5 == 0) goto L_0x0012;
    L_0x0009:
        r1 = r10.__ioException;	 Catch:{ all -> 0x000f }
        r3 = 0;
        r10.__ioException = r3;	 Catch:{ all -> 0x000f }
        throw r1;	 Catch:{ all -> 0x000f }
    L_0x000f:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x000f }
        throw r3;
    L_0x0012:
        r5 = r10.__bytesAvailable;	 Catch:{ all -> 0x000f }
        if (r5 != 0) goto L_0x0084;
    L_0x0016:
        r5 = r10.__hasReachedEOF;	 Catch:{ all -> 0x000f }
        if (r5 == 0) goto L_0x001d;
    L_0x001a:
        monitor-exit(r4);	 Catch:{ all -> 0x000f }
        r0 = r3;
    L_0x001c:
        return r0;
    L_0x001d:
        r5 = r10.__threaded;	 Catch:{ all -> 0x000f }
        if (r5 == 0) goto L_0x003c;
    L_0x0021:
        r5 = r10.__queue;	 Catch:{ all -> 0x000f }
        r5.notify();	 Catch:{ all -> 0x000f }
        r5 = 1;
        r10.__readIsWaiting = r5;	 Catch:{ InterruptedException -> 0x0032 }
        r5 = r10.__queue;	 Catch:{ InterruptedException -> 0x0032 }
        r5.wait();	 Catch:{ InterruptedException -> 0x0032 }
        r5 = 0;
        r10.__readIsWaiting = r5;	 Catch:{ InterruptedException -> 0x0032 }
        goto L_0x0005;
    L_0x0032:
        r1 = move-exception;
        r3 = new java.io.InterruptedIOException;	 Catch:{ all -> 0x000f }
        r5 = "Fatal thread interruption during read.";
        r3.<init>(r5);	 Catch:{ all -> 0x000f }
        throw r3;	 Catch:{ all -> 0x000f }
    L_0x003c:
        r5 = 1;
        r10.__readIsWaiting = r5;	 Catch:{ all -> 0x000f }
        r2 = 1;
    L_0x0040:
        r0 = r10.__read(r2);	 Catch:{ InterruptedIOException -> 0x004a }
        if (r0 >= 0) goto L_0x0063;
    L_0x0046:
        if (r0 == r7) goto L_0x0063;
    L_0x0048:
        monitor-exit(r4);	 Catch:{ all -> 0x000f }
        goto L_0x001c;
    L_0x004a:
        r1 = move-exception;
        r5 = r10.__queue;	 Catch:{ all -> 0x000f }
        monitor-enter(r5);	 Catch:{ all -> 0x000f }
        r10.__ioException = r1;	 Catch:{ all -> 0x0060 }
        r6 = r10.__queue;	 Catch:{ all -> 0x0060 }
        r6.notifyAll();	 Catch:{ all -> 0x0060 }
        r6 = r10.__queue;	 Catch:{ InterruptedException -> 0x00ae }
        r8 = 100;
        r6.wait(r8);	 Catch:{ InterruptedException -> 0x00ae }
    L_0x005c:
        monitor-exit(r5);	 Catch:{ all -> 0x0060 }
        monitor-exit(r4);	 Catch:{ all -> 0x000f }
        r0 = r3;
        goto L_0x001c;
    L_0x0060:
        r3 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0060 }
        throw r3;	 Catch:{ all -> 0x000f }
    L_0x0063:
        if (r0 == r7) goto L_0x0068;
    L_0x0065:
        r10.__processChar(r0);	 Catch:{ InterruptedException -> 0x007c }
    L_0x0068:
        r2 = 0;
        r5 = super.available();	 Catch:{ all -> 0x000f }
        if (r5 <= 0) goto L_0x0078;
    L_0x006f:
        r5 = r10.__bytesAvailable;	 Catch:{ all -> 0x000f }
        r6 = r10.__queue;	 Catch:{ all -> 0x000f }
        r6 = r6.length;	 Catch:{ all -> 0x000f }
        r6 = r6 + -1;
        if (r5 < r6) goto L_0x0040;
    L_0x0078:
        r5 = 0;
        r10.__readIsWaiting = r5;	 Catch:{ all -> 0x000f }
        goto L_0x0005;
    L_0x007c:
        r1 = move-exception;
        r5 = r10.__isClosed;	 Catch:{ all -> 0x000f }
        if (r5 == 0) goto L_0x0068;
    L_0x0081:
        monitor-exit(r4);	 Catch:{ all -> 0x000f }
        r0 = r3;
        goto L_0x001c;
    L_0x0084:
        r3 = r10.__queue;	 Catch:{ all -> 0x000f }
        r5 = r10.__queueHead;	 Catch:{ all -> 0x000f }
        r0 = r3[r5];	 Catch:{ all -> 0x000f }
        r3 = r10.__queueHead;	 Catch:{ all -> 0x000f }
        r3 = r3 + 1;
        r10.__queueHead = r3;	 Catch:{ all -> 0x000f }
        r5 = r10.__queue;	 Catch:{ all -> 0x000f }
        r5 = r5.length;	 Catch:{ all -> 0x000f }
        if (r3 < r5) goto L_0x0098;
    L_0x0095:
        r3 = 0;
        r10.__queueHead = r3;	 Catch:{ all -> 0x000f }
    L_0x0098:
        r3 = r10.__bytesAvailable;	 Catch:{ all -> 0x000f }
        r3 = r3 + -1;
        r10.__bytesAvailable = r3;	 Catch:{ all -> 0x000f }
        r3 = r10.__bytesAvailable;	 Catch:{ all -> 0x000f }
        if (r3 != 0) goto L_0x00ab;
    L_0x00a2:
        r3 = r10.__threaded;	 Catch:{ all -> 0x000f }
        if (r3 == 0) goto L_0x00ab;
    L_0x00a6:
        r3 = r10.__queue;	 Catch:{ all -> 0x000f }
        r3.notify();	 Catch:{ all -> 0x000f }
    L_0x00ab:
        monitor-exit(r4);	 Catch:{ all -> 0x000f }
        goto L_0x001c;
    L_0x00ae:
        r6 = move-exception;
        goto L_0x005c;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.net.telnet.TelnetInputStream.read():int");
    }

    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (length < 1) {
            return 0;
        }
        synchronized (this.__queue) {
            if (length > this.__bytesAvailable) {
                length = this.__bytesAvailable;
            }
        }
        int ch = read();
        if (ch == -1) {
            return -1;
        }
        int offset2;
        int off = offset;
        while (true) {
            offset2 = offset + 1;
            buffer[offset] = (byte) ch;
            length--;
            if (length <= 0) {
                break;
            }
            ch = read();
            if (ch == -1) {
                break;
            }
            offset = offset2;
        }
        offset = offset2;
        return offset2 - off;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        int i;
        synchronized (this.__queue) {
            if (this.__threaded) {
                i = this.__bytesAvailable;
            } else {
                i = this.__bytesAvailable + super.available();
            }
        }
        return i;
    }

    public void close() throws IOException {
        super.close();
        synchronized (this.__queue) {
            this.__hasReachedEOF = true;
            this.__isClosed = true;
            if (this.__thread != null && this.__thread.isAlive()) {
                this.__thread.interrupt();
            }
            this.__queue.notifyAll();
        }
    }

    public void run() {
        while (!this.__isClosed) {
            try {
                try {
                    int ch = __read(true);
                    if (ch < 0) {
                        break;
                    }
                    boolean notify = false;
                    try {
                        notify = __processChar(ch);
                    } catch (InterruptedException e) {
                        if (this.__isClosed) {
                            break;
                        }
                    }
                    if (notify) {
                        this.__client.notifyInputListener();
                    } else {
                        continue;
                    }
                } catch (InterruptedIOException e2) {
                    synchronized (this.__queue) {
                        this.__ioException = e2;
                        this.__queue.notifyAll();
                        try {
                            this.__queue.wait(100);
                        } catch (InterruptedException e3) {
                            if (this.__isClosed) {
                                break;
                            }
                        }
                    }
                } catch (RuntimeException e4) {
                    super.close();
                }
            } catch (IOException ioe) {
                synchronized (this.__queue) {
                    this.__ioException = ioe;
                    this.__client.notifyInputListener();
                }
            }
        }
        synchronized (this.__queue) {
            this.__isClosed = true;
            this.__hasReachedEOF = true;
            this.__queue.notify();
        }
        this.__threaded = false;
    }
}
