package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.client.TestMessageClient;
import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.controller.SharedMessageController;
import com.celadonsea.messagingframework.controller.TestMessagingController;
import com.celadonsea.messagingframework.scanner.ListenerCallbackPostProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class CallBackTest {

    private CallBack callBack;

    private MessageClient messageClient;

    private TestMessagingController testMessagingController;

    private SharedMessageController sharedMessageController;

    private ListenerCallbackPostProcessor listenerCallbackPostProcessor;

    @Before
    public void setup() {
        MessageClientConfig config = getConfig();
        messageClient = new TestMessageClient(config);
        callBack = new CallBack(messageClient, config);
        messageClient.reconnect(callBack);
        testMessagingController = new TestMessagingController();
        sharedMessageController = new SharedMessageController();
        listenerCallbackPostProcessor = new ListenerCallbackPostProcessor();
        register(testMessagingController);
        register(sharedMessageController);
    }

    private MessageClientConfig getConfig() {
        return new MessageClientConfig() {
            @Override
            public String getClientType() {
                return "mqtt";
            }

            @Override
            public String getClientId() {
                return "unitTest";
            }

            @Override
            public String getBrokerUrl() {
                return "memory";
            }

            @Override
            public int getMaxInFlight() {
                return 0;
            }

            @Override
            public int getConnectionTimeout() {
                return 0;
            }

            @Override
            public int getKeepAliveInterval() {
                return 0;
            }

            @Override
            public int getQos() {
                return 0;
            }

            @Override
            public boolean isConnectionSecured() {
                return false;
            }

            @Override
            public int getMaxThread() {
                return 10;
            }

            @Override
            public int getThreadKeepAliveTime() {
                return 1;
            }
        };
    }

    private void register(Object handler) {

        for (Annotation annotation : handler.getClass().getAnnotations()) {
            if (annotation.annotationType() == MessagingController.class) {
                processController(handler, (MessagingController) annotation);
            }
        }
    }

    private void processController(Object handler, MessagingController annotation) {
        for (Method method : handler.getClass().getMethods()) {
            for (Annotation methodAnnotation : method.getAnnotations()) {
                if (methodAnnotation.annotationType() == Listener.class) {
                    listenerCallbackPostProcessor.processListenerMethod(handler, messageClient, method, (Listener) methodAnnotation, annotation);
                }
            }
        }
    }

    @Test
    public void shouldReceiveGenericStringMessage() {
        callBack.messageArrived(
            "any/topicvariable1/any3/topic1/topicvariable2",
            ("{'timestamp':1,'value':'hello messaging'}".replaceAll("'", "\"").getBytes()));
        await()
            .atMost(1, TimeUnit.SECONDS)
            .until(testControllerHasTheRightValue("hello messaging#topicvariable1#topicvariable2"));
    }

    @Test
    public void shouldReceiveGenericPojoMessage() {
        callBack.messageArrived(
            "any/topicvariable1a/any3/topic2/56",
            ("{'timestamp':1,'value':{'a':42,'b':'hello new messaging'}}".replaceAll("'", "\"").getBytes()));

        await()
            .atMost(1, TimeUnit.SECONDS)
            .until(testControllerHasTheRightValue("42#hello new messaging#topicvariable1a#56"));
    }

    @Test
    public void shouldReceivePojoMessage() {
        callBack.messageArrived(
            "any/topicvariable1b/any3/topic3",
            ("{'a':48,'b':'hello pojo messaging'}".replaceAll("'", "\"").getBytes()));

        await()
            .atMost(1, TimeUnit.SECONDS)
            .until(testControllerHasTheRightValue("48#hello pojo messaging#topicvariable1b"));
    }

    @Test
    public void shouldReceiveStringMessage() {
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic4",
            "hello simple messaging".getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue("hello simple messaging#topicvariable1c"));
    }

    @Test
    public void shouldReceiveIntMessage() {
        String content = String.valueOf(Integer.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic5",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue(content));
    }

    @Test
    public void shouldReceiveLongMessage() {
        String content = String.valueOf(Long.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic6",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue(content));
    }

    @Test
    public void shouldReceiveByteMessage() {
        String content = String.valueOf(Byte.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic7",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue(content));
    }

    @Test
    public void shouldNotReceiveIntMessageIfNumberTooBig() {
        String content = String.valueOf(Long.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic5",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue("0"));
    }

    @Test
    public void shouldNotReceiveLongMessageIfNumberInvalid() {
        String content = "a";
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic6",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue("0"));
    }

    @Test
    public void shouldNotReceiveByteMessageIfNumberTooBig() {
        String content = String.valueOf(Short.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic7",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue("0"));
    }

    @Test
    public void shouldNotReceiveShortMessageIfNumberTooBig() {
        String content = String.valueOf(Integer.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic8",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue("0"));
    }

    @Test
    public void shouldReceiveShortMessage() {
        String content = String.valueOf(Short.MAX_VALUE);
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic8",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue(content));
    }

    @Test
    public void shouldReceiveByteArrayMessage() {
        String content = "bytearraytest";
        callBack.messageArrived(
            "any/topicvariable1c/any3/topic9",
            content.getBytes());

        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue(content));
    }

    @Test
    public void shouldStartSeparateThreadsForEachMessage() {
        byte sleepTime = 2;
        byte[] content = {sleepTime};
        long startTime = System.currentTimeMillis();
        // Thread pool has 10 threads, we will start 15 processes
        for (int i = 0; i < 15; i++) {
            callBack.messageArrived("any/topicvar1/any3/longrunning", content);
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        Assert.assertTrue(elapsedTime < 1_000);
        await().atMost(sleepTime * 2 + 2, TimeUnit.SECONDS).until(testControllerHasTheRightValue("@@@@@@@@@@@@@@@"));
    }

    @Test
    public void shouldSubscribeToSharedTopics() throws InterruptedException {
        callBack.messageArrived("my/topic", "Heureca!".getBytes());
        await().atMost(1, TimeUnit.SECONDS).until(sharedControllerHasTheRightValue());
    }

    @Test
    public void shouldAccessDefaultSubscription() throws InterruptedException {
        callBack.messageArrived("any/unittest/any3", "any content".getBytes());
        await().atMost(1, TimeUnit.SECONDS).until(testControllerHasTheRightValue("any/{any2}/any3|any/unittest/any3"));
    }

    private Callable<Boolean> sharedControllerHasTheRightValue() {
        return () -> "Heureca!".equals(sharedMessageController.getIncomingMessage());
    }

    private Callable<Boolean> testControllerHasTheRightValue(String expectedMessage) {
        return () -> expectedMessage.equals(testMessagingController.getIncomingMessage());
    }
}
