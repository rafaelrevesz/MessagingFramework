package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.core.ConsumingProperties;
import com.celadonsea.messagingframework.core.ProducingProperties;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.security.CertificateLoader;
import com.celadonsea.messagingframework.security.CredentialStore;
import com.celadonsea.messagingframework.security.MessageTlsSocketFactory;
import com.celadonsea.messagingframework.topic.TopicFormat;
import com.celadonsea.messagingframework.topic.TopicTransformer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.util.Assert;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Message client implementation for MQTT protocol based on eclipse paho library
 * (https://www.eclipse.org/paho/).
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MqttMessageClient implements MessageClient {

    /**
     * The client configuration
     */
    private final MessageClientConfig messageClientConfig;

    /**
     * The topic format which is valid for MQTT protocol
     * -- SETTER --
     * Sets the MQTT specific topic format.
     * -- GETTER --
     * Returns the MQTT specific topic format.
     *
     * @param the new topic format
     * @return topic format
     */
    @Getter
    @Setter
    private TopicFormat topicFormat = new TopicFormat('/', '+', '#');

    /**
     * The paho MQTT client
     */
    private IMqttClient mqttClient;

    /**
     * Handles the incoming messages
     */
    private CallBack callBack;

    /**
     * Stores the credentials for the secure connection
     */
    private CredentialStore credentialStore;

    /**
     * Constructs the client for supporting secure connection.
     *
     * @param messageClientConfig client configuration
     * @param credentialStore credential store should contain the credentials for the secure connection
     */
    MqttMessageClient(MessageClientConfig messageClientConfig, CredentialStore credentialStore) {
        this.messageClientConfig = messageClientConfig;
        this.credentialStore = credentialStore;
    }

    /**
     * Starts the connection mechanism. The callback will be created
     * and the {@link #reconnect(CallBack)} will be called for the connection mechanism.
     */
    @Override
    public void connect() {
        callBack = new CallBack(this, messageClientConfig);
        reconnect(callBack);
    }

    /**
     * Connects or reconnects to the message broker configured in the given client configuration.
     * The first connect will be triggered by the {@link #connect()} function, the reconnect by
     * the {@link CallBack} instance.
     *
     * @param callBack the message callback instance
     */
    public void reconnect(CallBack callBack) {
        try {
            mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(
                messageClientConfig.getBrokerUrl(),
                messageClientConfig.getClientId(),
                new MemoryPersistence());

            ((org.eclipse.paho.client.mqttv3.MqttClient)mqttClient).setTimeToWait(messageClientConfig.getConnectionTimeout());
            log.info("Client {} connecting to broker (URL: {})", messageClientConfig.getClientId(), messageClientConfig.getBrokerUrl());
            mqttClient.connect(getMqttConnectOptions());
            mqttClient.setCallback(callBack);
        } catch (MqttException e) {
            log.error("Client {} cannot connect to message broker {}", messageClientConfig.getClientId(), messageClientConfig.getBrokerUrl(), e);
        }
    }

    /**
     * Creates and returns the connection options like
     * - session cleaning
     * - max in flight settings
     * - connection timeout
     * - keep alive interval
     * - socket factory for the secured connection
     *
     * @return the connection options
     */
    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setMaxInflight(messageClientConfig.getMaxInFlight());
        connectOptions.setConnectionTimeout(messageClientConfig.getConnectionTimeout());
        connectOptions.setKeepAliveInterval(messageClientConfig.getKeepAliveInterval());

        if (messageClientConfig.isConnectionSecured()) {
            CertificateLoader.KeyStorePasswordPair keyStorePasswordPair = CertificateLoader.getKeyStorePasswordPair(
                credentialStore.getCertificate(),
                credentialStore.getPrivateKey());
            connectOptions.setSocketFactory(new MessageTlsSocketFactory(keyStorePasswordPair));
        }

        return connectOptions;
    }

    /**
     * Publishes the given message payload with the given producing properties.
     * If the quality of service is not set in the producing properties then it will be set
     * from the client configuration
     *
     * @param message message payload
     * @param producingProperties properties for the publishing
     * @throws if the topic is not set in the producing properties
     */
    @Override
    public void publish(byte[] message, ProducingProperties producingProperties) {
        Assert.notNull(producingProperties, "Producing properties must not be null");
        Assert.notNull(producingProperties.getTopic(), "Cannot publish message to an unset topic");
        int qos = producingProperties.getQos() == ProducingProperties.DEFAULT_UNSET_QOS ? messageClientConfig.getQos() : producingProperties.getQos();
        try {
            MqttMessage mqttMessage = new MqttMessage(message);
            mqttMessage.setQos(qos);
            mqttClient.publish(producingProperties.getTopic(), mqttMessage);
        } catch (MqttException e) {
            log.error("Client {} cannot publish message", messageClientConfig.getClientId(), e);
        }
    }

    /**
     * Starts the subscription mechanism with the following steps:
     * - subscribe to the topic with paho's MQTT client
     * - register the subscription in the callback
     *
     * @param consumingProperties the consuming properties
     * @param messageConsumer the message consumer function
     */
    @Override
    public void subscribe(ConsumingProperties consumingProperties, BiConsumer<String, byte[]> messageConsumer) {
        Assert.notNull(consumingProperties, "Consuming properties must not be null");
        Assert.notNull(consumingProperties.getTopic(), "Topic must be set for subscription");
        try {
            mqttClient.subscribe(consumingProperties.getTopic());

            callBack.subscribe(consumingProperties.getTopic(), topicTransformer().apply(consumingProperties.getTopic()), messageConsumer);

            log.info("Client {} subscribed to {}", messageClientConfig.getClientId(), consumingProperties.getTopic());
        } catch (MqttException e) {
            log.error("Client {} cannot subscribe topic {}", messageClientConfig.getClientId(), consumingProperties.getTopic(), e);
        }
    }

    /**
     * Creates and returns a new instance of the message publisher.
     *
     * @return a new instance of the message publisher
     */
    @Override
    public MessagePublisher publisher() {
        return new MessagePublisher(this);
    }

    /**
     * Returns a topic transformation function for handling the shared subscription topics.
     *
     * @return a transformation function for handling the shared subscriptions
     */
    @Override
    public Function<String, String> topicTransformer() {
        return originalTopic -> TopicTransformer.transform(originalTopic).ifShared().andReturn();
    }
}
