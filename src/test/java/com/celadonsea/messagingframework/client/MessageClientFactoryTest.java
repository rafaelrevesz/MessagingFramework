package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.security.CredentialStore;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MessageClientFactoryTest {

    @Test
    public void shouldCreateMqttClient() {
        MessageClientConfig messageClientConfig = createConfig("mqtt", false);
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig);
        Assert.assertEquals('#', client.topicFormat().getJoker());
        Assert.assertEquals('/', client.topicFormat().getLevelSeparator());
        Assert.assertEquals('+', client.topicFormat().getWildcard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateSecuredClientWithUnsecuredMethod() {
        MessageClientConfig messageClientConfig = createConfig("mqtt", true);
        MessageClientFactory.getFactory().getClient(messageClientConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateUnsupportedClient() {
        MessageClientConfig messageClientConfig = createConfig("amqp", false);
        MessageClientFactory.getFactory().getClient(messageClientConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateSecuredClientWithEmptyCredentialStore() {
        MessageClientConfig messageClientConfig = createConfig("mqtt", true);
        MessageClientFactory.getFactory().getClient(messageClientConfig, null);
    }

    @Test
    public void shouldCreateSecuredClient() {
        MessageClientConfig messageClientConfig = createConfig("mqtt", true);
        CredentialStore credentialStore = createCredentialStore();
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig, credentialStore);
        Assert.assertEquals('#', client.topicFormat().getJoker());
        Assert.assertEquals('/', client.topicFormat().getLevelSeparator());
        Assert.assertEquals('+', client.topicFormat().getWildcard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateUnsupportedSecuredClient() {
        MessageClientConfig messageClientConfig = createConfig("amqp", false);
        CredentialStore credentialStore = createCredentialStore();
        MessageClientFactory.getFactory().getClient(messageClientConfig, credentialStore);
    }

    @Test
    public void shouldCreateUnsecuredClientWithSecuredMethod() {
        MessageClientConfig messageClientConfig = createConfig("mqtt", false);
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig, null);
        Assert.assertEquals('#', client.topicFormat().getJoker());
        Assert.assertEquals('/', client.topicFormat().getLevelSeparator());
        Assert.assertEquals('+', client.topicFormat().getWildcard());
    }

    private MessageClientConfig createConfig(final String clientType, final boolean secured) {
        return new MessageClientConfig() {
            @Override
            public String getClientType() {
                return clientType;
            }

            @Override
            public String getClientId() {
                return null;
            }

            @Override
            public String getBrokerUrl() {
                return null;
            }

            @Override
            public int getMaxInFlight() {
                return 0;
            }

            @Override
            public int getConnectionTimeout() {
                return 0;
            }

            @Override
            public int getKeepAliveInterval() {
                return 0;
            }

            @Override
            public int getQos() {
                return 0;
            }

            @Override
            public boolean isConnectionSecured() {
                return secured;
            }
        };
    }

    private CredentialStore createCredentialStore() {
        return new CredentialStore() {
            @Override
            public InputStream getCertificate() {
                return new ByteArrayInputStream("certificate".getBytes());
            }

            @Override
            public InputStream getPrivateKey() {
                return new ByteArrayInputStream("privatekey".getBytes());
            }
        };
    }
}
