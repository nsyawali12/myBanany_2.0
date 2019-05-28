package org.apache.commons.io.input;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Tailer implements Runnable {
    private static final int DEFAULT_BUFSIZE = 4096;
    private static final int DEFAULT_DELAY_MILLIS = 1000;
    private static final String RAF_MODE = "r";
    private final long delayMillis;
    private final boolean end;
    private final File file;
    private final byte[] inbuf;
    private final TailerListener listener;
    private final boolean reOpen;
    private volatile boolean run;

    public Tailer(File file, TailerListener listener) {
        this(file, listener, 1000);
    }

    public Tailer(File file, TailerListener listener, long delayMillis) {
        this(file, listener, delayMillis, false);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end) {
        this(file, listener, delayMillis, end, 4096);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen) {
        this(file, listener, delayMillis, end, reOpen, 4096);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end, int bufSize) {
        this(file, listener, delayMillis, end, false, bufSize);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
        this.run = true;
        this.file = file;
        this.delayMillis = delayMillis;
        this.end = end;
        this.inbuf = new byte[bufSize];
        this.listener = listener;
        listener.init(this);
        this.reOpen = reOpen;
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, int bufSize) {
        Tailer tailer = new Tailer(file, listener, delayMillis, end, bufSize);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
        Tailer tailer = new Tailer(file, listener, delayMillis, end, reOpen, bufSize);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end) {
        return create(file, listener, delayMillis, end, 4096);
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen) {
        return create(file, listener, delayMillis, end, reOpen, 4096);
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis) {
        return create(file, listener, delayMillis, false);
    }

    public static Tailer create(File file, TailerListener listener) {
        return create(file, listener, 1000, false);
    }

    public File getFile() {
        return this.file;
    }

    public long getDelay() {
        return this.delayMillis;
    }

    public void run() {
        Closeable randomAccessFile;
        Exception e;
        long last = 0;
        long position = 0;
        RandomAccessFile reader = null;
        while (this.run && reader == null) {
            try {
                try {
                    randomAccessFile = new RandomAccessFile(this.file, RAF_MODE);
                } catch (FileNotFoundException e2) {
                    this.listener.fileNotFound();
                    Object obj = reader;
                }
                if (randomAccessFile == null) {
                    try {
                        Thread.sleep(this.delayMillis);
                        reader = randomAccessFile;
                    } catch (InterruptedException e3) {
                        reader = randomAccessFile;
                    }
                } else {
                    try {
                        position = this.end ? this.file.length() : 0;
                        last = System.currentTimeMillis();
                        randomAccessFile.seek(position);
                        reader = randomAccessFile;
                    } catch (Exception e4) {
                        e = e4;
                    }
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = reader;
            } catch (Throwable th) {
                Throwable th2 = th;
                randomAccessFile = reader;
            }
        }
        while (this.run) {
            boolean newer = FileUtils.isFileNewer(this.file, last);
            long length = this.file.length();
            if (length < position) {
                this.listener.fileRotated();
                Closeable save = reader;
                try {
                    randomAccessFile = new RandomAccessFile(this.file, RAF_MODE);
                    position = 0;
                    try {
                        IOUtils.closeQuietly(save);
                        reader = randomAccessFile;
                    } catch (FileNotFoundException e6) {
                        this.listener.fileNotFound();
                        reader = randomAccessFile;
                    }
                } catch (FileNotFoundException e7) {
                    randomAccessFile = reader;
                    this.listener.fileNotFound();
                    reader = randomAccessFile;
                }
            } else {
                RandomAccessFile reader2;
                if (length > position) {
                    position = readLines(reader);
                    last = System.currentTimeMillis();
                } else if (newer) {
                    reader.seek(0);
                    position = readLines(reader);
                    last = System.currentTimeMillis();
                }
                if (this.reOpen) {
                    IOUtils.closeQuietly((Closeable) reader);
                }
                try {
                    Thread.sleep(this.delayMillis);
                } catch (InterruptedException e8) {
                }
                if (this.run && this.reOpen) {
                    randomAccessFile = new RandomAccessFile(this.file, RAF_MODE);
                    randomAccessFile.seek(position);
                } else {
                    reader2 = reader;
                }
                reader = reader2;
            }
        }
        IOUtils.closeQuietly((Closeable) reader);
        randomAccessFile = reader;
        return;
        try {
            this.listener.handle(e);
            IOUtils.closeQuietly(randomAccessFile);
        } catch (Throwable th3) {
            th2 = th3;
            IOUtils.closeQuietly(randomAccessFile);
            throw th2;
        }
    }

    public void stop() {
        this.run = false;
    }

    private long readLines(RandomAccessFile reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        long pos = reader.getFilePointer();
        long rePos = pos;
        boolean seenCR = false;
        while (this.run) {
            int num = reader.read(this.inbuf);
            if (num != -1) {
                for (int i = 0; i < num; i++) {
                    byte ch = this.inbuf[i];
                    switch (ch) {
                        case (byte) 10:
                            seenCR = false;
                            this.listener.handle(sb.toString());
                            sb.setLength(0);
                            rePos = (((long) i) + pos) + 1;
                            break;
                        case (byte) 13:
                            if (seenCR) {
                                sb.append('\r');
                            }
                            seenCR = true;
                            break;
                        default:
                            if (seenCR) {
                                seenCR = false;
                                this.listener.handle(sb.toString());
                                sb.setLength(0);
                                rePos = (((long) i) + pos) + 1;
                            }
                            sb.append((char) ch);
                            break;
                    }
                }
                pos = reader.getFilePointer();
            } else {
                reader.seek(rePos);
                return rePos;
            }
        }
        reader.seek(rePos);
        return rePos;
    }
}
