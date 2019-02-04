package com.celadonsea.messagingframework.topic;

import org.junit.Assert;
import org.junit.Test;

public class TopicTransformerTest {

    @Test
    public void shouldReturnOriginalTopicIfNoTransformationCalled() {
        String topic = "my/main/sub/topic";
        String transformedTopic = TopicTransformer.transform(topic).andReturn();
        Assert.assertEquals(topic, transformedTopic);
    }

    @Test
    public void shouldReturnNullIfTopicIsNull() {
        String topic = null;
        String transformedTopic = TopicTransformer.transform(topic).andReturn();
        Assert.assertEquals(topic, transformedTopic);
    }

    @Test
    public void shouldReturnOriginalTopicIfNoSharedInfoPresent() {
        String topic = "my/main/sub/topic";
        String transformedTopic = TopicTransformer.transform(topic).ifShared().andReturn();
        Assert.assertEquals(topic, transformedTopic);
    }

    @Test
    public void shouldReturnTransformedTopicIfSharedInfoPresent() {
        String topic = "$share/mygroup/my/main/sub/topic";
        String transformedTopic = TopicTransformer.transform(topic).ifShared().andReturn();
        Assert.assertEquals("my/main/sub/topic", transformedTopic);
    }

    @Test
    public void shouldReturnEmptyTopicIfOriginalHasOnlyGroupWithSlash() {
        String topic = "$share/mygroup/";
        String transformedTopic = TopicTransformer.transform(topic).ifShared().andReturn();
        Assert.assertEquals("", transformedTopic);
    }

    @Test
    public void shouldReturnOriginalTopicIfOriginalHasOnlyGroupWithoutSlash() {
        String topic = "$share/mygroup";
        String transformedTopic = TopicTransformer.transform(topic).ifShared().andReturn();
        Assert.assertEquals(topic, transformedTopic);
    }

    @Test
    public void shouldOriginalIfSharedGroupIsNotWellDefined() {
        String topic = "$share";
        String transformedTopic = TopicTransformer.transform(topic).ifShared().andReturn();
        Assert.assertEquals(topic, transformedTopic);
    }
}
