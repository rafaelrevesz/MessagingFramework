package com.celadonsea.messagingframework;

import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("default")
public class ApplicationTest {

    @Autowired
    private TestMessagingController testMessagingController;

    @Autowired
    private MessageClient messageClient;

    @Test
    public void shouldRecognizeControllerAndHandleMessage() {


        ((TestMessageClient)messageClient).getCallBack().messageArrived(
            "valami/variable1/valami3/topic1/variable2",
            ("{'timestamp':42, 'value': 'AnyMessage'}".replaceAll("'", "\"")).getBytes());


        Assert.assertEquals("AnyMessage#variable1#variable2", testMessagingController.getIncomingMessage());
    }
}
