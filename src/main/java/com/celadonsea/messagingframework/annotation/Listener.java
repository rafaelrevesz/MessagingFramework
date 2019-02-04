package com.celadonsea.messagingframework.annotation;

import com.celadonsea.messagingframework.scanner.MessagingControllerPostProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated method is a topic listener.
 * Such methods of a {@link MessagingController @MessagingController} are considered
 * as callable function for incoming messages identified by the defined topic.
 * These methods can have parameters with
 *   {@link TopicParameter @TopicParameter} annotation
 *   {@link MessageBody @MessageBody} annotation
 *   or {@link com.celadonsea.messagingframework.message.MessageContext @MessageContext} type
 * All other parameter type will be skipped during the call.
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see MessagingController
 * @see MessagingControllerPostProcessor
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Listener {

    /**
     * The value may indicate the topic definition extending the topic definition
     * of the parent {@link MessagingController @MessagingController} class. If it's empty
     * then the topic definition of the {@link MessagingController @MessagingController}
     * will be applied to the method.
     * @return the topic definition extending the topic definition of the {@link MessagingController @MessagingController}
     */
    String value() default "";
}
