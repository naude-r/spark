package spark.examples.hello;

import spark.util.SparkTestUtil;

import static spark.Spark.get;
import static spark.Spark.secure;

/**
 * You can provide a JKS keystore as arg 0 and its password as arg 1, otherwise we will use one for testing.
 *
 * You can test from command with:
 * > curl -i -k 'https://localhost:4567/'
 */
public class HelloSecureWorld {
    public static void main(String[] args) {
        if(args.length == 0) {
            secure(SparkTestUtil.getKeyStoreLocation(), SparkTestUtil.getKeystorePassword(), null, null);
        } else {
            secure(args[0], args[1], null, null);
        }
        get("/", (request, response) -> {
            return "Hello Secure World!";
        });
    }
}
