package org.apache.commons.net.telnet;

public class InvalidTelnetOptionException extends Exception {
    private static final long serialVersionUID = -2516777155928793597L;
    private final String msg;
    private final int optionCode;

    public InvalidTelnetOptionException(String message, int optcode) {
        this.optionCode = optcode;
        this.msg = message;
    }

    public String getMessage() {
        return this.msg + ": " + this.optionCode;
    }
}
