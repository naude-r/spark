package spark.examples.hello;

import spark.util.SparkTestUtil;

import static spark.Spark.get;
import static spark.Spark.http2;
import static spark.Spark.secure;

/**
 *
 * You can provide a JKS keystore as arg 0 and its password as arg 1, otherwise we will use one for testing.
 *
 * You can test from command with:
 * > curl -i --http2 -k 'https://localhost:4567/'
 *
 * Or: (nghttp2-client required)
 * To test the upgrade you can run: "nghttp -vu https://127.0.0.1:4567/"
 * To test the direct use of http2: "nghttp -v https://127.0.0.1:4567/"
 */
public class HelloSecureHttp2World {
    public static void main(String[] args) {
        if(args.length == 0) {
            secure(SparkTestUtil.getKeyStoreLocation(), SparkTestUtil.getKeystorePassword(), null, null);
        } else {
            secure(args[0], args[1], null, null);
        }
        http2();
        get("/", (request, response) -> {
            return "Hello Secure HTTP2 World!";
        });
    }
}
