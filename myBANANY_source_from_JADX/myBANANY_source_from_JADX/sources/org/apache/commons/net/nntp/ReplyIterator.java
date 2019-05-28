package org.apache.commons.net.nntp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.net.io.DotTerminatedMessageReader;
import org.apache.commons.net.io.Util;

class ReplyIterator implements Iterator<String>, Iterable<String> {
    private String line;
    private final BufferedReader reader;
    private Exception savedException;

    ReplyIterator(BufferedReader _reader, boolean addDotReader) throws IOException {
        if (addDotReader) {
            _reader = new DotTerminatedMessageReader(_reader);
        }
        this.reader = _reader;
        this.line = this.reader.readLine();
        if (this.line == null) {
            Util.closeQuietly(this.reader);
        }
    }

    ReplyIterator(BufferedReader _reader) throws IOException {
        this(_reader, true);
    }

    public boolean hasNext() {
        if (this.savedException == null) {
            return this.line != null;
        } else {
            throw new NoSuchElementException(this.savedException.toString());
        }
    }

    public String next() throws NoSuchElementException {
        if (this.savedException != null) {
            throw new NoSuchElementException(this.savedException.toString());
        }
        String prev = this.line;
        if (prev == null) {
            throw new NoSuchElementException();
        }
        try {
            this.line = this.reader.readLine();
            if (this.line == null) {
                Util.closeQuietly(this.reader);
            }
        } catch (IOException ex) {
            this.savedException = ex;
            Util.closeQuietly(this.reader);
        }
        return prev;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<String> iterator() {
        return this;
    }
}
