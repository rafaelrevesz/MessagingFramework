package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessageBody;
import com.celadonsea.messagingframework.annotation.TopicParameter;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.message.MessageContext;
import com.celadonsea.messagingframework.topic.TopicParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {

    private ObjectMapper objectMapper = new ObjectMapper();

    public void processListener(Object handler, MessageClient client, Method method, Listener methodAnnotation, String baseTopic) {
        String subTopic = methodAnnotation.value();
        List<ParameterDescriptor> parameters = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.getAnnotations() != null && parameter.getAnnotations().length > 0) {
                for (Annotation parameterAnnotation : parameter.getAnnotations()) {
                    if (parameterAnnotation.annotationType() == TopicParameter.class) {
                        String parameterName = ((TopicParameter)parameterAnnotation).value();
                        parameters.add(new ParameterDescriptor(parameterName, parameter.getType()));
                        break;
                    } else if (parameterAnnotation.annotationType() == MessageBody.class) {
                        // message body
                        //https://stackoverflow.com/questions/6846244/jackson-and-generic-type-reference
                        if (parameter.getType() == String.class ||
                            parameter.getType() == byte[].class ||
                            parameter.getType() == Integer.class ||
                            parameter.getType() == int.class ||
                            parameter.getType() == Long.class ||
                            parameter.getType() == long.class ||
                            parameter.getType() == Short.class ||
                            parameter.getType() == short.class ||
                            parameter.getType() == Byte.class ||
                            parameter.getType() == byte.class) {
                            parameters.add(new ParameterDescriptor(ParameterDescriptor.PARAMETER_NAME_BODY, parameter.getType()));
                        } else {
                            parameters.add(new ParameterDescriptor(ParameterDescriptor.PARAMETER_NAME_BODY, parameter.getParameterizedType()));
                        }
                        break;
                    }
                }
            } else if (parameter.getType() == MessageContext.class) {
                // parameter without annotation can only be the context
                parameters.add(ParameterDescriptor.context());
            } else {
                // otherwise the parameter is null
                parameters.add(ParameterDescriptor.empty());
            }
        }
        String topicDefinition = baseTopic + "/" + subTopic;
        String subscribedTopic = TopicParser.changeSubscriptionFormatForMessageBrokers(topicDefinition, "+");
        client.subscribe(subscribedTopic, (topic, message) -> subscriptionLambda(handler, method, parameters, topicDefinition, topic, message));
    }

    private void subscriptionLambda(Object handler,
                                    Method method,
                                    List<ParameterDescriptor> parameterDescriptors,
                                    String subscribedTopic,
                                    String topic,
                                    byte[] content) {
        try {
            Object[] parameters = new Object[parameterDescriptors.size()];
            Map<String, String> parsedVariables = TopicParser.parseVariables(
                topic,
                subscribedTopic,
                "/",
                "#");
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
                    if (parameterDescriptor.getClazz() == byte[].class) {
                        parameters[parameterCount] = content;
                    } else if (parameterDescriptor.getClazz() == String.class) {
                        parameters[parameterCount] = new String(content);
                    } else if (parameterDescriptor.getClazz() == Integer.class || parameterDescriptor.getClazz() == int.class) {
                        parameters[parameterCount] = Integer.parseInt(new String(content));
                    } else if (parameterDescriptor.getClazz() == Long.class || parameterDescriptor.getClazz() == long.class) {
                        parameters[parameterCount] = Long.parseLong(new String(content));
                    } else if (parameterDescriptor.getClazz() == Byte.class || parameterDescriptor.getClazz() == byte.class) {
                        parameters[parameterCount] = Byte.parseByte(new String(content));
                    } else if (parameterDescriptor.getClazz() == Short.class || parameterDescriptor.getClazz() == short.class) {
                        parameters[parameterCount] = Short.parseShort(new String(content));
                    } else {
                        try {
                            JavaType javaType = objectMapper.getTypeFactory().constructType(parameterDescriptor.getType());
                            parameters[parameterCount] = objectMapper.readValue(content, javaType);
                        } catch (IOException e) {
                            log.error("Cannot read message", e);
                        }
                    }
                } else {
                    String parameterValue = parsedVariables.get(parameterDescriptor.getName());
                    if (parameterDescriptor.getClazz() == String.class) {
                        parameters[parameterCount] = parameterValue;
                    } else if (parameterDescriptor.getClazz() == Integer.class || parameterDescriptor.getClazz() == int.class) {
                        parameters[parameterCount] = Integer.parseInt(parameterValue);
                    }
                }

                parameterCount++;
            }
            // https://stackoverflow.com/questions/16207283/how-to-pass-multiple-parameters-to-a-method-in-java-reflections/16254447
            method.invoke(handler, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot call listener method for topic {}", subscribedTopic, e);
        }
    }
}
