package com.celadonsea.messagingframework.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Describes the topic format of the corresponding message broker.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class TopicFormat {

    /**
     * The character which separates the topic levels
     */
    private char levelSeparator;

    /**
     * The single-level wildcard represents a text between two topic level separator.
     * For example: any/topic/+/end
     *
     *   where topic level separator is "/" and single-level wildcard is "+"
     */
    private char singleLevelWildcard;

    /**
     * Multi-level wildcard character can be stay at the end of a subscription. It matches to
     * any string.
     *
     * For example: any/topic/+/#
     *
     *   where topic level separator is "/", single-level wild card is "+" and multi-level wildcard is "#"
     */
    private char multiLevelWildcard;

}
