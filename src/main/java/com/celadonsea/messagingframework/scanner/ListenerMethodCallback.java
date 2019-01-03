package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.listener.MessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class ListenerMethodCallback implements ReflectionUtils.MethodCallback {

    private final Object bean;

    private final MessageListener messageListener;

    private final ConfigurableListableBeanFactory configurableBeanFactory;

    @Override
    public void doWith(Method method) {
        if (!method.isAnnotationPresent(Listener.class) || !bean.getClass().isAnnotationPresent(MessagingController.class)) {
            return;
        }
        MessagingController annotation = bean.getClass().getAnnotation(MessagingController.class);
        MessageClient client = (MessageClient)configurableBeanFactory.getBean(annotation.client());
        messageListener.processListener(bean, client, method, method.getAnnotation(Listener.class), annotation.topic());
    }
}
