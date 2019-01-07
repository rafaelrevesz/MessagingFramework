package com.celadonsea.messagingframework.topic;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TopicParserTest {

    @Test
    public void shouldParseTopicVariables() {
        Map<String, String> variables = TopicParser.parseVariables(
            "topic/variable/subtopic/subvariable/second/third/end/realend",
            "topic/{var1}/subtopic/{var2}/{var2}/{var2}/#",
            "/",
            "#");
        Assert.assertEquals("variable", variables.get("var1"));
        Assert.assertEquals("subvariable", variables.get("var2"));
        Assert.assertEquals("second", variables.get("var2#1"));
        Assert.assertEquals("third", variables.get("var2#2"));
        Assert.assertEquals("end/realend", variables.get("last"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotParseInvalidTopics() {
        TopicParser.parseVariables(
            "topic/variable/subtopic/subvariable/end",
            "topic/{var1}/subtopic/illegaltopicpart/{var2}/{last}",
            "/",
            "#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotParseTopicWithWrongJokerPosition() {
        TopicParser.parseVariables(
            "topic/variable/subtopic/subvariable/end",
            "topic/{var1}/subtopic/#/{last}",
            "/",
            "#");
    }

    @Test
    public void shouldReturnNoVariablesIfTopicsAreEmpty() {
        Map<String, String> variables = TopicParser.parseVariables(
            null,
            "topic/{var1}/subtopic/{var2}/{last}",
            "/",
            "#");
        Assert.assertEquals(0, variables.size());

        variables = TopicParser.parseVariables(
            "topic/variable/subtopic/subvariable/end",
            null,
            "/",
            "#");
        Assert.assertEquals(0, variables.size());
    }
}
