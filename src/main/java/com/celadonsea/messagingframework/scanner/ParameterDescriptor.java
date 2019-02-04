package com.celadonsea.messagingframework.scanner;

import com.celadonsea.messagingframework.message.MessageContext;
import lombok.Getter;

import java.lang.reflect.Type;

/**
 * Describes parameter for methods annotated with {@link com.celadonsea.messagingframework.annotation.Listener @Listener}.
 *
 * @author Rafael Revesz
 * @since 1.0
 * @see com.celadonsea.messagingframework.annotation.Listener
 */
@Getter
public class ParameterDescriptor {

    /**
     * Parameter name for message body, because only topic parameters have names
     */
    public static final String PARAMETER_NAME_BODY = "PARAMETER_NAME_BODY";

    /**
     * Parameter name for message context
     */
    public static final String PARAMETER_NAME_CONTEXT = "PARAMETER_NAME_CONTEXT";

    /**
     * Parameter name for unknown and unused parameters, the will be called with null
     */
    public static final String PARAMETER_NAME_EMPTY = "PARAMETER_NAME_EMPTY";

    /**
     * It describes the parameter name, which is defined in {@link com.celadonsea.messagingframework.annotation.TopicParameter @TopicParameter}
     * annotation.
     */
    private String name;

    /**
     * It describes the parameter type, if it's a simple type
     */
    private Class clazz;

    /**
     * It describes the parameter type, if it's a generic type (e.g List<T>)
     */
    private Type type;

    /**
     * Constructor for simple typed parameters.
     *
     * @param name Name of the parameter
     * @param clazz type of the parameter
     */
    public ParameterDescriptor(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    /**
     * Constructor for parameters with generic types
     *
     * @param name Name of the parameter
     * @param type Type of the parameter
     */
    public ParameterDescriptor(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Simple factory method for empty parameters
     *
     * @return Parameter description with a name that indicates the empty parameter
     */
    public static ParameterDescriptor empty() {
        return new ParameterDescriptor(PARAMETER_NAME_EMPTY, null);
    }

    /**
     * Simple factory method for message context
     *
     * @return Parameter description with a name that indicates the message context
     */
    public static ParameterDescriptor context() {
        return new ParameterDescriptor(PARAMETER_NAME_CONTEXT, MessageContext.class);
    }

    /**
     * Returns {@code true} if, and only if, the described parameter is an empty one.
     *
     * @return {@code true} if it's a description of an empty parameter, otherwise
     * {@code false}
     */
    public boolean isEmpty() {
        return name == null || name.equals(PARAMETER_NAME_EMPTY);
    }

    /**
     * Returns {@code true} if, and only if, the described parameter is a message context.
     *
     * @return {@code true} if it's a description of a message context parameter, otherwise
     * {@code false}
     */
    public boolean isContext() {
        return name != null && name.equals(PARAMETER_NAME_CONTEXT);
    }

    /**
     * Returns {@code true} if, and only if, the described parameter is a message body.
     *
     * @return {@code true} if it's a description of a message body parameter, otherwise
     * {@code false}
     */
    public boolean isBody() {
        return name != null && name.equals(PARAMETER_NAME_BODY);
    }
}
