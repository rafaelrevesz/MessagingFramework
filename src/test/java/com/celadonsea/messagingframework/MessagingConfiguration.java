package com.celadonsea.messagingframework;

import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import com.celadonsea.messagingframework.config.MessageClientConfig;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MessagingConfiguration implements MessageClientConfig {

    private String clientType= "mqtt";

    private String clientId = "testClient";

    private String brokerUrl = "memory";

    private int maxInFlight = 100;

    private int connectionTimeout = 30;

    private int keepAliveInterval = 30;

    private int qos = 0;

    private boolean connectionSecured = false;

    @Bean
    public MessageClient testClient() {
        TestMessageClient testMessageClient = new TestMessageClient(this);
        testMessageClient.connect();
        return testMessageClient;
    }
}
