package com.celadonsea.messagingframework.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common transfer properties for producing or consuming messages.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public abstract class TransferProperties {

    /**
     * Topic or routing key.
     * -- GETTER --
     * Returns the topic or routing key
     *
     * @return the topic or routing key
     */
    private final String topic;

    /**
     * The exchange
     * -- GETTER --
     * Returns the exchange
     *
     * @return the exchange value
     */
    private String exchange;
}
