package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.listener.MessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
@RequiredArgsConstructor
public class ListenerAnnotationProcessor implements BeanPostProcessor {
    // https://www.baeldung.com/spring-annotation-bean-pre-processor

    private final ConfigurableListableBeanFactory configurableBeanFactory;

    private final MessageListener messageListener;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        scanListenerAnnotation(bean);
        return bean;
    }

    private void scanListenerAnnotation(Object bean) {
        if (bean.getClass().isAnnotationPresent(MessagingController.class)) {
            ReflectionUtils.MethodCallback methodCallback = new ListenerMethodCallback(bean, messageListener, configurableBeanFactory);
            ReflectionUtils.doWithMethods(bean.getClass(), methodCallback);
        }
    }

}
