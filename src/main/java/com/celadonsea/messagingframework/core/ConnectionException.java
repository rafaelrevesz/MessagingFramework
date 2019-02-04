package com.celadonsea.messagingframework.core;

/**
 * Common runtime exception for the messaging framework.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public class ConnectionException extends RuntimeException {

    /**
     * Constructs a new connection exception if an expected exception was thrown.
     * @param e the cause
     */
    public ConnectionException(Exception e) {
        super(e);
    }

    /**
     * Constructs a new connection exception with the specified detail message.
     *
     * @param message the detailed message
     */
    public ConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new connection exception with the specified detail message
     * and cause.
     *
     * @param message the detailed message
     * @param e the cause
     */
    public ConnectionException(String message, Exception e) {
        super(message, e);
    }
}
