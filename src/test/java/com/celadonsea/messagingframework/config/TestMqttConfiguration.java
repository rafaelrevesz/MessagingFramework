package com.celadonsea.messagingframework.config;

import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import com.celadonsea.messagingframework.config.MessageClientConfig;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TestMqttConfiguration implements MessageClientConfig {

    private String clientType= "mqtt";

    private String clientId = "testMqttClient";

    private String brokerUrl = "memory";

    private int maxInFlight = 100;

    private int connectionTimeout = 30;

    private int keepAliveInterval = 30;

    private int qos = 0;

    private boolean connectionSecured = false;

    private int maxThread = 10;

    private int threadKeepAliveTime = 1;

    @Bean
    public MessageClient testClient() {
        TestMessageClient testMessageClient = new TestMessageClient(this);
        testMessageClient.connect();
        return testMessageClient;
    }
}
