package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.core.ProducingProperties;
import com.celadonsea.messagingframework.topic.TopicParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds up and publishes a message to a simple or parametrized topic.
 * The {@link MessageClient} implementations can instantiate it.
 *
 * The parametrized topics will be processed with the {@link TopicParser#generate(String, Map)}
 * function.
 *
 * Parametrized topic contains variables with the format {variableName}. Eg.:
 *
 *   basetopic/subtopic/{variable1}/otherTopicPart/{variable2}
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MessagePublisher {

    /**
     * The message client which is used to publish a message
     */
    private final MessageClient messageClient;

    /**
     * Variable map (name, value) for the parametrized topics
     */
    private final Map<String, String> variables = new HashMap<>();

    /**
     * Quality of service, default value is 0
     */
    private int qos = 0;

    /**
     * The message payload
     */
    private byte[] message;

    /**
     * The parametrized or simple topic
     */
    private String topic;

    /**
     * Adds a new variable name-value pair to the map.
     *
     * @param variable variable name
     * @param value variable value
     * @return the publisher instance
     */
    public MessagePublisher variable(String variable, String value) {
        variables.put(variable, value);
        return this;
    }

    /**
     * Sets a new quality of service value.
     *
     * @param qos quality of service
     * @return the publisher instance
     */
    public MessagePublisher qos(int qos) {
        this.qos = qos;
        return this;
    }

    /**
     * Sets the message payload.
     *
     * @param message message payload
     * @return the publisher instance
     */
    public MessagePublisher message(byte[] message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the topic.
     *
     * @param topic the parametrized or simple topic
     * @return the publisher instance
     */
    public MessagePublisher topic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Resolves the topic from the parametrized topic if necessary
     * and publishes the message payload to it.
     *
     * @throws IllegalArgumentException if topic or message is null
     */
    public void publish() {
        Assert.notNull(topic, "Topic must be set");
        Assert.notNull(message, "Message must be set");
        String resolvedTopic = TopicParser.generate(topic, variables);
        this.messageClient.publish(message, new ProducingProperties(resolvedTopic, qos));
    }
}
