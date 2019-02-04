package com.celadonsea.messagingframework.annotation;

import com.celadonsea.messagingframework.scanner.ListenerCallbackPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a message controller.
 * Such classes are considered as candidates for auto-detection and auto-subscription
 * when using annotation-based configuration and classpath scanning.
 * All {@link Listener @Listener} annotated methods of these classes will be used
 * for topic listening.
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see Component
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 * @see ListenerCallbackPostProcessor
 */
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessagingController {

    /**
     * The value may indicate the default topic name for the subscription process.
     * It can be extended by the topic definition of the {@link Listener @Listener} methods,
     * or it can be empty if the whole topic definition is given by the {@link Listener @Listener} method.
     * @return the default topic name for the subscription process
     */
    String topic() default "";

    /**
     * The value may indicate the exchange value for the topic subscription in case
     * of AMQP message protocol.
     * @return the exchange value
     */
    String exchange() default "";

    /**
     * The value should indicate the name of the message client bean in the spring context.
     * @return
     */
    String client();
}
