package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.message.MessageContext;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
public class ParameterDescriptor {

    public static final String PARAMETER_NAME_BODY = "PARAMETER_NAME_BODY";

    public static final String PARAMETER_NAME_CONTEXT = "PARAMETER_NAME_CONTEXT";

    public static final String PARAMETER_NAME_EMPTY = "PARAMETER_NAME_EMPTY";

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

    public static ParameterDescriptor empty() {
        return new ParameterDescriptor(PARAMETER_NAME_EMPTY, null);
    }

    public static ParameterDescriptor context() {
        return new ParameterDescriptor(PARAMETER_NAME_CONTEXT, MessageContext.class);
    }

    public boolean isEmpty() {
        return name == null || name.equals(PARAMETER_NAME_EMPTY);
    }

    public boolean isContext() {
        return name != null && name.equals(PARAMETER_NAME_CONTEXT);
    }

    public boolean isBody() {
        return name != null && name.equals(PARAMETER_NAME_BODY);
    }
}
