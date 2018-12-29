package com.celadonsea.messagingframework.scanner;

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
        ReflectionUtils.MethodCallback methodCallback = new ListenerMethodCallback(bean, configurableBeanFactory);
        ReflectionUtils.doWithMethods(bean.getClass(), methodCallback);
    }

}
