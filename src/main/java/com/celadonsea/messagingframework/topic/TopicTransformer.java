package com.celadonsea.messagingframework.topic;

/**
 * Contains transformation functions for topics.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public class TopicTransformer {

    /**
     * The topic after the transformation process
     */
    private String transformedTopic;

    /**
     * The original topic without any transformation
     */
    private String originalTopic;

    /**
     * The hidden constructors initializes the topic values.
     *
     * @param originalTopic the original topic
     */
    private TopicTransformer(String originalTopic) {
        this.originalTopic = originalTopic;
        transformedTopic = originalTopic;
    }

    /**
     * Creates and returns a new transformer instance.
     *
     * @param originalTopic the original topic
     * @return the new transformer instance
     */
    public static TopicTransformer transform(String originalTopic) {
        return new TopicTransformer(originalTopic);
    }

    /**
     * Makes a transformation for a topic if it's a shared topic:
     * cuts two topic parts from the start of the topic if it starts with "$share/"
     *
     * Reason: a shared subscription must be made with the $share/ prefix, but the incoming
     * topic won't contain this prefix.
     * The {@link com.celadonsea.messagingframework.listener.CallBack#messageArrived(String, byte[])}
     * function has to find the callback function for these topics and it's not possible if the callback
     * is registered with the shared topic name.
     *
     * @return the shared topic name without the $share/ prefix and the group name (second topic part)
     */
    public TopicTransformer ifShared() {
        if (transformedTopic.startsWith("$share")) {
            String topicWithGroup = transformedTopic.replaceAll("\\$share/", "");
            if (topicWithGroup.indexOf('/') >= 0) {
                transformedTopic = topicWithGroup.substring(topicWithGroup.indexOf('/') + 1);
            } else {
                transformedTopic = originalTopic;
            }
        }
        return this;
    }

    /**
     * Returns the transformed topic
     *
     * @return the transformed topic
     */
    public String andReturn() {
        return transformedTopic;
    }
}
