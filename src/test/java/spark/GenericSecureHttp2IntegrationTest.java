package spark;

import org.conscrypt.OpenSSLProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import spark.util.SparkTestUtil.UrlResponse;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GenericSecureHttp2IntegrationTest extends GenericSecureIntegrationTest {

    @BeforeClass
    public static void setup() {
        Security.insertProviderAt(new OpenSSLProvider(), 1);
        Spark.http2();
        GenericSecureIntegrationTest.setup();
    }

    @Override
    UrlResponse doMethodSecure(String requestMethod, String path, String body) throws Exception {
        return testUtil.doHttp2MethodSecure(requestMethod, path, body);
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
    @Ignore // Broken in tests, but works in browser. Disabled until we find a way to fix it
    public void testHttp1SecureRequest() throws Exception {
        UrlResponse responseHttp = testUtil.doMethodSecure("GET", "/hi", null);
        assertEquals(200, responseHttp.status);
        assertEquals("Hello World!", responseHttp.body);
    }
}
