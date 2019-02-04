package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.client.MessageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * A callback class for scanning all {@link Listener @Listener} annotated
 * methods of the {@link MessagingController @MessagingController} beans.
 * The callback will be triggered by the spring auto-detection through the
 * {@link MessagingControllerPostProcessor @MessagingControllerPostProcessor}.
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see Listener
 * @see MessagingController
 * @see ListenerCallbackPostProcessor
 * @see MessagingControllerPostProcessor
 */
@RequiredArgsConstructor
public class ListenerMethodCallback implements ReflectionUtils.MethodCallback {

    /**
     * The new bean instance created by the spring auto-detection mechanism
     */
    private final Object bean;

    /**
     * Post processor for process {@link com.celadonsea.messagingframework.annotation.Listener @Listener}
     * annotated methods.
     */
    private final ListenerCallbackPostProcessor listenerCallbackPostProcessor;

    /**
     * Bean factory for {@link ListenerMethodCallback @ListenerMethodCallback} to
     * access message {@link com.celadonsea.messagingframework.client.MessageClient @MessageClient}
     * from the spring context.
     */
    private final ConfigurableListableBeanFactory configurableBeanFactory;

    /**
     * Filters the incoming methods with {@link Listener @Listener} annotation and
     * the containing class with {@link MessagingController @MessagingController} annotation.
     *
     * The listener process will be applied for the filtered methods. The required
     * {@link MessageClient @MessageClient} will be return from the spring context
     * by the {@link ConfigurableListableBeanFactory @ConfigurableListableBeanFactory}.
     *
     * @param method method from a {@link MessagingController @MessagingController} annotated bean
     */
    @Override
    public void doWith(Method method) {
        if (!method.isAnnotationPresent(Listener.class) || !bean.getClass().isAnnotationPresent(MessagingController.class)) {
            return;
        }
        MessagingController controllerAnnotation = bean.getClass().getAnnotation(MessagingController.class);
        MessageClient client = (MessageClient)configurableBeanFactory.getBean(controllerAnnotation.client());
        listenerCallbackPostProcessor.processListenerMethod(
            bean,
            client,
            method,
            method.getAnnotation(Listener.class),
            controllerAnnotation);
    }
}
