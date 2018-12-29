package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessagingController;

import java.lang.reflect.Method;

public class ControllerScanner {

    public void scan() {

    }

    private void processController(Class controllerClass) {
        MessagingController annotation = (MessagingController)controllerClass.getAnnotation(MessagingController.class);
        String baseTopic = annotation.topic();

        for (Method method : controllerClass.getMethods()) {
            Listener declaredAnnotation = method.getDeclaredAnnotation(Listener.class);
            if (declaredAnnotation != null) {

            }
        }
    }
}
