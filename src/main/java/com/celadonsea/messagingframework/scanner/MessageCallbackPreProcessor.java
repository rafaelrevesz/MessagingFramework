package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessageBody;
import com.celadonsea.messagingframework.annotation.TopicParameter;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.message.MessageContext;
import com.celadonsea.messagingframework.topic.TopicFormat;
import com.celadonsea.messagingframework.topic.TopicParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MessageCallbackPreProcessor {

    private ObjectMapper objectMapper = new ObjectMapper();

    public void processListenerMethod(Object handler, MessageClient client, Method method, Listener methodAnnotation, String baseTopic) {
        TopicFormat topicFormat = client.topicFormat();

        String subTopic = methodAnnotation.value();
        List<ParameterDescriptor> parameters = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            parameters.add(analyzeParameterType(parameter));
        }
        String topicDefinition = baseTopic + topicFormat.getLevelSeparator() + subTopic;
        String subscribedTopic = TopicParser.changeSubscriptionFormatForMessageBrokers(topicDefinition, "" + topicFormat.getWildcard());
        client.subscribe(subscribedTopic, (topic, message) -> subscriptionLambda(handler, method, parameters, topicDefinition, topic, topicFormat, message));
    }

    private ParameterDescriptor analyzeParameterType(Parameter parameter) {
        ParameterDescriptor parameterDescriptor = null;
        if (parameter.getAnnotations() != null && parameter.getAnnotations().length > 0) {
            if (parameter.isAnnotationPresent(TopicParameter.class)) {
                String parameterName = parameter.getAnnotation(TopicParameter.class).value();
                return new ParameterDescriptor(parameterName, parameter.getType());
            }
            if (parameter.isAnnotationPresent(MessageBody.class)) {
                // message body
                //https://stackoverflow.com/questions/6846244/jackson-and-generic-type-reference
                if (hasSupportedPrimitiveType(parameter.getType())) {
                    parameterDescriptor = new ParameterDescriptor(ParameterDescriptor.PARAMETER_NAME_BODY, parameter.getType());
                } else {
                    parameterDescriptor = new ParameterDescriptor(ParameterDescriptor.PARAMETER_NAME_BODY, parameter.getParameterizedType());
                }
            }
        } else if (parameter.getType() == MessageContext.class) {
            // parameter without annotation can only be the context
            parameterDescriptor = ParameterDescriptor.context();
        } else {
            // otherwise the parameter is null
            parameterDescriptor = ParameterDescriptor.empty();
        }
        return parameterDescriptor;
    }

    private boolean hasSupportedPrimitiveType(Class clazz) {
        return clazz == String.class ||
            clazz == byte[].class ||
            clazz == Integer.class ||
            clazz == int.class ||
            clazz == Long.class ||
            clazz == long.class ||
            clazz == Short.class ||
            clazz == short.class ||
            clazz == Byte.class ||
            clazz == byte.class;
    }

    private void subscriptionLambda(Object handler,
                                    Method method,
                                    List<ParameterDescriptor> parameterDescriptors,
                                    String subscribedTopic,
                                    String topic,
                                    TopicFormat topicFormat,
                                    byte[] content) {
        try {
            Object[] parameters = new Object[parameterDescriptors.size()];
            Map<String, String> parsedVariables = TopicParser.parseVariables(
                topic,
                subscribedTopic,
                "" + topicFormat.getLevelSeparator(),
                "" + topicFormat.getJoker());
            int parameterCount = 0;
            MessageContext messageContext = new MessageContext();
            messageContext.setParameterMap(parsedVariables);
            messageContext.setTopic(topic);
            messageContext.setSubscribedTopic(subscribedTopic);
            for (ParameterDescriptor parameterDescriptor : parameterDescriptors) {
                if (parameterDescriptor.isEmpty()) {
                    parameters[parameterCount] = null;
                } else if (parameterDescriptor.isContext()) {
                    parameters[parameterCount] = messageContext;
                } else if (parameterDescriptor.isBody()) {
                    parameters[parameterCount] = parseBody(content, parameterDescriptor);
                } else { // topic parameter
                    parameters[parameterCount] = parseTopicParameter(parsedVariables, parameterDescriptor);
                }

                parameterCount++;
            }
            // https://stackoverflow.com/questions/16207283/how-to-pass-multiple-parameters-to-a-method-in-java-reflections/16254447
            method.invoke(handler, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot call listener method for topic {}", subscribedTopic, e);
        }
    }

    private Object parseTopicParameter(Map<String, String> parsedVariables, ParameterDescriptor parameterDescriptor) {
        Object obj = null;
        String parameterValue = parsedVariables.get(parameterDescriptor.getName());
        if (hasSupportedPrimitiveType(parameterDescriptor.getClazz())) {
            obj = parsePrimitiveTypes(parameterDescriptor, parameterValue.getBytes());
        }
        return obj;
    }

    private Object parseBody(byte[] content, ParameterDescriptor parameterDescriptor) {
        Object obj = null;
        if (hasSupportedPrimitiveType(parameterDescriptor.getClazz())) {
            obj = parsePrimitiveTypes(parameterDescriptor, content);
        } else {
            try {
                JavaType javaType = objectMapper.getTypeFactory().constructType(parameterDescriptor.getType());
                obj = objectMapper.readValue(content, javaType);
            } catch (IOException e) {
                log.error("Cannot read message", e);
            }
        }
        return obj;
    }

    private Object parsePrimitiveTypes(ParameterDescriptor parameterDescriptor, byte[] content) {
        String stringContent = new String(content);
        Object obj = null;
        if (parameterDescriptor.getClazz() == String.class) {
            obj = stringContent;
        } else if (parameterDescriptor.getClazz() == byte[].class) {
            obj = content;
        } else if (parameterDescriptor.getClazz() == Integer.class) {
            obj = parseInteger(stringContent, null);
        } else if (parameterDescriptor.getClazz() == int.class) {
            obj = parseInteger(stringContent, 0);
        } else if (parameterDescriptor.getClazz() == Long.class) {
            obj = parseLong(stringContent, null);
        } else if (parameterDescriptor.getClazz() == long.class) {
            obj = parseLong(stringContent, 0L);
        } else if (parameterDescriptor.getClazz() == Byte.class) {
            obj = parseByte(stringContent, null);
        } else if (parameterDescriptor.getClazz() == byte.class) {
            obj = parseByte(stringContent, (byte) 0);
        } else if (parameterDescriptor.getClazz() == Short.class) {
            obj = parseShort(stringContent, null);
        } else if (parameterDescriptor.getClazz() == short.class) {
            obj = parseShort(stringContent, (short) 0);
        }
        return obj;
    }

    private Object parseShort(String stringContent, Object defaultValue) {
        try {
            return Short.parseShort(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with short type", stringContent);
            return defaultValue;
        }
    }

    private Object parseByte(String stringContent, Object defaultValue) {
        try {
            return Byte.parseByte(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with byte type", stringContent);
            return defaultValue;
        }
    }

    private Object parseLong(String stringContent, Object defaultValue) {
        try {
            return Long.parseLong(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with long type", stringContent);
            return defaultValue;
        }
    }

    private Object parseInteger(String stringContent, Object defaultValue) {
        try {
            return Integer.parseInt(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with integer type", stringContent);
            return defaultValue;
        }
    }
}
