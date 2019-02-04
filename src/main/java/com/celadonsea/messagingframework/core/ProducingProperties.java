package com.celadonsea.messagingframework.core;

import lombok.Getter;

/**
 * Properties for producing messages.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public class ProducingProperties extends TransferProperties {

    /**
     * The default quality of service, if the qos is not set.
     */
    public static final int DEFAULT_UNSET_QOS = -1;

    /**
     * Quality of service, the default is for marking the unset property
     * -- GETTER --
     * Returns the quality of service
     *
     * @return the quality of service
     */
    @Getter
    private int qos = DEFAULT_UNSET_QOS;

    /**
     * Constructs the properties with routing key (topic) and exchange.
     * It's commonly used for AMQP publishing.
     *
     * @param topic routing key (topic)
     * @param exchange exchange
     */
    public ProducingProperties(String topic, String exchange) {
        super(topic, exchange);
    }

    /**
     * Constructs the properties with topic.
     * It's commonly used for MQTT publishing.
     *
     * @param topic topic
     */
    public ProducingProperties(String topic) {
        super(topic, null);
    }

    /**
     * Constructs the properties with topic and quality of service
     * @param topic topic
     * @param qos quality of service
     */
    public ProducingProperties(String topic, int qos) {
        super(topic, null);
        this.qos = qos;
    }
}
