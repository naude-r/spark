package spark;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import spark.util.SparkTestUtil;

import static org.junit.Assert.assertEquals;

/**
 * @since 2022/07/26.
 */
public class RequestRootAliasesTest {
    static int port1 = 19832;
    static int port2 = 19833;
    // Prepare clients:
    static final SparkTestUtil http1 = new SparkTestUtil(port1);
    static final SparkTestUtil http2 = new SparkTestUtil(port2);

    @Before
    public void setup() throws IOException {
        // Prepare servers:
        Service s1 = Service.ignite().port(port1);
        Service s2 = Service.ignite().port(port2);
        s1.get("", (request, response) -> {
            System.out.printf("Requested: %s, Matched: %s %n", request.pathInfo(), request.matchedPath());
            assertEquals("/", request.matchedPath());
            return "ok";
        });
        s2.get("/?", (request, response) -> {
            System.out.printf("Requested: %s, Matched: %s %n", request.pathInfo(), request.matchedPath());
            assertEquals("/", request.matchedPath());
            return "ok";
        });

        // Wait for them:
        s1.awaitInitialization();
        s2.awaitInitialization();
    }

    @Test
    public void pathsShouldMatch() {
        SparkTestUtil.UrlResponse res;
        try {
            res = http1.get("/");
            assertEquals(200, res.status);
            res = http2.get("/");
            assertEquals(200, res.status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pathsShouldNotMatch() {
        SparkTestUtil.UrlResponse res;
        try {
            res = http1.get("/_");
            assertEquals(404, res.status);
            res = http2.get("/@");
            assertEquals(404, res.status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
