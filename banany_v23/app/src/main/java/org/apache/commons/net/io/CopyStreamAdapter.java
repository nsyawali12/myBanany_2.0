package org.apache.commons.net.io;

import java.util.EventListener;
import java.util.Iterator;
import org.apache.commons.net.util.ListenerList;

public class CopyStreamAdapter implements CopyStreamListener {
    private final ListenerList internalListeners = new ListenerList();

    public void bytesTransferred(CopyStreamEvent event) {
        Iterator i$ = this.internalListeners.iterator();
        while (i$.hasNext()) {
            ((CopyStreamListener) ((EventListener) i$.next())).bytesTransferred(event);
        }
    }

    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        Iterator i$ = this.internalListeners.iterator();
        while (i$.hasNext()) {
            ((CopyStreamListener) ((EventListener) i$.next())).bytesTransferred(totalBytesTransferred, bytesTransferred, streamSize);
        }
    }

    public void addCopyStreamListener(CopyStreamListener listener) {
        this.internalListeners.addListener(listener);
    }

    public void removeCopyStreamListener(CopyStreamListener listener) {
        this.internalListeners.removeListener(listener);
    }
}
