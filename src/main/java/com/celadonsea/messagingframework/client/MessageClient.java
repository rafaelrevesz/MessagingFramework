package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.core.ConsumingProperties;
import com.celadonsea.messagingframework.core.ProducingProperties;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.topic.TopicFormat;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Interface for message clients.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public interface MessageClient {

    /**
     * Performs the connection to the configured message broker
     */
    void connect();

    /**
     * Restores the lost or broken connection to the configured message broker
     *
     * @param callBack the message call back instance
     */
    void reconnect(CallBack callBack);

    /**
     * Publishes a message payload with the given properties
     *
     * @param message message payload
     * @param producingProperties properties for the publishing
     */
    void publish(byte[] message, ProducingProperties producingProperties);

    /**
     * Sets the consuming of messages with the given properties handling with a lambda function as callback
     *
     * @param consumingProperties the consuming properties
     * @param messageConsumer the message consumer function
     */
    void subscribe(ConsumingProperties consumingProperties, BiConsumer<String, byte[]> messageConsumer);

    /**
     * Returns the protocol specific topic format description.
     *
     * @return topic format description
     */
    TopicFormat getTopicFormat();

    /**
     * Sets the protocol specific topic format description.
     *
     * @param topicFormat the new topic format
     */
    void setTopicFormat(TopicFormat topicFormat);

    /**
     * Returns a message publisher for the current client.
     *
     * @return message publisher
     */
    MessagePublisher publisher();

    /**
     * Provides a function to process client specific topic transformation if needed.
     *
     * If it's not set (is null) then the transformation process will be skipped.
     *
     * @return client specific transformation function for topics
     */
    Function<String, String> topicTransformer();
}
