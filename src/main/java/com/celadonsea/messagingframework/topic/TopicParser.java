package com.celadonsea.messagingframework.topic;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This utility class helps to handle topic subscriptions (templates) and
 * topic variables.
 * <p>
 * For example:
 * <p>
 * Topic subscription:       my/great/topic/+/{variable1}/and/{variable2}/#
 * Incoming message's topic: my/great/topic/with/funnyValue/and/42/andSoOn
 * <p>
 * Where the following topic format is valid:
 * topic level separator:    /
 * single-level wildcard:    +
 * multi-level wildcard:     #
 * <p>
 * Topic variables are: variable1  -> "funnyValue"
 * variable2  -> 42
 * last       -> "andSoOn"
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Slf4j
public class TopicParser {

    /**
     * Hidden constructor, because of static methods.
     */
    private TopicParser() {
    }

    /**
     * It generates a real topic from a template and variable map.
     * It's useful for publishing.
     * <p>
     * For example:
     * Template: my/best/topic/{var1}/{var2}/test
     * Variable map: "var1" -> "someStringValue"
     * "var2" -> 1024
     * <p>
     * Result: my/best/topic/someStringValue/1024/test
     *
     * @param template  topic template
     * @param variables variable map
     * @return the topic which is ready to publish on it
     */
    public static String generate(final String template, final Map<String, String> variables) {
        String topic = template;
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            topic = topic.replaceAll("\\{" + variable.getKey() + "\\}", variable.getValue());
        }
        return topic;
    }

    /**
     * It converts the topic template to subscription topic.
     * For example:
     * In:             my/best/topic/{var1}/{var2}/#
     * single-level wildcard: +
     * <p>
     * Return:         my/best/topic/+/+/#
     *
     * @param topic       topic template
     * @param topicFormat topic format (message broker specific)
     * @return the topic which is ready to subscribe to it
     */
    public static String changeSubscriptionFormatForMessageBrokers(final String topic, TopicFormat topicFormat) {
        return topic.replaceAll("\\{(\\w+)\\}", "" + topicFormat.getSingleLevelWildcard());
    }

    /**
     * Returns the variable-value map parsed from the template and incoming - real - topic.
     *
     * @param topic           incoming topic
     * @param subscribedTopic topic template
     * @param topicFormat     topic format (message broker dependent)
     * @return variable name - value map
     * @throws IllegalArgumentException if template topic has more items then the incoming topic or
     *                                  multi-level wildcard is not at the end of the template (it's a must))
     */
    public static Map<String, String> parseVariables(String topic,
                                                     String subscribedTopic,
                                                     TopicFormat topicFormat) {
        Map<String, String> variables = new LinkedHashMap<>();
        if (topic == null || subscribedTopic == null) {
            return variables;
        }
        String topicLevelSeparator = ("" + topicFormat.getLevelSeparator()).replaceAll("\\.", "\\\\.");

        String[] topicParts = topic.split(topicLevelSeparator);
        String[] subscribedTopicParts = subscribedTopic.split(topicLevelSeparator);

        if (subscribedTopicParts.length > topicParts.length) {
            log.error("Subscribed topic has more items then the incoming topic: {}, {}", subscribedTopic, topic);
            throw new IllegalArgumentException("Subscribed topic has more items then the incoming topic: " + subscribedTopic + ", " + topic);
        }

        for (int index = 0; index < subscribedTopicParts.length; index++) {
            if (subscribedTopicParts[index].startsWith("{") && subscribedTopicParts[index].endsWith("}")) {
                String variableName = getVariableName(subscribedTopicParts[index], variables);
                variables.put(variableName, topicParts[index]);
            }
            if (subscribedTopicParts[index].equals("" + topicFormat.getMultiLevelWildcard())) {
                if (index == subscribedTopicParts.length - 1) {
                    String variableName = getVariableName("{last}", variables);
                    variables.put(variableName, getLastVariableValue(index, topicParts));
                } else {
                    log.error("Wildcard # must be the last character in the topic: {}", subscribedTopic);
                    throw new IllegalArgumentException("Wildcard # must be the last character in the topic: " + subscribedTopic);
                }
            }
        }
        return variables;
    }

    /**
     * Returns the last all parts of the topic from the level of the last template level.
     * <p>
     * For example:
     * Template:       my/geat/topic/{v1}/{v2}/#
     * multi-level wildcard position: 5 (zero based)
     * Topic:          my/great/topic/some/value/this/is/the/last
     * <p>
     * The last variable value is: "this/is/the/last"
     *
     * @param index      the level position of the multi-level wildcard in the template (zero based)
     * @param topicParts the topic parts coming from the topic split with the topic level separator character
     * @return the multi-level wildcard defined topic parts
     */
    private static String getLastVariableValue(int index, String[] topicParts) {
        StringBuilder value = new StringBuilder();
        for (int subIndex = index; subIndex < topicParts.length; subIndex++) {
            if (subIndex > index) {
                value.append("/");
            }
            value.append(topicParts[subIndex]);
        }
        return value.toString();
    }

    /**
     * Searches end returns the first unique variable name.
     * It can be numbered if the variable name is not unique in the template.
     * <p>
     * For example:
     * <p>
     * Template: my/great/topic/{variable}/{variable}/{variable}/and/so/on
     * The variable names are: variable
     * variable#1
     * variable#2
     *
     * @param subscribedTopicPart the variable definition from the template (eg. {variable})
     * @param variables           the current variable map
     * @return the unique variable name (original or numbered)
     */
    private static String getVariableName(final String subscribedTopicPart, final Map<String, String> variables) {
        String variableName = subscribedTopicPart.substring(1, subscribedTopicPart.indexOf('}'));
        if (variables.containsKey(variableName)) {
            return getNumberedVariableName(variableName, 1, variables);
        } else {
            return variableName;
        }

    }

    /**
     * It searches recursively the first unique variable name.
     *
     * @param baseName  original variable name
     * @param number    the initial number to check
     * @param variables the current variable map
     * @return the unique variable name
     */
    private static String getNumberedVariableName(final String baseName, final int number, final Map<String, String> variables) {
        String postponedVariableName = baseName + "#" + number;
        if (variables.containsKey(postponedVariableName)) {
            return getNumberedVariableName(baseName, number + 1, variables);
        } else {
            return postponedVariableName;
        }
    }
}
