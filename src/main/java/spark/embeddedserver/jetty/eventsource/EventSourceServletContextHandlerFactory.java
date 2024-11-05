package spark.embeddedserver.jetty.eventsource;

import java.util.Map;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlets.EventSourceServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSourceServletContextHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(EventSourceServletContextHandlerFactory.class);

    /**
     * Creates a new eventSource servlet context handler.
     *
     * @param eventSourceHandlers          eventSourceHandlers
     * @return a new eventSource servlet context handler or 'null' if creation failed.
     */
    public static ServletContextHandler create(Map<String, EventSourceHandlerWrapper> eventSourceHandlers) {
        ServletContextHandler eventSourceServletContextHandler = null;
        if (eventSourceHandlers != null) {
            try {
                eventSourceServletContextHandler = new ServletContextHandler("/", true, false);
                addToExistingContext(eventSourceServletContextHandler, eventSourceHandlers);
            } catch (Exception ex) {
                logger.error("creation of event source context handler failed.", ex);
                eventSourceServletContextHandler = null;
            }
        }
        return eventSourceServletContextHandler;
    }

    public static void addToExistingContext(ServletContextHandler contextHandler, Map<String, EventSourceHandlerWrapper> eventSourceHandlers){
        if (eventSourceHandlers == null)
            return;
        eventSourceHandlers.forEach((path, servletWrapper)->
            contextHandler.addServlet(new ServletHolder((EventSourceServlet)servletWrapper.getHandler()), path));
    }
}
