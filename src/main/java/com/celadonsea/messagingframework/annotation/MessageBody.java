package com.celadonsea.messagingframework.annotation;

import com.celadonsea.messagingframework.scanner.ListenerCallbackPostProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter of a {@link Listener @Listener} method
 * is a payload of a message. The payload coming as byte array will be parsed
 * to the type of this parameter.
 * The annotation will be processed during the auto-detection and auto-subscription
 * process.
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see Listener
 * @see ListenerCallbackPostProcessor
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageBody {
}
