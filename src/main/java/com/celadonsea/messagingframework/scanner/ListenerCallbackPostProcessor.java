package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessageBody;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.annotation.TopicParameter;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.core.ConsumingProperties;
import com.celadonsea.messagingframework.message.MessageContext;
import com.celadonsea.messagingframework.topic.TopicFormat;
import com.celadonsea.messagingframework.topic.TopicParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A method post processor for {@link Listener @Listener} annotated methods to
 * prepare subscription and call back of arriving messages.
 * <p>
 * Steps:
 * 1., Parameter analysis
 * 2., Topic preparation
 * 3., Call back method preparation as lambda function
 * 4., Topic subscription with the prepared topic and lambda function
 *
 * @author Rafael Revesz
 * @see Listener
 * @see com.celadonsea.messagingframework.annotation.MessagingController
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ListenerCallbackPostProcessor {

    /**
     * Object mapper is for deserialize JSON string to a given message body class definition
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The entry point of the post processing provides the automatic topic subscription
     * with the parameters coming from the given annotations.
     * The topic will be merged from the topic parts coming fom
     * {@link com.celadonsea.messagingframework.annotation.MessagingController @MessagingController}
     * and {@link Listener @Listener} annotations.
     * A topic transformation will be applied too if the client contains a transformation function.
     * It's necessary for example for shared subscriptions:
     * $share/clientGroup/normalTopicParts/...  -> normalTopicParts/...
     *
     * @param messagingController  the new {@link com.celadonsea.messagingframework.annotation.MessagingController @MessagingController} bean
     * @param client               the required message client
     * @param method               the {@link Listener @Listener} annotated method
     * @param methodAnnotation     the annotation containing the defined subtopic
     * @param controllerAnnotation the annotation containing the defined base topic and the optional exchange value
     */
    public void processListenerMethod(Object messagingController,
                                      MessageClient client,
                                      Method method,
                                      Listener methodAnnotation,
                                      MessagingController controllerAnnotation) {

        TopicFormat topicFormat = client.getTopicFormat();
        String exchange = controllerAnnotation.exchange();

        List<ParameterDescriptor> parameters = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            parameters.add(analyzeParameterType(parameter));
        }

        String topicDefinition = prepareTopic(topicFormat, controllerAnnotation.topic(), methodAnnotation.value());

        String transformedTopicDefinition = getTransformedTopicDefinition(topicDefinition, client.topicTransformer());

        String subscribedTopic = TopicParser.changeSubscriptionFormatForMessageBrokers(topicDefinition, topicFormat);

        ConsumingProperties consumingProperties;
        if (!"".equals(exchange.trim())) {
            consumingProperties = new ConsumingProperties(subscribedTopic, exchange);
        } else {
            consumingProperties = new ConsumingProperties(subscribedTopic);
        }

        client.subscribe(consumingProperties, (topic, message) -> subscriptionLambda(messagingController, method, parameters, transformedTopicDefinition, topic, topicFormat, message));
    }

    /**
     * Transforms the topic definition if a transformation function exists.
     *
     * @param topicDefinition the original topic definition
     * @param transformationFunction the transformation function
     * @return the original or the transformed topic definition
     */
    private String getTransformedTopicDefinition(String topicDefinition, Function<String, String> transformationFunction) {
        String transformedTopicDefinition;
        if (transformationFunction != null) {
            transformedTopicDefinition = transformationFunction.apply(topicDefinition);
        } else {
            transformedTopicDefinition = topicDefinition;
        }
        return transformedTopicDefinition;
    }

    /**
     * Prepares the topic definition from the base and the sub topic. These topic parts will be
     * concatenated with the insert of the topic level separator, if necessary.
     * The following parts will be concatenated:
     *  - base topic part if exists
     *  - topic level separator if base and sub topics are defined and no level separator present
     *    at the end of the base part and at the start of the sub part
     *  - sub topic part if exists
     *
     * @param topicFormat topic format definition
     * @param baseTopic base topic definition
     * @param subTopic sub topic definition
     * @return prepared topic definition
     * @throws IllegalArgumentException if either the base nor the sub topic part are present
     */
    private String prepareTopic(TopicFormat topicFormat, String baseTopic, String subTopic) {
        String topicDefinition = null;
        if (baseTopic != null && !"".equals(baseTopic.trim())) {
            topicDefinition = baseTopic;
        }
        if (subTopic != null && !"".equals(subTopic.trim())) {
            if (topicDefinition == null || "".equals(topicDefinition.trim())) {
                topicDefinition = subTopic;
            } else {
                if (!topicDefinition.endsWith("" + topicFormat.getLevelSeparator()) &&
                    !subTopic.startsWith("" + topicFormat.getLevelSeparator())) {
                    topicDefinition += topicFormat.getLevelSeparator();
                }
                topicDefinition += subTopic;
            }
        }
        Assert.notNull(topicDefinition, "No defined topic found");
        return topicDefinition;
    }

    /**
     * This method provides a pre analysis for method parameters. Three type of parameters
     * are important for the processing:
     * <p>
     * topic parameters are annotated with {@link TopicParameter @TopicParameter}
     * message body parameter is annotated with {@link MessageBody @MessageBody}
     * message context has the type {@link MessageContext @MessageContext}
     * <p>
     * All other parameter will be set with null.
     *
     * @param parameter a method parameter to analyse
     * @return a parameter description containing the role of the parameter in case of a call back
     */
    private ParameterDescriptor analyzeParameterType(Parameter parameter) {
        ParameterDescriptor parameterDescriptor = null;
        if (parameter.getAnnotations() != null && parameter.getAnnotations().length > 0) {
            if (parameter.isAnnotationPresent(TopicParameter.class)) {
                String parameterName = parameter.getAnnotation(TopicParameter.class).value();
                return new ParameterDescriptor(parameterName, parameter.getType());
            }
            if (parameter.isAnnotationPresent(MessageBody.class)) {
                // message body
                if (hasSupportedSimpleType(parameter.getType())) {
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

    /**
     * This method provides the information if the class has a supported simple type.
     *
     * @param clazz the class of the method parameter
     * @return true if the class is one of the supported simple type, otherwise false
     */
    private boolean hasSupportedSimpleType(Class clazz) {
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

    /**
     * This method will be used as a lambda function calling at message arrive.
     * It will prepare all the parameters and call the given method with them.
     *
     * @param messagingController  the messaging controller bean
     * @param method               the method to call
     * @param parameterDescriptors list of parameter descriptions
     * @param subscribedTopic      topic definition
     * @param topic                incoming topic
     * @param topicFormat          topic format description (coming from message client)
     * @param content              message body as raw format
     */
    private void subscriptionLambda(Object messagingController,
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
                topicFormat);
            int parameterCount = 0;
            MessageContext messageContext = new MessageContext(topic, subscribedTopic, parsedVariables);
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
            method.invoke(messagingController, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot call listener method for topic {}", subscribedTopic, e);
        }
    }

    /**
     * The method provides the converted value of a topic parameter.
     * Topic parameter annotation refers to topic variables defined in the topic definition
     * (eg. /basetopic/subtopic/{topicvariable}).
     * The topicVariables contains all the parsed variables from the topic definition.
     * The method will return a value if the parameter has a supported simple type and
     * the requested parameter ID is present in the topicVariables map.
     * Otherwise it returns null.
     *
     * @param topicVariables      parsed topic variable IDs and values
     * @param parameterDescriptor the description of the topic parameter type
     * @return the converted topic parameter
     */
    private Object parseTopicParameter(Map<String, String> topicVariables, ParameterDescriptor parameterDescriptor) {
        Object obj = null;
        String parameterValue = topicVariables.get(parameterDescriptor.getName());
        if (hasSupportedSimpleType(parameterDescriptor.getClazz())) {
            obj = parseSupportedSimpleTypes(parameterDescriptor, parameterValue.getBytes());
        }
        return obj;
    }

    /**
     * The method provides the converted value of the message body as raw format.
     * If the message body parameter has one of the supported simple type than it will
     * be converted with the corresponding method. Otherwise the object mapper tries to
     * deserialize the raw format to the requested type.
     *
     * @param content             message content as raw format
     * @param parameterDescriptor the description of the message body type
     * @return the converted message body
     */
    private Object parseBody(byte[] content, ParameterDescriptor parameterDescriptor) {
        Object obj = null;
        if (hasSupportedSimpleType(parameterDescriptor.getClazz())) {
            obj = parseSupportedSimpleTypes(parameterDescriptor, content);
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

    /**
     * This method provides a converting function from byte array to a defined
     * data type. The destination type is coming in the parameter descriptor.
     * It won't be any conversion if the requested type is byte array, any other
     * cases the payload will be converted first to string.
     * IMPORTANT: 1 as byte won't be parsed from the byte array {1} but from {49}
     * Steps:
     * byte array {49} -> string "1"
     * string "1" -> byte 1
     * <p>
     * It's the same for all numeric types.
     *
     * @param parameterDescriptor the parameter description coming from the pre analysis
     * @param content             the message payload
     * @return the converted value
     */
    private Object parseSupportedSimpleTypes(ParameterDescriptor parameterDescriptor, byte[] content) {
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

    /**
     * The method converts the string type parameter to short. If it cannot be
     * converted then the default value will be returned.
     *
     * @param stringContent the value to convert
     * @param defaultValue  the default value
     * @return the converted short or the default value
     */
    private Object parseShort(String stringContent, Object defaultValue) {
        try {
            return Short.parseShort(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with short type", stringContent);
            return defaultValue;
        }
    }

    /**
     * The method converts the string type parameter to byte. If it cannot be
     * converted then the default value will be returned.
     *
     * @param stringContent the value to convert
     * @param defaultValue  the default value
     * @return the converted byte or the default value
     */
    private Object parseByte(String stringContent, Object defaultValue) {
        try {
            return Byte.parseByte(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with byte type", stringContent);
            return defaultValue;
        }
    }

    /**
     * The method converts the string type parameter to long. If it cannot be
     * converted then the default value will be returned.
     *
     * @param stringContent the value to convert
     * @param defaultValue  the default value
     * @return the converted long or the default value
     */
    private Object parseLong(String stringContent, Object defaultValue) {
        try {
            return Long.parseLong(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with long type", stringContent);
            return defaultValue;
        }
    }

    /**
     * The method converts the string type parameter to integer. If it cannot be
     * converted then the default value will be returned.
     *
     * @param stringContent the value to convert
     * @param defaultValue  the default value
     * @return the converted integer or the default value
     */
    private Object parseInteger(String stringContent, Object defaultValue) {
        try {
            return Integer.parseInt(stringContent);
        } catch (NumberFormatException e) {
            log.error("Cannot parse {} for body with integer type", stringContent);
            return defaultValue;
        }
    }
}
