package org.apache.commons.net.smtp;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import org.apache.commons.net.util.Base64;

public class AuthenticatingSMTPClient extends SMTPSClient {

    public enum AUTH_METHOD {
        PLAIN,
        CRAM_MD5,
        LOGIN,
        XOAUTH;

        public static final String getAuthName(AUTH_METHOD method) {
            if (method.equals(PLAIN)) {
                return "PLAIN";
            }
            if (method.equals(CRAM_MD5)) {
                return "CRAM-MD5";
            }
            if (method.equals(LOGIN)) {
                return "LOGIN";
            }
            if (method.equals(XOAUTH)) {
                return "XOAUTH";
            }
            return null;
        }
    }

    public AuthenticatingSMTPClient(String protocol) {
        super(protocol);
    }

    public AuthenticatingSMTPClient(String proto, boolean implicit) {
        super(proto, implicit);
    }

    public AuthenticatingSMTPClient(String proto, boolean implicit, String encoding) {
        super(proto, implicit, encoding);
    }

    public AuthenticatingSMTPClient(boolean implicit, SSLContext ctx) {
        super(implicit, ctx);
    }

    public AuthenticatingSMTPClient(String protocol, String encoding) {
        super(protocol, false, encoding);
    }

    public int ehlo(String hostname) throws IOException {
        return sendCommand(15, hostname);
    }

    public boolean elogin(String hostname) throws IOException {
        return SMTPReply.isPositiveCompletion(ehlo(hostname));
    }

    public boolean elogin() throws IOException {
        String name = getLocalAddress().getHostName();
        if (name == null) {
            return false;
        }
        return SMTPReply.isPositiveCompletion(ehlo(name));
    }

    public int[] getEnhancedReplyCode() {
        String reply = getReplyString().substring(4);
        String[] parts = reply.substring(0, reply.indexOf(32)).split("\\.");
        int[] res = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            res[i] = Integer.parseInt(parts[i]);
        }
        return res;
    }

    public boolean auth(AUTH_METHOD method, String username, String password) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        if (!SMTPReply.isPositiveIntermediate(sendCommand(14, AUTH_METHOD.getAuthName(method)))) {
            return false;
        }
        if (method.equals(AUTH_METHOD.PLAIN)) {
            return SMTPReply.isPositiveCompletion(sendCommand(Base64.encodeBase64StringUnChunked(("\u0000" + username + "\u0000" + password).getBytes(getCharsetName()))));
        }
        if (method.equals(AUTH_METHOD.CRAM_MD5)) {
            byte[] serverChallenge = Base64.decodeBase64(getReplyString().substring(4).trim());
            Mac hmac_md5 = Mac.getInstance("HmacMD5");
            hmac_md5.init(new SecretKeySpec(password.getBytes(getCharsetName()), "HmacMD5"));
            byte[] hmacResult = _convertToHexString(hmac_md5.doFinal(serverChallenge)).getBytes(getCharsetName());
            byte[] usernameBytes = username.getBytes(getCharsetName());
            byte[] toEncode = new byte[((usernameBytes.length + 1) + hmacResult.length)];
            System.arraycopy(usernameBytes, 0, toEncode, 0, usernameBytes.length);
            toEncode[usernameBytes.length] = (byte) 32;
            System.arraycopy(hmacResult, 0, toEncode, usernameBytes.length + 1, hmacResult.length);
            return SMTPReply.isPositiveCompletion(sendCommand(Base64.encodeBase64StringUnChunked(toEncode)));
        } else if (method.equals(AUTH_METHOD.LOGIN)) {
            if (SMTPReply.isPositiveIntermediate(sendCommand(Base64.encodeBase64StringUnChunked(username.getBytes(getCharsetName()))))) {
                return SMTPReply.isPositiveCompletion(sendCommand(Base64.encodeBase64StringUnChunked(password.getBytes(getCharsetName()))));
            }
            return false;
        } else if (method.equals(AUTH_METHOD.XOAUTH)) {
            return SMTPReply.isPositiveIntermediate(sendCommand(Base64.encodeBase64StringUnChunked(username.getBytes(getCharsetName()))));
        } else {
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
