package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.TestMessagingController;
import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class CallBackTest {

    private CallBack callBack;

    private MessageClient messageClient;

    private TestMessagingController testMessagingController;

    private MessageListener messageListener;

    @Before
    public void setup() {
        messageClient = new TestMessageClient();
        callBack = new CallBack(messageClient);
        messageClient.reconnect(callBack);
        testMessagingController = new TestMessagingController();
        messageListener = new MessageListener();
        register(testMessagingController);
    }

    private void register(Object handler) {

        for (Annotation annotation : handler.getClass().getAnnotations()) {
            if (annotation.annotationType() == MessagingController.class) {
                processController(handler, (MessagingController) annotation);
            }
        }
    }

    private void processController(Object handler, MessagingController annotation) {
        String baseTopic = annotation.topic();
        for (Method method : handler.getClass().getMethods()) {
            for (Annotation methodAnnotation : method.getAnnotations()) {
                if (methodAnnotation.annotationType() == Listener.class) {
                    messageListener.processListener(handler, messageClient, method, (Listener) methodAnnotation, baseTopic);
                }
            }
        }
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
