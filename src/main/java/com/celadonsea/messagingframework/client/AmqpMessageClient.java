package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.core.ConsumingProperties;
import com.celadonsea.messagingframework.core.ProducingProperties;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.security.CertificateLoader;
import com.celadonsea.messagingframework.security.CredentialStore;
import com.celadonsea.messagingframework.security.MessageTlsSocketFactory;
import com.celadonsea.messagingframework.topic.TopicFormat;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Message client implementation for AMQP protocol based on RabbitMQ library
 * (https://www.rabbitmq.com/java-client.html).
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AmqpMessageClient implements MessageClient {

    /**
     * Default exchange
     */
    private static final String MESSAGING_DEFAULT_EXCHANGE = "messaging_default_exchange";

    /**
     * The client configuration
     */
    private final MessageClientConfig messageClientConfig;

    /**
     * Stores the credentials for the secure connection
     */
    private CredentialStore credentialStore;

    /**
     * The topic format which is valid for AMQP protocol
     * -- GETTER --
     * Returns the topic format set for the AMQP message client
     * -- SETTER --
     * Sets the topic format for the AMQP message client
     *
     * @param the developer specified topic format
     * @return the topic format
     */
    @Getter
    @Setter
    private TopicFormat topicFormat = new TopicFormat('.', '*', '#');

    /**
     * Convenience factory class to facilitate opening a {@link Connection} to a RabbitMQ node.
     */
    private ConnectionFactory connectionFactory = new ConnectionFactory();

    /**
     * Connection interface from RabbitMQ for AMQP
     */
    private Connection connection;

    /**
     * Interface to a channel.
     * -- GETTER --
     * Returns the channel created by the connection.
     *
     * @return AMQP channel
     *
     */
    @Getter
    private Channel channel;

    /**
     * Handles the incoming messages
     */
    private CallBack callBack;

    /**
     * Constructs the client for supporting secure connection.
     *
     * @param messageClientConfig client configuration
     * @param credentialStore credential store should contain the credentials for the secure connection
     */
    AmqpMessageClient(MessageClientConfig messageClientConfig, CredentialStore credentialStore) {
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
     * The first connect will be triggered by the {@link #connect()} function.
     * The connection is built up with the settings:
     * - message broker URL
     * - automatic recovery
     * - connection timeout
     * - socket factory for the secured connection
     *
     * @param callBack the message callback instance
     */
    @Override
    public void reconnect(CallBack callBack) {
        try {
            connectionFactory.setUri(messageClientConfig.getBrokerUrl());
            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.setConnectionTimeout(messageClientConfig.getConnectionTimeout());

            if (messageClientConfig.isConnectionSecured()) {
                CertificateLoader.KeyStorePasswordPair keyStorePasswordPair = CertificateLoader.getKeyStorePasswordPair(
                    credentialStore.getCertificate(),
                    credentialStore.getPrivateKey());
                connectionFactory.setSocketFactory(new MessageTlsSocketFactory(keyStorePasswordPair));
            }

            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException | URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Cannot connect to AMQP broker", e);
        }
    }

    /**
     * The channel and the connection will be closed before the client destroys.
     */
    @PreDestroy
    public void closeConnections() {
        if (channel != null) {
            try {
                channel.close();
                log.info("Channel closed");
            } catch (IOException | TimeoutException e) {
                log.error("Cannot close channel", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
                log.info("Connection closed");
            } catch (IOException e) {
                log.error("Cannot close connection", e);
            }
        }
    }

    /**
     * Publishes the given message payload to the given topic and to the exchange.
     * If the exchange is not set in the {@code producingProperties} then the default exchange will be used.
     * The message will be published with the channel interface.
     *
     * @param message message payload
     * @param producingProperties properties for the publishing
     * @throws IllegalArgumentException if routing key (topic) is null in the {@code producingProperties}
     */
    @Override
    public void publish(byte[] message, ProducingProperties producingProperties) {
        Assert.notNull(producingProperties, "Producing properties must not be null");
        Assert.notNull(producingProperties.getTopic(), "Publishing needs routing key (topic)");
        String exchange = producingProperties.getExchange() != null ? producingProperties.getExchange() : MESSAGING_DEFAULT_EXCHANGE;
        try {
            channel.basicPublish(exchange, producingProperties.getTopic(), null, message);
        } catch (IOException e) {
            log.error("Cannot publish message to default topic for topic {}", producingProperties.getTopic(), e);
        }
    }

    /**
     * Starts the subscription mechanism with the following steps:
     * - declares an exchange
     * - declares a queue with the name of the routing key (topic)
     * - binds the queue to the exchange with the given routing key
     * - sets the consuming with the routing key (topic) and the callback
     * - registers the subscription in the callback
     *
     * @param consumingProperties the consuming properties
     * @param messageConsumer the message consumer function
     * @throws IllegalArgumentException if consuming properties or routing key (topic) in the consuming properties is null
     */
    @Override
    public void subscribe(ConsumingProperties consumingProperties, BiConsumer<String, byte[]> messageConsumer) {
        Assert.notNull(consumingProperties, "Consuming properties must not be null");
        Assert.notNull(consumingProperties.getTopic(), "Publishing needs routing key (topic)");
        String exchange = consumingProperties.getExchange() != null ? consumingProperties.getExchange() : MESSAGING_DEFAULT_EXCHANGE;

        try {
            channel.exchangeDeclare(exchange, "topic");
            channel.queueDeclare(consumingProperties.getTopic(), false, false, false, null);
            channel.queueBind(consumingProperties.getTopic(), exchange, consumingProperties.getTopic());
            channel.basicConsume(consumingProperties.getTopic(), true, callBack);

            callBack.subscribe(consumingProperties.getTopic(), messageConsumer);
        } catch (IOException e) {
            log.error("Cannot subscribe to exchange {} and topic {}", exchange, consumingProperties.getTopic(), e);
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
     * Returns a function which do no transformation. In AMQP there is no need to transform any
     * kind of topics.
     *
     * @return a function which returns the original routing key (topic)
     */
    @Override
    public Function<String, String> topicTransformer() {
        return s -> s;
    }
}
