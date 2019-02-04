package com.celadonsea.messagingframework.config;

import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TestAmqpConfiguration implements MessageClientConfig {

    private String clientType= "amqp";

    private String clientId = "testAmqpClient";

    private String brokerUrl = "memory";

    private int maxInFlight = 100;

    private int connectionTimeout = 30;

    private int keepAliveInterval = 30;

    private int qos = 0;

    private boolean connectionSecured = false;

    private int maxThread = 10;

    private int threadKeepAliveTime = 1;

    @Bean
    public MessageClient amqpTestClient() {
        TestMessageClient testMessageClient = new TestMessageClient(this);
        testMessageClient.connect();
        return testMessageClient;
    }
}
