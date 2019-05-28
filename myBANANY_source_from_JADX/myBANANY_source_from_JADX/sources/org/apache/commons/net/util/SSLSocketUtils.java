package org.apache.commons.net.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.net.ssl.SSLSocket;

public class SSLSocketUtils {
    private SSLSocketUtils() {
    }

    public static boolean enableEndpointNameVerification(SSLSocket socket) {
        try {
            Method setEndpointIdentificationAlgorithm = Class.forName("javax.net.ssl.SSLParameters").getDeclaredMethod("setEndpointIdentificationAlgorithm", new Class[]{String.class});
            Method getSSLParameters = SSLSocket.class.getDeclaredMethod("getSSLParameters", new Class[0]);
            Method setSSLParameters = SSLSocket.class.getDeclaredMethod("setSSLParameters", new Class[]{cls});
            if (!(setEndpointIdentificationAlgorithm == null || getSSLParameters == null || setSSLParameters == null)) {
                Object sslParams = getSSLParameters.invoke(socket, new Object[0]);
                if (sslParams != null) {
                    setEndpointIdentificationAlgorithm.invoke(sslParams, new Object[]{"HTTPS"});
                    setSSLParameters.invoke(socket, new Object[]{sslParams});
                    return true;
                }
            }
        } catch (SecurityException e) {
        } catch (ClassNotFoundException e2) {
        } catch (NoSuchMethodException e3) {
        } catch (IllegalArgumentException e4) {
        } catch (IllegalAccessException e5) {
        } catch (InvocationTargetException e6) {
        }
        return false;
    }
}
