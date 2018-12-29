package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.security.CredentialStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Slf4j
public class MessageClientFactory {

    @Getter
    private static final MessageClientFactory factory = new MessageClientFactory();

    private MessageClientFactory() {}

    public MessageClient getClient(MessageClientConfig messageClientConfig) {
        Assert.notNull(messageClientConfig, "Cannot create messaging client: message configuration is null!");
        Assert.notNull(messageClientConfig.getClientType(), "Cannot create messaging client: client type is null!");

        if (messageClientConfig.isConnectionSecured()) {
            throw new IllegalArgumentException("Secured connection must be configured with a credential store.");
        }

        if (messageClientConfig.getClientType().equalsIgnoreCase("mqtt")) {
            return new MqttMessageClient(messageClientConfig);
        } else {
            throw new IllegalArgumentException("Cannot create messaging client: unsupported client type " + messageClientConfig.getClientType());
        }
    }

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

        if (messageClientConfig.getClientType().equalsIgnoreCase("mqtt")) {
            return new MqttMessageClient(messageClientConfig, credentialStore);
        } else {
            throw new IllegalArgumentException("Cannot create messaging client: unsupported client type " + messageClientConfig.getClientType());
        }
    }

}
