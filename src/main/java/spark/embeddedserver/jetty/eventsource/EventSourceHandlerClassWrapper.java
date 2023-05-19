package spark.embeddedserver.jetty.eventsource;

import java.lang.reflect.InvocationTargetException;

import static java.util.Objects.requireNonNull;

public class EventSourceHandlerClassWrapper implements EventSourceHandlerWrapper {
    private final Class<?> handlerClass;
    public EventSourceHandlerClassWrapper(Class<?> handlerClass) {
        requireNonNull(handlerClass, "EventSource handler class cannot be null");
        EventSourceHandlerWrapper.validateHandlerClass(handlerClass);
        this.handlerClass = handlerClass;
    }
    @Override
    public Object getHandler() {
        try {
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new RuntimeException("Could not instantiate event source handler", ex);
        }
    }
}
