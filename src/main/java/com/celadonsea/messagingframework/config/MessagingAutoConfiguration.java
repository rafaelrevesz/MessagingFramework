package com.celadonsea.messagingframework.config;

import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.listener.MessageListener;
import com.celadonsea.messagingframework.scanner.ListenerAnnotationProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class MessagingAutoConfiguration {

    @Bean
    public MessageListener messageListener() {
        return new MessageListener();
    }

    @Bean
    @ConditionalOnClass(MessagingController.class)
    public ListenerAnnotationProcessor listenerAnnotationProcessor(ConfigurableListableBeanFactory configurableBeanFactory,
                                                                   MessageListener messageListener) {
        return new ListenerAnnotationProcessor(configurableBeanFactory, messageListener);
    }
}
