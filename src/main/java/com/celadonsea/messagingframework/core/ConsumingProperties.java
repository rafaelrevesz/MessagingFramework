package com.celadonsea.messagingframework.core;

/**
 * Properties for consuming messages
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public class ConsumingProperties extends TransferProperties {

    /**
     * Constructs the properties with the routing key (topic) and exchange.
     * It's commonly used for AMQP subscription.
     *
     * @param topic the routing key
     * @param exchange the exchange
     */
    public ConsumingProperties(String topic, String exchange) {
        super(topic, exchange);
    }

    /**
     * Constructs the properties with the topic.
     * It's commonly used for MQTT subscription.
     *
     * @param topic the topic
     */
    public ConsumingProperties(String topic) {
        super(topic, null);
    }
}
