package com.celadonsea.messagingframework.core;

public class ConnectionException extends RuntimeException {

    public ConnectionException(Exception e) {
        super(e);
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Exception e) {
        super(message, e);
    }
}
