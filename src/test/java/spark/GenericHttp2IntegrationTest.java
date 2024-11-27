package spark;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.util.SparkTestUtil.UrlResponse;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GenericHttp2IntegrationTest extends GenericIntegrationTest {

    @BeforeClass
    public static void setup() throws IOException {
        Spark.http2();
        GenericIntegrationTest.setup();
    }

    @Override
    UrlResponse doMethod(String requestMethod, String path, String body, String acceptType) throws Exception {
        return testUtil.doHttp2Method(requestMethod, path, body, acceptType);
    }

    @Override
    UrlResponse doMethod(String requestMethod, String path, String body) throws Exception {
        return testUtil.doHttp2Method(requestMethod, path, body);
    }

    @Override
    UrlResponse doMethod(String requestMethod,
                         String path,
                         String body,
                         boolean secureConnection,
                         String acceptType,
                         Map<String, String> reqHeaders) throws Exception {
        return testUtil.doHttp2Method(requestMethod, path, body, secureConnection, acceptType, reqHeaders);
    }

    @Test
    public void testHttp1Request() throws Exception {
        UrlResponse responseHttp = testUtil.doMethod("GET", "/hi", null);
        assertEquals(200, responseHttp.status);
        assertEquals("Hello World!", responseHttp.body);
    }

    @Override
    HttpClient createHttpClient() {
        return new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()));
    }
}
