package com.celadonsea.messagingframework.controller;

import com.celadonsea.messagingframework.TestPojo;
import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessageBody;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.annotation.TopicParameter;
import com.celadonsea.messagingframework.message.Message;
import com.celadonsea.messagingframework.message.MessageContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@MessagingController(topic = "any/{any2}/any3", client = "testClient")
public class TestMessagingController {

    @Getter
    private String incomingMessage;

    @Listener("topic1/{var1}")
    public void listenerMethod(@TopicParameter("any2") String any2,
                               @TopicParameter("var1") String var1,
                               @MessageBody Message<String> message,
                               String anyUnusedDummyParameter,
                               MessageContext context) {

        log.info("topic1/{var1}");
        incomingMessage = message.getValue() + "#" + any2 + "#" + var1;
    }

    @Listener("topic2/{var3}")
    public void anotherListenerMethod(@TopicParameter("any2") String any2,
                                      @TopicParameter("var3") int var3,
                                      @MessageBody Message<TestPojo> message,
                                      MessageContext context) {

        incomingMessage = message.getValue().getA() + "#" + message.getValue().getB() + "#" + any2 + "#" + var3;
    }

    @Listener("topic3")
    public void pojoListenerMethod(@TopicParameter("any2") String any2,
                                   @MessageBody TestPojo message,
                                   MessageContext context) {

        incomingMessage = message.getA() + "#" + message.getB() + "#" + any2;
    }

    @Listener("topic4")
    public void simpleStringListenerMethod(@TopicParameter("any2") String any2,
                                           @MessageBody String message,
                                           MessageContext context) {

        incomingMessage = message + "#" + any2;
    }

    @Listener("topic5")
    public void simpleIntListenerMethod(@MessageBody int message) {

        incomingMessage = String.valueOf(message);
    }

    @Listener("topic6")
    public void simpleLongListenerMethod(@MessageBody long message) {

        incomingMessage = String.valueOf(message);
    }

    @Listener("topic7")
    public void simpleByteListenerMethod(@MessageBody byte message) {

        incomingMessage = String.valueOf(message);
    }

    @Listener("topic8")
    public void simpleShortListenerMethod(@MessageBody short message) {

        incomingMessage = String.valueOf(message);
    }

    @Listener("topic9")
    public void simpleByteArrayListenerMethod(@MessageBody byte[] message) {

        incomingMessage = new String(message);
    }

    @Listener("longrunning")
    public void longRunningMethod(@MessageBody byte[] message) {
        log.info("Long running method started...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("Long running method interrupted", e);
        }
        if (incomingMessage == null || !incomingMessage.startsWith("@")) {
            incomingMessage = "";
        }
        incomingMessage += "@";
        log.info("...long running method ended");
    }

    @Listener()
    public void logTopics(MessageContext context) {

        incomingMessage = context.getSubscribedTopic() + "|" + context.getTopic();
    }
}