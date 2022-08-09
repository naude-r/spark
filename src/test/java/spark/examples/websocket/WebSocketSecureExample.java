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
package spark.examples.websocket;

import spark.util.SparkTestUtil;

import static spark.Spark.init;
import static spark.Spark.secure;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

public class WebSocketSecureExample {

    public static void main(String[] args) {
        // Will serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
        staticFiles.location("/public");

        secure(SparkTestUtil.getKeyStoreLocation(), SparkTestUtil.getKeystorePassword(), null, null);

        webSocket("/echo", EchoWebSocket.class);
        webSocket("/ping", PingWebSocket.class);
        init();
    }
}
