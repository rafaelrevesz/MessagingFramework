package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.topic.TopicFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class TestMessageClient implements MessageClient {

    public static final int DEFAULT_QOS = 0;

    @Getter
    private CallBack callBack;

    private MessageClientConfig messageClientConfig;

    public TestMessageClient(MessageClientConfig messageClientConfig) {
        this.messageClientConfig = messageClientConfig;
    }

    @Getter
    private Map<String, List<byte[]>> publishedMessages = new HashMap<>();

    @Override
    public void connect() {
        log.info("Connectig to {}", messageClientConfig.getBrokerUrl());
        callBack = new CallBack(this);
    }

    @Override
    public void reconnect(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void publish(String topic, byte[] message) {
        publish(topic, message, DEFAULT_QOS);
    }

    @Override
    public void publish(String topic, byte[] message, int qos) {
        String key = getMessageKey(topic, qos);
        if (!publishedMessages.containsKey(key)) {
            publishedMessages.put(key, new ArrayList<>());
        }
        publishedMessages.get(key).add(message);
    }

    public String getMessageKey(String topic, int qos) {
        return topic + "___" + qos;
    }

    @Override
    public void subscribe(String topic, BiConsumer<String, byte[]> messageConsumer) {
        callBack.subscribe(topic, messageConsumer);
        log.info("Subscribed to {}", topic);
    }

    @Override
    public TopicFormat topicFormat() {
        return new TopicFormat('/', '+', '#');
    }

    @Override
    public MessagePublisher publisher() {
        return new MessagePublisher(this);
    }
}
