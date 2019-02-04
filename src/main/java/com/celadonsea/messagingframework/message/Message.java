package com.celadonsea.messagingframework.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Generic message container with timestamp.
 *
 * @param <E> the type of the value property
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message<E> {

	/**
	 * The timestamp when the value is valid
	 */
	private long timestamp;

	/**
	 * The value of the message
	 */
	private E value;
}
