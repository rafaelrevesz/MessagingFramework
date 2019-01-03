package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.topic.TopicFormat;

import java.util.function.BiConsumer;

public class TestMessageClient implements MessageClient {

    private CallBack callBack;

    @Override
    public void connect() {

    }

    @Override
    public void reconnect(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void publish(String topic, String message) {

    }

    @Override
    public void subscribe(String topic, BiConsumer<String, byte[]> messageConsumer) {
        callBack.subscribe(topic, messageConsumer);
    }

    @Override
    public TopicFormat topicFormat() {
        return null;
    }
}
