package org.apache.commons.net.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class SSLContextUtils {
    private SSLContextUtils() {
    }

    public static SSLContext createSSLContext(String protocol, KeyManager keyManager, TrustManager trustManager) throws IOException {
        TrustManager[] trustManagerArr = null;
        KeyManager[] keyManagerArr = keyManager == null ? null : new KeyManager[]{keyManager};
        if (trustManager != null) {
            trustManagerArr = new TrustManager[]{trustManager};
        }
        return createSSLContext(protocol, keyManagerArr, trustManagerArr);
    }

    public static SSLContext createSSLContext(String protocol, KeyManager[] keyManagers, TrustManager[] trustManagers) throws IOException {
        try {
            SSLContext ctx = SSLContext.getInstance(protocol);
            ctx.init(keyManagers, trustManagers, null);
            return ctx;
        } catch (GeneralSecurityException e) {
            IOException ioe = new IOException("Could not initialize SSL context");
            ioe.initCause(e);
            throw ioe;
        }
    }
}
