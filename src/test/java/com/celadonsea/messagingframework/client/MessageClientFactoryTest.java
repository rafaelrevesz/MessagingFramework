package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.security.CredentialStore;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MessageClientFactoryTest {

    private static final String AMQP = "amqp";

    private static final String MQTT = "mqtt";

    private static final String JMS = "jms";

    @Test
    public void shouldCreateMqttClient() {
        MessageClientConfig messageClientConfig = createConfig(MQTT, false);
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig);
        Assert.assertEquals('#', client.getTopicFormat().getMultiLevelWildcard());
        Assert.assertEquals('/', client.getTopicFormat().getLevelSeparator());
        Assert.assertEquals('+', client.getTopicFormat().getSingleLevelWildcard());
    }

    @Test
    public void shouldCreateAmqpClient() {
        MessageClientConfig messageClientConfig = createConfig(AMQP, false);
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig);
        Assert.assertEquals('#', client.getTopicFormat().getMultiLevelWildcard());
        Assert.assertEquals('.', client.getTopicFormat().getLevelSeparator());
        Assert.assertEquals('*', client.getTopicFormat().getSingleLevelWildcard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateSecuredMqttClientWithUnsecuredMethod() {
        MessageClientConfig messageClientConfig = createConfig(MQTT, true);
        MessageClientFactory.getFactory().getClient(messageClientConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateUnsupportedClient() {
        MessageClientConfig messageClientConfig = createConfig(JMS, false);
        MessageClientFactory.getFactory().getClient(messageClientConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateSecuredMqttClientWithEmptyCredentialStore() {
        MessageClientConfig messageClientConfig = createConfig(MQTT, true);
        MessageClientFactory.getFactory().getClient(messageClientConfig, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateSecuredAmqpClientWithEmptyCredentialStore() {
        MessageClientConfig messageClientConfig = createConfig(AMQP, true);
        MessageClientFactory.getFactory().getClient(messageClientConfig, null);
    }

    @Test
    public void shouldCreateSecuredMqttClient() {
        MessageClientConfig messageClientConfig = createConfig(MQTT, true);
        CredentialStore credentialStore = createCredentialStore();
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig, credentialStore);
        Assert.assertEquals('#', client.getTopicFormat().getMultiLevelWildcard());
        Assert.assertEquals('/', client.getTopicFormat().getLevelSeparator());
        Assert.assertEquals('+', client.getTopicFormat().getSingleLevelWildcard());
    }

    @Test
    public void shouldCreateSecuredAmqpClient() {
        MessageClientConfig messageClientConfig = createConfig(AMQP, true);
        CredentialStore credentialStore = createCredentialStore();
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig, credentialStore);
        Assert.assertEquals('#', client.getTopicFormat().getMultiLevelWildcard());
        Assert.assertEquals('.', client.getTopicFormat().getLevelSeparator());
        Assert.assertEquals('*', client.getTopicFormat().getSingleLevelWildcard());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateUnsupportedSecuredClient() {
        MessageClientConfig messageClientConfig = createConfig(JMS, false);
        CredentialStore credentialStore = createCredentialStore();
        MessageClientFactory.getFactory().getClient(messageClientConfig, credentialStore);
    }

    @Test
    public void shouldCreateUnsecuredMqttClientWithSecuredMethod() {
        MessageClientConfig messageClientConfig = createConfig(MQTT, false);
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig, null);
        Assert.assertEquals('#', client.getTopicFormat().getMultiLevelWildcard());
        Assert.assertEquals('/', client.getTopicFormat().getLevelSeparator());
        Assert.assertEquals('+', client.getTopicFormat().getSingleLevelWildcard());
    }

    @Test
    public void shouldCreateUnsecuredAmqpClientWithSecuredMethod() {
        MessageClientConfig messageClientConfig = createConfig(AMQP, false);
        MessageClient client = MessageClientFactory.getFactory().getClient(messageClientConfig, null);
        Assert.assertEquals('#', client.getTopicFormat().getMultiLevelWildcard());
        Assert.assertEquals('.', client.getTopicFormat().getLevelSeparator());
        Assert.assertEquals('*', client.getTopicFormat().getSingleLevelWildcard());
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

            @Override
            public int getMaxThread() {
                return 10;
            }

            @Override
            public int getThreadKeepAliveTime() {
                return 1;
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
