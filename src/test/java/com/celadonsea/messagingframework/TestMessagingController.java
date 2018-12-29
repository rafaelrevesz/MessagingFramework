package com.celadonsea.messagingframework;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessageBody;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.annotation.TopicParameter;
import com.celadonsea.messagingframework.message.Message;
import com.celadonsea.messagingframework.message.MessageContext;
import lombok.Getter;

@MessagingController(topic = "valami/{valami2}/valami3", client = "testClient")
public class TestMessagingController {

    @Getter
    private String incomingMessage;

    @Listener("topic1/{var1}")
    public void listenerMethod(@TopicParameter("valami2") String valami2,
                               @TopicParameter("var1") String var1,
                               @MessageBody Message<String> message,
                               MessageContext context) {

        incomingMessage = message.getValue() + "#" + valami2 + "#" + var1;
    }

    @Listener("topic2/{var3}")
    public void anotherListenerMethod(@TopicParameter("valami2") String valami2,
                                      @TopicParameter("var3") int var3,
                                      @MessageBody Message<TestPojo> message,
                                      MessageContext context) {

        incomingMessage = message.getValue().getA() + "#" + message.getValue().getB() + "#" + valami2 + "#" + var3;
    }

    @Listener("topic3")
    public void pojoListenerMethod(@TopicParameter("valami2") String valami2,
                                   @MessageBody TestPojo message,
                                   MessageContext context) {

        incomingMessage = message.getA() + "#" + message.getB() + "#" + valami2;
    }

    @Listener("topic4")
    public void simpleStringListenerMethod(@TopicParameter("valami2") String valami2,
                                           @MessageBody String message,
                                           MessageContext context) {

        incomingMessage = message + "#" + valami2;
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
}