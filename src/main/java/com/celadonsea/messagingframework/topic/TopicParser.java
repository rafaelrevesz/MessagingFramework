package com.celadonsea.messagingframework.topic;

import java.util.LinkedHashMap;
import java.util.Map;

public class TopicParser {

	private TopicParser() {}

	public static String generate(final String template, final Map<String, String> variables) {
		String topic = template;
		for (Map.Entry<String, String> variable: variables.entrySet()) {
			topic = topic.replaceAll("\\{" + variable.getKey() + "\\}", variable.getValue());
		}
		return topic;
	}

	public static String changeSubscriptionFormatForMessageBrokers(final String topic, String topicWildcard) {
		return topic.replaceAll("\\{(\\w+)\\}", topicWildcard);
	}

	public static Map<String, String> parseVariables(String topic,
                                                     String subscribedTopic,
                                                     String topicLevelSeparator,
                                                     String topicJoker) {
		Map<String, String> variables = new LinkedHashMap<>();
		if (topic == null || subscribedTopic == null) {
			return variables;
		}
		String[] topicParts = topic.split(topicLevelSeparator.replaceAll("\\.", "\\\\."));
		String[] subscribedTopicParts = subscribedTopic.split(topicLevelSeparator.replaceAll("\\.", "\\\\."));

		if (subscribedTopicParts.length > topicParts.length) {
			throw new IllegalArgumentException("Subscribed topic has more items then the incoming topic: " +  subscribedTopic + topic);
		}

		for (int index = 0; index < subscribedTopicParts.length; index++) {
			if (subscribedTopicParts[index].startsWith("{") && subscribedTopicParts[index].endsWith("}")) {
				String variableName = getVariableName(subscribedTopicParts[index], variables);
				variables.put(variableName, topicParts[index]);
			}
			if (subscribedTopicParts[index].equals(topicJoker)) {
				if (index == subscribedTopicParts.length - 1) {
					String variableName = getVariableName("{last}", variables);
					variables.put(variableName, getLastVariableValue(index, topicParts));
				} else {
					throw new IllegalArgumentException("Wildcard # must be the last character in the topic: " + subscribedTopic);
				}
			}
		}
		return variables;
	}

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

	private static String getVariableName(final String subscribedTopicPart, final Map<String, String> variables) {
		String variableName = subscribedTopicPart.substring(1, subscribedTopicPart.indexOf('}'));
		if (variables.containsKey(variableName)) {
			return getNumberedVariableName(variableName, 1, variables);
		} else {
			return variableName;
		}

	}

	private static String getNumberedVariableName(final String baseName, final int number, final Map<String, String> variables) {
		String postponedVariableName = baseName + "#" + number;
		if (variables.containsKey(postponedVariableName)) {
			return getNumberedVariableName(baseName, number + 1, variables);
		} else {
			return postponedVariableName;
		}
	}
}
