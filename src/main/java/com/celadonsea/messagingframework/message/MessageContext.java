package com.celadonsea.messagingframework.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MessageContext {

    private String topic;

    private String subscribedTopic;

    private Map<String, String> parameterMap = new HashMap<>();
}
