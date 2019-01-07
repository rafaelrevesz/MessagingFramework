package com.celadonsea.messagingframework.publisher;

import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import org.junit.Assert;
import org.junit.Test;

public class MessagePublisherTest {

    @Test
    public void shouldPublishMessage() {
        MessageClient messageClient = new TestMessageClient(null);
        messageClient
            .publisher()
            .message("Hello world".getBytes())
            .topic("hello/{x}/world")
            .variable("x", "yyyy")
            .publish();

        messageClient
            .publisher()
            .message("Hello world again".getBytes())
            .topic("hello/{x}/world")
            .variable("x", "yyyy")
            .qos(1)
            .publish();

        messageClient
            .publisher()
            .message("Hello world again and again".getBytes())
            .topic("hello/{x}/world")
            .variable("x", "yyyy")
            .qos(0)
            .publish();

        String messageKey = ((TestMessageClient) messageClient).getMessageKey("hello/yyyy/world", 0);
        String messageKey2 = ((TestMessageClient) messageClient).getMessageKey("hello/yyyy/world", 1);

        Assert.assertEquals(2, ((TestMessageClient) messageClient).getPublishedMessages().get(messageKey).size());
        Assert.assertEquals(1, ((TestMessageClient) messageClient).getPublishedMessages().get(messageKey2).size());
    }
}