package com.celadonsea.messagingframework.config;

import com.celadonsea.messagingframework.scanner.MessageCallbackPreProcessor;
import com.celadonsea.messagingframework.scanner.ListenerAnnotationProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingAutoConfiguration {

    @Bean
    public MessageCallbackPreProcessor messageListener() {
        return new MessageCallbackPreProcessor();
    }

    @Bean
    public ListenerAnnotationProcessor listenerAnnotationProcessor(ConfigurableListableBeanFactory configurableBeanFactory,
                                                                   MessageCallbackPreProcessor messageCallbackPreProcessor) {
        return new ListenerAnnotationProcessor(configurableBeanFactory, messageCallbackPreProcessor);
    }
}
