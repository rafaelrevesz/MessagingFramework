package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.config.MessageClientConfig;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MqttClientConfiguration implements MessageClientConfig {

    private String clientType = "mqtt";

    private String clientId = "mqtttestclient";

    private String brokerUrl = "tcp://localhost:1883";

    private int maxInFlight = 100;

    private int connectionTimeout = 1000;

    private int keepAliveInterval = 1000;

    private int qos = 2;

    private boolean connectionSecured = false;

    private int maxThread = 10;

    private int threadKeepAliveTime = 1;

    @Bean
    public MessageClient mqttClient() {
        return MessageClientFactory.getFactory().getClient(this);
    }
}
