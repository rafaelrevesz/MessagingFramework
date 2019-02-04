package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.security.CredentialStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Factory provides functionality to create a corresponding messaging client.
 * The two supported message protocols are MQTT and AMQP.
 * The {@link MessageClientConfig#getClientType()  @MessageClientConfig#getClientType()} defines the
 * client type to create ("mqtt" or "amqp")
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see MessageClientConfig
 * @see MqttMessageClient
 * @see AmqpMessageClient
 */
@Slf4j
public class MessageClientFactory {

    /**
     * The factory instance
     */
    @Getter
    private static final MessageClientFactory factory = new MessageClientFactory();

    /**
     * The type ID for MQTT clients
     */
    private static final String MQTT = "mqtt";
    /**
     * The type ID for AMQP clients
     */
    private static final String AMQP = "amqp";

    /**
     * The constructor is private because it's a singleton class.
     */
    private MessageClientFactory() {}

    /**
     * Creates a messaging client instance with the given client configuration.
     * This method is for creating clients without secured connection possibilities.
     *
     * @param messageClientConfig the client configuration
     * @return the new messaging client instance
     * @throws IllegalArgumentException if the requested configuration needs a secured connection (no credential parameter)
     *                                  or if the given configuration or client type in the configuration is null
     *                                  or the configured client type is not supported.
     */
    public MessageClient getClient(MessageClientConfig messageClientConfig) {
        Assert.notNull(messageClientConfig, "Cannot create messaging client: message configuration is null!");
        Assert.notNull(messageClientConfig.getClientType(), "Cannot create messaging client: client type is null!");

        if (messageClientConfig.isConnectionSecured()) {
            throw new IllegalArgumentException("Secured connection must be configured with a credential store.");
        }

        if (messageClientConfig.getClientType().equalsIgnoreCase(MQTT)) {
            return new MqttMessageClient(messageClientConfig);
        } else if (messageClientConfig.getClientType().equalsIgnoreCase(AMQP)) {
            return new AmqpMessageClient(messageClientConfig);
        } else {
            throw new IllegalArgumentException("Cannot create messaging client: unsupported client type " + messageClientConfig.getClientType());
        }
    }

    /**
     * Creates a messaging client instance with the given client configuration.
     * This method is for creating clients which are able to open secured connection with.
     *
     * @param messageClientConfig the client configuration
     * @param credentialStore the credential store for the secured connections
     * @return the new messaging client instance
     * @throws IllegalArgumentException if the requested configuration needs a secured connection (no credential parameter)
     *                                  or if the given configuration or client type in the configuration is null
     *                                  or the configured client type is not supported.
     */
    public MessageClient getClient(MessageClientConfig messageClientConfig, CredentialStore credentialStore) {
        Assert.notNull(messageClientConfig, "Cannot create messaging client: message configuration is null!");
        Assert.notNull(messageClientConfig.getClientType(), "Cannot create messaging client: client type is null!");

        if (credentialStore == null) {
            if (messageClientConfig.isConnectionSecured()) {
                throw new IllegalArgumentException("Secured connection must be configured with a credential store.");
            } else {
                log.warn("For an unsecured connection you should use the method without credential store parameter!");
            }
        }

        if (messageClientConfig.getClientType().equalsIgnoreCase(MQTT)) {
            return new MqttMessageClient(messageClientConfig, credentialStore);
        } else if (messageClientConfig.getClientType().equalsIgnoreCase(AMQP)) {
            return new AmqpMessageClient(messageClientConfig, credentialStore);
        } else {
            throw new IllegalArgumentException("Cannot create messaging client: unsupported client type " + messageClientConfig.getClientType());
        }
    }

}
