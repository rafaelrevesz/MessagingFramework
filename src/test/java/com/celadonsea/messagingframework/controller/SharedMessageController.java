package com.celadonsea.messagingframework.controller;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessageBody;
import com.celadonsea.messagingframework.annotation.MessagingController;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessagingController(topic = "$share/mygroup/my/topic", client = "testClient")
public class SharedMessageController {

    @Getter
    private String incomingMessage;

    @Listener
    public void sharedMessageListener(@MessageBody String message) {
        this.incomingMessage = message;
    }
}
