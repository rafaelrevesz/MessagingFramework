package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.client.MessageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class CallBack implements MqttCallback {

    private final MessageClient messageClient;

    private Map<String, BiConsumer<String, byte[]>> subscriptionMap = new HashMap<>();

    public void subscribe(String topic, BiConsumer<String, byte[]> consumer) {
        subscriptionMap.put(topic, consumer);
    }

    public void messageArrived(String topic, byte[] message) {
        Iterator<Map.Entry<String, BiConsumer<String, byte[]>>> iterator = subscriptionMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, BiConsumer<String, byte[]>> subscription = iterator.next();
            if (topic.matches(subscription.getKey()
                .replaceAll("/", "\\/")
                .replaceAll("\\.", "\\.")
                .replaceAll("\\*", "(.*)")
                .replaceAll("\\+", "(.*)"))) {
                subscription.getValue().accept(topic, message);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("Connection lost", cause);
        messageClient.reconnect(this);
        subscriptionMap.forEach(messageClient::subscribe);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        messageArrived(topic, message.getPayload());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("Message delivered: " + token.toString());
    }
}
