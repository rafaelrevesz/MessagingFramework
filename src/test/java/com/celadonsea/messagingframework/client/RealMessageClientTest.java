package com.celadonsea.messagingframework.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("default")
public class RealMessageClientTest {

    @Autowired
    private MessageClient mqttClient;


    @Autowired
    private MessageClient amqpClient;

    @Test
    public void shouldConnectMqttBrokerIfAvailable() {
        mqttClient.connect();
        MqttMessageClient client = new MqttMessageClient(null);
    }

    @Test
    public void shouldConnectAmqpBrokerIfAvailable() {
        amqpClient.connect();
    }
}
