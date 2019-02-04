package com.celadonsea.messagingframework.annotation;

import com.celadonsea.messagingframework.scanner.ListenerCallbackPostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter of a {@link Listener @Listener} method
 * is a topic parameter of a message. The topic of the incoming message will be parsed
 * with the topic definition, and all the defined variables are available as topic parameters.
 * Eg.:
 *   topic definition: basetopic/{subtopic}/someextension/{subextension}/{numericparameter}
 *   incoming topic:   basetopic/funnysubtopic/someextension/greatextension/42
 *
 *   Following parameters and values are available:
 *     subtopic -> funnysubtopic (as String)
 *     subextension -> greatextension (as String)
 *     numericparameter -> 42 (as String or any numeric types like byte, short, etc.)
 *
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
public @interface TopicParameter {

    /**
     * Indicates the name of the topic variable defined in the {@link Listener @Listener} or
     * {@link MessagingController @MessagingController} annotation with curly brackets.
     * @return the name of the topic variable
     */
    String value();
}
