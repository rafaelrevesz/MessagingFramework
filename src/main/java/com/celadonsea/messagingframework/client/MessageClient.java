package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.topic.TopicFormat;

import java.util.function.BiConsumer;

public interface MessageClient {

    void connect();

    void reconnect(CallBack callBack);

    void publish(String topic, String message);

    void subscribe(String topic, BiConsumer<String, byte[]> messageConsumer);

    TopicFormat topicFormat();

}
