package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.MessagingController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

/**
 * A bean post processor for {@link MessagingController @MessagingController} that will be
 * triggered by the spring auto-detection. It starts processing of the
 * {@link com.celadonsea.messagingframework.annotation.Listener @Listener} annotated methods
 * after the initialization of the bean.
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see MessagingController
 * @see com.celadonsea.messagingframework.annotation.Listener
 * @see ListenerMethodCallback
 */
@Slf4j
@RequiredArgsConstructor
public class MessagingControllerPostProcessor implements BeanPostProcessor {

    /**
     * Bean factory for {@link ListenerMethodCallback @ListenerMethodCallback} to
     * access message {@link com.celadonsea.messagingframework.client.MessageClient @MessageClient}
     * from the spring context.
     */
    private final ConfigurableListableBeanFactory configurableBeanFactory;

    /**
     * Post processor for process {@link com.celadonsea.messagingframework.annotation.Listener @Listener}
     * annotated methods.
     */
    private final ListenerCallbackPostProcessor listenerCallbackPostProcessor;

    /**
     * Returns the new bean instance without any modification.
     * @param bean the new bean instance
     * @param beanName name of the new bean instance
     * @return the unmodified bean instance
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Apply the method scan processing only for the {@link MessagingController @MessagingController}
     * beans. For these instances a {@link ListenerMethodCallback @ListenerMethodCallback} will
     * be triggered for scanning the {@link com.celadonsea.messagingframework.annotation.Listener @Listener}
     * annotated methods.
     *
     * @param bean the new bean instance
     * @param beanName the name of the new bean instance
     * @return the unmodified bean instance
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        scanListenerAnnotation(bean);
        return bean;
    }

    /**
     * Filters the {@link MessagingController @MessagingController} annotated beans and
     * start the scan of the {@link com.celadonsea.messagingframework.annotation.Listener @Listener}
     * annotated methods.
     *
     * @param bean the new bean instance
     */
    private void scanListenerAnnotation(Object bean) {
        if (bean.getClass().isAnnotationPresent(MessagingController.class)) {
            log.debug("Load message controller {}", bean.getClass().getName());
            ReflectionUtils.MethodCallback methodCallback = new ListenerMethodCallback(bean, listenerCallbackPostProcessor, configurableBeanFactory);
            ReflectionUtils.doWithMethods(bean.getClass(), methodCallback);
        }
    }

}
