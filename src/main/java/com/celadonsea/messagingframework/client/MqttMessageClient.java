package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.core.ConnectionException;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.security.CertificateLoader;
import com.celadonsea.messagingframework.security.CredentialStore;
import com.celadonsea.messagingframework.security.MessageTlsSocketFactory;
import com.celadonsea.messagingframework.topic.TopicFormat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MqttMessageClient implements MessageClient {

    private final MessageClientConfig messageClientConfig;

    private TopicFormat topicFormat = new TopicFormat('/', '+', '#');

    private IMqttClient mqttClient;

    private CallBack callBack;

    private CredentialStore credentialStore;

    MqttMessageClient(MessageClientConfig messageClientConfig, CredentialStore credentialStore) {
        this.messageClientConfig = messageClientConfig;
        this.credentialStore = credentialStore;
    }

    @Override
    public void connect() {
        callBack = new CallBack(this);
    }

    public void reconnect(CallBack callBack) {
        try {
            mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(
                messageClientConfig.getBrokerUrl(),
                messageClientConfig.getClientId(),
                new MemoryPersistence());

            log.info("Connecting to broker (URL: {})", messageClientConfig.getBrokerUrl());
            mqttClient.connect(getMqttConnectOptions());
            mqttClient.setCallback(callBack);
        } catch (MqttException e) {
            log.error("Cannot connect to message broker {}", messageClientConfig.getBrokerUrl(), e);
        }
    }

    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setMaxInflight(messageClientConfig.getMaxInFlight());
        connectOptions.setConnectionTimeout(messageClientConfig.getConnectionTimeout());
        connectOptions.setKeepAliveInterval(messageClientConfig.getKeepAliveInterval());

        if (messageClientConfig.isConnectionSecured()) {
            CertificateLoader.KeyStorePasswordPair pair = CertificateLoader.getKeyStorePasswordPair(
                credentialStore.getCertificate(),
                credentialStore.getPrivateKey());
            connectOptions.setSocketFactory(new MessageTlsSocketFactory(pair.keyStore, pair.keyPassword));
        }

        return connectOptions;
    }

    @Override
    public void publish(String topic, String message) {

    }

    @Override
    public void subscribe(String topic, BiConsumer<String, byte[]> messageConsumer) {

    }

    @Override
    public TopicFormat topicFormat() {
        return topicFormat;
    }
}
