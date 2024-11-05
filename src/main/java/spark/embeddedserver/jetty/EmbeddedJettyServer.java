/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.embeddedserver.jetty;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.jetty.eventsource.EventSourceHandlerWrapper;
import spark.embeddedserver.jetty.eventsource.EventSourceServletContextHandlerFactory;
import spark.embeddedserver.jetty.websocket.WebSocketHandlerWrapper;
import spark.embeddedserver.jetty.websocket.WebSocketServletContextHandlerFactory;
import spark.ssl.SslStores;

/**
 * Spark server implementation
 *
 * @author Per Wendel
 */
public class EmbeddedJettyServer implements EmbeddedServer {

    private static final int SPARK_DEFAULT_PORT = 4567;
    private static final String NAME = "Spark";

    private final JettyServerFactory serverFactory;
    private final Handler handler;
    private Server server;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, WebSocketHandlerWrapper> webSocketHandlers;
    //private Map<String, EventSourceHandlerWrapper> eventSourceHandlers;
    private Optional<Long> webSocketIdleTimeoutMillis;

    private ThreadPool threadPool = null;
    private boolean trustForwardHeaders = true; // true by default

    public EmbeddedJettyServer(JettyServerFactory serverFactory, HttpServlet servlet, boolean httpOnly) {
        this.serverFactory = serverFactory;
        this.handler = toServletHandler(servlet, httpOnly);
    }

    @Override
    public void configureWebSockets(Map<String, WebSocketHandlerWrapper> webSocketHandlers,
                                    Optional<Long> webSocketIdleTimeoutMillis) {

        this.webSocketHandlers = webSocketHandlers;
        this.webSocketIdleTimeoutMillis = webSocketIdleTimeoutMillis;
    }

    /*@Override
    public void configureEventSourcing(Map<String, EventSourceHandlerWrapper> eventSourceHandlers) {
        this.eventSourceHandlers = eventSourceHandlers;
    }*/

    @Override
    public void trustForwardHeaders(boolean trust) {
        this.trustForwardHeaders = trust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int ignite(String host,
                      int port,
                      SslStores sslStores,
                      int maxThreads,
                      int minThreads,
                      int threadIdleTimeoutMillis,
                      boolean http2Enabled) throws Exception {

        boolean hasCustomizedConnectors = false;

        if (port == 0) {
            try (ServerSocket s = new ServerSocket(0)) {
                port = s.getLocalPort();
            } catch (IOException e) {
                logger.error("Could not get first available port (port set to 0), using default: {}", SPARK_DEFAULT_PORT);
                port = SPARK_DEFAULT_PORT;
            }
        }

        // Create instance of jetty server with either default or supplied queued thread pool
        if(threadPool == null) {
            server = serverFactory.create(maxThreads, minThreads, threadIdleTimeoutMillis);
        } else {
            server = serverFactory.create(threadPool);
        }

        ServerConnector connector;

        if (sslStores == null) {
            if (http2Enabled) {
                connector = SocketConnectorFactory.createHttp2SocketConnector(server, host, port, trustForwardHeaders);
            } else {
                connector = SocketConnectorFactory.createSocketConnector(server, host, port, trustForwardHeaders);
            }
        } else {
            if (http2Enabled) {
                connector = SocketConnectorFactory.createSecureHttp2SocketConnector(server, host, port, sslStores, trustForwardHeaders);
            } else {
                connector = SocketConnectorFactory.createSecureSocketConnector(server, host, port, sslStores, trustForwardHeaders);
            }
        }

        Connector previousConnectors[] = server.getConnectors();
        server = connector.getServer();
        if (previousConnectors.length != 0) {
            server.setConnectors(previousConnectors);
            hasCustomizedConnectors = true;
        } else {
            server.addConnector(connector);
        }

        ServletContextHandler webSocketServletContextHandler =
            WebSocketServletContextHandlerFactory.create(webSocketHandlers, webSocketIdleTimeoutMillis);
        //ServletContextHandler eventSourceServletContextHandler =
        //    EventSourceServletContextHandlerFactory.create(eventSourceHandlers);

        // Handle web socket routes
        if (webSocketServletContextHandler == null) { // && eventSourceServletContextHandler == null) {
            server.setHandler(handler);
        } else {
            List<Handler> handlersInList = new ArrayList<>();
            JettyHandler jettyHandler = (JettyHandler) handler;
            jettyHandler.consume(webSocketHandlers.keySet());
            //jettyHandler.consume(eventSourceHandlers.keySet());
            handlersInList.add(handler);

            // WebSocket handler must be the last one
            handlersInList.add(webSocketServletContextHandler);
              //  handlersInList.add(eventSourceServletContextHandler);

            Handler.Sequence handlers = new Handler.Sequence(new Handler[handlersInList.size()]);
            server.setHandler(handlers);
        }

        logger.info("== {} has ignited ...", NAME);
        if (hasCustomizedConnectors) {
            logger.info(">> Listening on Custom Server ports!");
        } else {
            logger.info(">> Listening on {}:{}", host, port);
        }

        server.start();
        return port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void join() throws InterruptedException {
        server.join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extinguish() {
        logger.info(">>> {} shutting down ...", NAME);
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            logger.error("stop failed", e);
            System.exit(100); // NOSONAR
        }
        logger.info("done");
    }

    @Override
    public int activeThreadCount() {
        if (server == null) {
            return 0;
        }
        return server.getThreadPool().getThreads() - server.getThreadPool().getIdleThreads();
    }

    /**
     * Sets optional thread pool for jetty server.  This is useful for overriding the default thread pool
     * behaviour for example io.dropwizard.metrics.jetty9.InstrumentedQueuedThreadPool.
     * @param threadPool thread pool
     * @return Builder pattern - returns this instance
     */
    public EmbeddedJettyServer withThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    private static ServletContextHandler toServletHandler(final HttpServlet servlet, boolean httpOnly) {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");

        // Add Application Servlets
        servletContextHandler.addServlet(servlet, "/");
        servletContextHandler.getSessionHandler().getSessionCookieConfig().setHttpOnly(httpOnly);

        return servletContextHandler;
    }
}
