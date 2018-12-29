package com.celadonsea.messagingframework.client;

import com.celadonsea.messagingframework.TestMessagingController;
import com.celadonsea.messagingframework.listener.CallBack;
import com.celadonsea.messagingframework.listener.MessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CallBackTest {

    private CallBack callBack;

    private TestMessagingController testMessagingController;

    @Before
    public void setup() {
        callBack = new CallBack();
        testMessagingController = new TestMessagingController();
        MessageListener messageListener = new MessageListener(callBack, new ObjectMapper());
        messageListener.register(testMessagingController);
    }

    @Test
    public void shouldReceiveGenericStringMessage() {
        callBack.messageArrived(
            "valami/topicvariable1/valami3/topic1/topicvariable2",
            ("{'timestamp':1,'value':'hello messaging'}".replaceAll("'", "\"").getBytes()));

        Assert.assertEquals("hello messaging#topicvariable1#topicvariable2", testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveGenericPojoMessage() {
        callBack.messageArrived(
            "valami/topicvariable1a/valami3/topic2/56",
            ("{'timestamp':1,'value':{'a':42,'b':'hello new messaging'}}".replaceAll("'", "\"").getBytes()));

        Assert.assertEquals("42#hello new messaging#topicvariable1a#56", testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceivePojoMessage() {
        callBack.messageArrived(
            "valami/topicvariable1b/valami3/topic3",
            ("{'a':48,'b':'hello pojo messaging'}".replaceAll("'", "\"").getBytes()));

        Assert.assertEquals("48#hello pojo messaging#topicvariable1b", testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveStringMessage() {
        callBack.messageArrived(
            "valami/topicvariable1c/valami3/topic4",
            "hello simple messaging".getBytes());

        Assert.assertEquals("hello simple messaging#topicvariable1c", testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveIntMessage() {
        String content = String.valueOf(Integer.MAX_VALUE);
        callBack.messageArrived(
            "valami/topicvariable1c/valami3/topic5",
            content.getBytes());

        Assert.assertEquals(content, testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveLongMessage() {
        String content = String.valueOf(Long.MAX_VALUE);
        callBack.messageArrived(
            "valami/topicvariable1c/valami3/topic6",
            content.getBytes());

        Assert.assertEquals(content, testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveByteMessage() {
        String content = String.valueOf(Byte.MAX_VALUE);
        callBack.messageArrived(
            "valami/topicvariable1c/valami3/topic7",
            content.getBytes());

        Assert.assertEquals(content, testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveShortMessage() {
        String content = String.valueOf(Short.MAX_VALUE);
        callBack.messageArrived(
            "valami/topicvariable1c/valami3/topic8",
            content.getBytes());

        Assert.assertEquals(content, testMessagingController.getIncomingMessage());
    }

    @Test
    public void shouldReceiveByteArrayMessage() {
        String content = "bytearraytest";
        callBack.messageArrived(
            "valami/topicvariable1c/valami3/topic9",
            content.getBytes());

        Assert.assertEquals(content, testMessagingController.getIncomingMessage());
    }
}
