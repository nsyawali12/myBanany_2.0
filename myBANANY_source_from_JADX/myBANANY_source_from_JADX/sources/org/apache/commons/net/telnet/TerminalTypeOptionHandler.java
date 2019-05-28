package org.apache.commons.net.telnet;

public class TerminalTypeOptionHandler extends TelnetOptionHandler {
    protected static final int TERMINAL_TYPE = 24;
    protected static final int TERMINAL_TYPE_IS = 0;
    protected static final int TERMINAL_TYPE_SEND = 1;
    private final String termType;

    public TerminalTypeOptionHandler(String termtype, boolean initlocal, boolean initremote, boolean acceptlocal, boolean acceptremote) {
        super(24, initlocal, initremote, acceptlocal, acceptremote);
        this.termType = termtype;
    }

    public TerminalTypeOptionHandler(String termtype) {
        super(24, false, false, false, false);
        this.termType = termtype;
    }

    public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
        if (suboptionData == null || suboptionLength <= 1 || this.termType == null || suboptionData[0] != 24 || suboptionData[1] != 1) {
            return null;
        }
        int[] iArr = new int[(this.termType.length() + 2)];
        iArr[0] = 24;
        iArr[1] = 0;
        for (int ii = 0; ii < this.termType.length(); ii++) {
            iArr[ii + 2] = this.termType.charAt(ii);
        }
        return iArr;
    }
}
