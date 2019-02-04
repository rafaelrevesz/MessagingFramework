package com.celadonsea.messagingframework;

import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import com.celadonsea.messagingframework.controller.TestMessagingController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("default")
public class ApplicationTest {

    @Autowired
    private TestMessagingController testMessagingController;

    @Autowired
    private MessageClient testClient;

    @Test
    public void shouldRecognizeControllerAndHandleMessage() throws InterruptedException {


        ((TestMessageClient) testClient).getCallBack().messageArrived(
            "any/variable1/any3/topic1/variable2",
            ("{'timestamp':42, 'value': 'AnyMessage'}".replaceAll("'", "\"")).getBytes());

        await()
            .atMost(500, TimeUnit.MILLISECONDS)
            .until(() -> "AnyMessage#variable1#variable2".equals(testMessagingController.getIncomingMessage()));
    }
}
