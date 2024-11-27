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
package spark.http.matching;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spark.CustomErrorPages;
import spark.ExceptionMapper;
import spark.HaltException;
import spark.RequestResponseFactory;
import spark.Response;
import spark.route.HttpMethod;
import spark.routematch.RouteMatch;
import spark.serialization.SerializerChain;
import spark.staticfiles.StaticFilesConfiguration;

/**
 * Matches Spark routes and filters.
 *
 * @author Per Wendel
 */
public class MatcherFilter implements Filter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MatcherFilter.class);

    private static final String ACCEPT_TYPE_REQUEST_MIME_HEADER = "Accept";
    private static final String HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";

    private final StaticFilesConfiguration staticFiles;

    private final spark.route.Routes routeMatcher;
    private final SerializerChain serializerChain;
    private final ExceptionMapper exceptionMapper;

    private final boolean externalContainer;
    private final boolean hasOtherHandlers;

    /**
     * Constructor
     *
     * @param routeMatcher      The route matcher
     * @param staticFiles       The static files configuration object
     * @param externalContainer Tells the filter that Spark is run in an external web container.
     *                          If true, chain.doFilter will be invoked if request is not consumed by Spark.
     * @param hasOtherHandlers  If true, do nothing if request is not consumed by Spark in order to let others handlers process the request.
     */
    public MatcherFilter(spark.route.Routes routeMatcher,
                         StaticFilesConfiguration staticFiles,
                         ExceptionMapper exceptionMapper,
                         boolean externalContainer,
                         boolean hasOtherHandlers) {

        this.routeMatcher = routeMatcher;
        this.staticFiles = staticFiles;
        this.exceptionMapper = exceptionMapper;
        this.externalContainer = externalContainer;
        this.hasOtherHandlers = hasOtherHandlers;
        this.serializerChain = new SerializerChain();
    }

    @Override
    public void init(FilterConfig config) {
        //
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        final String method = getHttpMethodFrom(httpRequest);
        final String httpMethodStr = method.toLowerCase();
        final HttpMethod httpMethod = HttpMethod.get(httpMethodStr);
        if (httpMethod == HttpMethod.unsupported) {
            httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // handle static resources
        boolean consumedByStaticFile = staticFiles.consume(httpRequest, httpResponse);
        if (consumedByStaticFile) {
            return;
        }

        String acceptType = httpRequest.getHeader(ACCEPT_TYPE_REQUEST_MIME_HEADER);
        String uri = httpRequest.getRequestURI();
        uri = URLDecoder.decode(uri, "UTF-8");

        List<RouteMatch> routes = routeMatcher.findAll();
        String firstAcceptType = null;
        for (RouteMatch rm : routes) {
            if (rm.getMatchUri().equals(uri)) {
                firstAcceptType = rm.getAcceptType();
                break;
            }
        }
        if ("*/*".equals(acceptType) && firstAcceptType != null) {
            acceptType = firstAcceptType;
        }

        final Body body = Body.create();

        final RequestWrapper requestWrapper = RequestWrapper.create();
        final ResponseWrapper responseWrapper = ResponseWrapper.create();
        final Response response = RequestResponseFactory.create(httpResponse);

        RouteContext context = RouteContext.create()
                .withMatcher(routeMatcher)
                .withHttpRequest(httpRequest)
                .withUri(uri)
                .withAcceptType(acceptType)
                .withBody(body)
                .withRequestWrapper(requestWrapper)
                .withResponseWrapper(responseWrapper)
                .withResponse(response)
                .withHttpMethod(httpMethod);

        try {
            try {
                BeforeFilters.execute(context);
                Routes.execute(context);
                AfterFilters.execute(context);
            } catch (HaltException halt) {

                Halt.modify(httpResponse, body, halt);

            } catch (Exception generalException) {

                GeneralError.modify(
                        httpRequest,
                        httpResponse,
                        body,
                        requestWrapper,
                        responseWrapper,
                        exceptionMapper,
                        generalException);

            }

            // If redirected and content is null set to empty string to not throw NotConsumedException
            if (body.notSet() && responseWrapper.isRedirected()) {
                body.set("");
            }

            if (body.notSet()) {
                int returnStatus;
                if(httpMethodStr.equals("put") && response.status() == 200) returnStatus = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
                else returnStatus = HttpServletResponse.SC_NOT_FOUND;
                LOG.info("The requested route [{}] has not been mapped in Spark for {}: [{}]",
                         uri, ACCEPT_TYPE_REQUEST_MIME_HEADER, acceptType);
                httpResponse.setStatus(returnStatus);

                if (CustomErrorPages.existsFor(returnStatus)) {
                    requestWrapper.setDelegate(RequestResponseFactory.create(httpRequest));
                    responseWrapper.setDelegate(RequestResponseFactory.create(httpResponse));
                    body.set(CustomErrorPages.getFor(returnStatus, requestWrapper, responseWrapper));
                } else {
                    body.set(String.format(CustomErrorPages.NOT_FOUND));
                }
            }
        } finally {
            try {
                AfterAfterFilters.execute(context);
            } catch (Exception generalException) {
                GeneralError.modify(
                        httpRequest,
                        httpResponse,
                        body,
                        requestWrapper,
                        responseWrapper,
                        exceptionMapper,
                        generalException);
            }
        }

        if (body.isSet()) {
            body.serializeTo(httpResponse, serializerChain, httpRequest, responseWrapper.compression);
        } else if (chain != null) {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private String getHttpMethodFrom(HttpServletRequest httpRequest) {
        String method = httpRequest.getHeader(HTTP_METHOD_OVERRIDE_HEADER);

        if (method == null) {
            method = httpRequest.getMethod();
        }
        return method;
    }

    @Override
    public void destroy() {
    }


}
