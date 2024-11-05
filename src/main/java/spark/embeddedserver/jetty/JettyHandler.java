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

import java.io.IOException;
import java.util.Set;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.session.SessionHandler;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

/**
 * Simple Jetty Handler
 *
 * @author Per Wendel
 */
public class JettyHandler extends HttpServlet {

    private final Filter filter;

    private Set<String> consume;

    public JettyHandler(Filter filter) {
        this.filter = filter;
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
        HttpMethod method = HttpMethod.fromString(request.getMethod().trim().toUpperCase());
        if(method == null) {
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED_405);
            return;
        }

        if(consume!=null && consume.contains(request.getRequestURI())){
            wrapper.notConsumed(true);
        } else {
            filter.doFilter(wrapper, response, null);
        }
    }

    public void consume(Set<String> consume){
        this.consume=consume;
    }

    public Set<String> consume(){
        return this.consume;
    }
}
