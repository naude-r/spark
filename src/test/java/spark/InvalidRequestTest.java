package spark;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import spark.util.SparkTestUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

public class InvalidRequestTest {

    public static class HttpFoo extends HttpRequestBase {
        public final static String METHOD_NAME = "FOO";
        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
        public HttpFoo(final String uri) {
            super();
            setURI(URI.create(uri));
        }
        public String getName() {
            return "FOO";
        }
    }

    public static final String FILE = "/page.html";

    public static final String SERVICE="/test";

    public static final int PORT = 4567;

    private static final SparkTestUtil http = new SparkTestUtil(PORT);

    @Before
    public void setup() {
        staticFileLocation("/public");
        get(SERVICE, (request, response) -> {
            assertTrue(request.requestMethod().equalsIgnoreCase("GET"));
            return "Hello";
        });

        awaitInitialization();
    }

    public int requestPathWithInvalidMethod(String path) {
        int code = 0;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpFoo fooMethod = new HttpFoo("http://localhost:" + PORT + path);
            HttpResponse response = httpClient.execute(fooMethod);
            code = response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        return code;
    }

    @Test
    public void invalidRequestTest(){
        // Testing that file and service is up:
        try {
            SparkTestUtil.UrlResponse response = http.doMethod("GET", SERVICE, "");
            assertEquals(200, response.status);
            assertEquals("Hello", response.body);

            response = http.doMethod("GET", FILE, "");
            assertEquals(200, response.status);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
        // Testing wrong method (we cannot use http.doMethod as it can not handle invalid methods)
        assertEquals(405, requestPathWithInvalidMethod(FILE));
        assertEquals(405, requestPathWithInvalidMethod(SERVICE));
    }
}
