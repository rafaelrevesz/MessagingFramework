package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.topic.TopicFormat;

import java.util.function.BiConsumer;

public interface MessageClient {

    void connect();

    void reconnect(CallBack callBack);

    void publish(String topic, byte[] message);

    void publish(String topic, byte[] message, int qos);

    void subscribe(String topic, BiConsumer<String, byte[]> messageConsumer);

    TopicFormat topicFormat();

    MessagePublisher publisher();
}
