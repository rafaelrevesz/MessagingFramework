package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.topic.TopicParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MessagePublisher {

    private final MessageClient messageClient;

    private final Map<String, String> variables = new HashMap<>();

    private int qos = 0;

    private byte[] message;

    private String topic;

    public MessagePublisher variable(String variable, String value) {
        variables.put(variable, value);
        return this;
    }

    public MessagePublisher qos(int qos) {
        this.qos = qos;
        return this;
    }

    public MessagePublisher message(byte[] message) {
        this.message = message;
        return this;
    }

    public MessagePublisher topic(String topic) {
        this.topic = topic;
        return this;
    }

    public void publish() {
        String resolvedTopic = TopicParser.generate(topic, variables);
        this.messageClient.publish(resolvedTopic, message, qos);
    }
}
