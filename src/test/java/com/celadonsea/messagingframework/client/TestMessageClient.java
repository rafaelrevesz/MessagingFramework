package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.core.ConsumingProperties;
import com.celadonsea.messagingframework.core.ProducingProperties;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.topic.TopicFormat;
import com.celadonsea.messagingframework.topic.TopicTransformer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public class TestMessageClient implements MessageClient {

    public static final int DEFAULT_QOS = 0;

    @Getter
    private CallBack callBack;

    private MessageClientConfig messageClientConfig;

    private TopicFormat topicFormat = new TopicFormat('/', '+', '#');

    public TestMessageClient(MessageClientConfig messageClientConfig) {
        this.messageClientConfig = messageClientConfig;
    }

    @Getter
    private Map<String, List<byte[]>> publishedMessages = new HashMap<>();

    @Override
    public void connect() {
        log.info("Connectig to {}", messageClientConfig.getBrokerUrl());
        callBack = new CallBack(this, messageClientConfig);
    }

    @Override
    public void reconnect(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void publish(byte[] message, ProducingProperties producingProperties) {
        int qos = producingProperties.getQos() == ProducingProperties.DEFAULT_UNSET_QOS ? DEFAULT_QOS : producingProperties.getQos();

        String key = getMessageKey(producingProperties.getTopic(), qos);
        if (!publishedMessages.containsKey(key)) {
            publishedMessages.put(key, new ArrayList<>());
        }
        publishedMessages.get(key).add(message);
    }

    @Override
    public void subscribe(ConsumingProperties consumingProperties, BiConsumer<String, byte[]> messageConsumer) {
        callBack.subscribe(consumingProperties.getTopic(), topicTransformer().apply(consumingProperties.getTopic()), messageConsumer);
        log.info("Subscribed to exchange {} and topic {}", consumingProperties.getExchange(), consumingProperties.getTopic());
    }

    @Override
    public void setTopicFormat(TopicFormat topicFormat) {
        this.topicFormat = topicFormat;
    }

    public String getMessageKey(String topic, int qos) {
        return topic + "___" + qos;
    }

    @Override
    public TopicFormat getTopicFormat() {
        return topicFormat;
    }

    @Override
    public MessagePublisher publisher() {
        return new MessagePublisher(this);
    }

    @Override
    public Function<String, String> topicTransformer() {
        return originalTopic -> TopicTransformer.transform(originalTopic).ifShared().andReturn();
    }
}
