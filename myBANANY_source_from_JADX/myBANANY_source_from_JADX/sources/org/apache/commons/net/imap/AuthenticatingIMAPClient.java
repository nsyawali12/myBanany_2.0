package org.apache.commons.net.imap;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import org.apache.commons.net.imap.IMAP.IMAPState;
import org.apache.commons.net.util.Base64;

public class AuthenticatingIMAPClient extends IMAPSClient {

    public enum AUTH_METHOD {
        PLAIN("PLAIN"),
        CRAM_MD5("CRAM-MD5"),
        LOGIN("LOGIN"),
        XOAUTH("XOAUTH");
        
        private final String authName;

        private AUTH_METHOD(String name) {
            this.authName = name;
        }

        public final String getAuthName() {
            return this.authName;
        }
    }

    public AuthenticatingIMAPClient() {
        this(IMAPSClient.DEFAULT_PROTOCOL, false);
    }

    public AuthenticatingIMAPClient(boolean implicit) {
        this(IMAPSClient.DEFAULT_PROTOCOL, implicit);
    }

    public AuthenticatingIMAPClient(String proto) {
        this(proto, false);
    }

    public AuthenticatingIMAPClient(String proto, boolean implicit) {
        this(proto, implicit, null);
    }

    public AuthenticatingIMAPClient(String proto, boolean implicit, SSLContext ctx) {
        super(proto, implicit, ctx);
    }

    public AuthenticatingIMAPClient(boolean implicit, SSLContext ctx) {
        this(IMAPSClient.DEFAULT_PROTOCOL, implicit, ctx);
    }

    public AuthenticatingIMAPClient(SSLContext context) {
        this(false, context);
    }

    public boolean authenticate(AUTH_METHOD method, String username, String password) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        return auth(method, username, password);
    }

    public boolean auth(AUTH_METHOD method, String username, String password) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        if (!IMAPReply.isContinuation(sendCommand(IMAPCommand.AUTHENTICATE, method.getAuthName()))) {
            return false;
        }
        int result;
        switch (method) {
            case PLAIN:
                result = sendData(Base64.encodeBase64StringUnChunked(("\u0000" + username + "\u0000" + password).getBytes(getCharsetName())));
                if (result == 0) {
                    setState(IMAPState.AUTH_STATE);
                }
                if (result != 0) {
                    return false;
                }
                return true;
            case CRAM_MD5:
                byte[] serverChallenge = Base64.decodeBase64(getReplyString().substring(2).trim());
                Mac hmac_md5 = Mac.getInstance("HmacMD5");
                hmac_md5.init(new SecretKeySpec(password.getBytes(getCharsetName()), "HmacMD5"));
                byte[] hmacResult = _convertToHexString(hmac_md5.doFinal(serverChallenge)).getBytes(getCharsetName());
                byte[] usernameBytes = username.getBytes(getCharsetName());
                byte[] toEncode = new byte[((usernameBytes.length + 1) + hmacResult.length)];
                System.arraycopy(usernameBytes, 0, toEncode, 0, usernameBytes.length);
                toEncode[usernameBytes.length] = (byte) 32;
                System.arraycopy(hmacResult, 0, toEncode, usernameBytes.length + 1, hmacResult.length);
                result = sendData(Base64.encodeBase64StringUnChunked(toEncode));
                if (result == 0) {
                    setState(IMAPState.AUTH_STATE);
                }
                if (result != 0) {
                    return false;
                }
                return true;
            case LOGIN:
                if (sendData(Base64.encodeBase64StringUnChunked(username.getBytes(getCharsetName()))) != 3) {
                    return false;
                }
                result = sendData(Base64.encodeBase64StringUnChunked(password.getBytes(getCharsetName())));
                if (result == 0) {
                    setState(IMAPState.AUTH_STATE);
                }
                if (result != 0) {
                    return false;
                }
                return true;
            case XOAUTH:
                result = sendData(username);
                if (result == 0) {
                    setState(IMAPState.AUTH_STATE);
                }
                if (result != 0) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    private String _convertToHexString(byte[] a) {
        StringBuilder result = new StringBuilder(a.length * 2);
        for (byte element : a) {
            if ((element & 255) <= 15) {
                result.append("0");
            }
            result.append(Integer.toHexString(element & 255));
        }
        return result.toString();
    }
}
