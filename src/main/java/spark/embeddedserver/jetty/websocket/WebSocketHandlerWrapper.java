package spark.embeddedserver.jetty.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * A wrapper for web socket handler classes/instances.
 */
public interface WebSocketHandlerWrapper {
    
    /**
     * Gets the actual handler - if necessary, instantiating an object.
     * 
     * @return The handler instance.
     */
    Object getHandler();
    
    static void validateHandlerClass(Class<?> handlerClass) {
        boolean valid = Session.Listener.class.isAssignableFrom(handlerClass)
                || handlerClass.isAnnotationPresent(WebSocket.class);
        if (!valid) {
            throw new IllegalArgumentException(
                    "WebSocket handler must implement 'Session.Listener' or be annotated as '@WebSocket'");
        }
    }

}
