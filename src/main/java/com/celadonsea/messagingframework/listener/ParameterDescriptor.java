package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.message.MessageContext;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
public class ParameterDescriptor {

    public static final String BODY = "PARAMETER_NAME_BODY";

    public static final String CONTEXT = "PARAMETER_NAME_CONTEXT";

    public static final String EMPTY = "PARAMETER_NAME_EMPTY";

    private String name;

    private Class clazz;

    private Type type;

    public ParameterDescriptor(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public ParameterDescriptor(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public static ParameterDescriptor EMPTY() {
        return new ParameterDescriptor(EMPTY, null);
    }

    public static ParameterDescriptor CONTEXT() {
        return new ParameterDescriptor(CONTEXT, MessageContext.class);
    }

    public boolean isEmpty() {
        return name == null || name.equals(EMPTY);
    }

    public boolean isContext() {
        return name != null && name.equals(CONTEXT);
    }

    public boolean isBody() {
        return name != null && name.equals(BODY);
    }
}
