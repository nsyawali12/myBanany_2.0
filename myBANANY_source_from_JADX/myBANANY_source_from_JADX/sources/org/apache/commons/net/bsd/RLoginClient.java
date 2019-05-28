package org.apache.commons.net.bsd;

import java.io.IOException;

public class RLoginClient extends RCommandClient {
    public static final int DEFAULT_PORT = 513;

    public RLoginClient() {
        setDefaultPort(513);
    }

    public void rlogin(String localUsername, String remoteUsername, String terminalType, int terminalSpeed) throws IOException {
        rexec(localUsername, remoteUsername, terminalType + "/" + terminalSpeed, false);
    }

    public void rlogin(String localUsername, String remoteUsername, String terminalType) throws IOException {
        rexec(localUsername, remoteUsername, terminalType, false);
    }
}
