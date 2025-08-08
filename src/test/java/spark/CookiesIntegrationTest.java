package spark;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static spark.Spark.halt;
import static spark.Spark.post;

/**
 * System tests for the Cookies support.
 *
 * @author dreambrother
 */
public class CookiesIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CookiesIntegrationTest.class);

    private static final String DEFAULT_HOST_URL = "http://localhost:4567";

    private final HttpClient httpClient = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom()
            // Test fails without this change
            // Apache HTTP Client 4.4+ does not handle RFC compliant headers well
            // https://stackoverflow.com/questions/36473478/fixing-httpclient-warning-invalid-expires-attribute-using-fluent-api
            .setCookieSpec(CookieSpecs.STANDARD)
            .build())
        .build();

    @BeforeClass
    public static void initRoutes() throws InterruptedException {
        post("/assertNoCookies", (request, response) -> {
            if (!request.cookies().isEmpty()) {
                LOGGER.error("Received non empty cookies -> {}", request.cookies());
                halt(500);
            }
            return "";
        });

        post("/setCookie", (request, response) -> {
            response.cookie(request.queryParams("cookieName"), request.queryParams("cookieValue"));
            return "";
        });

        post("/assertHasCookie", (request, response) -> {
            String cookieValue = request.cookie(request.queryParams("cookieName"));
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                LOGGER.error("No cookie [{}] found!", request.queryParams("cookieName"));
                halt(500);
            }
            return "";
        });

        post("/removeCookie", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.cookie(cookieName);
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                LOGGER.error("cookie [{}] has value [{}], expected [{}]", cookieName, cookieValue, request.queryParams("cookieValue"));
                halt(500);
            }
            response.removeCookie(cookieName);
            return "";
        });

        post("/path/setCookieWithPath", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.queryParams("cookieValue");
            response.cookie("/path", cookieName, cookieValue, -1, false);
            return "";
        }) ;

        post("/path/removeCookieWithPath", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.cookie(cookieName);
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                LOGGER.error("cookie [{}] with value [{}]. expected [{}]", cookieName, cookieValue, request.queryParams("cookieValue"));
                halt(500);
            }
            response.removeCookie("/path", cookieName);
            return "";
        }) ;

        post("/path/assertNoCookies", (request, response) -> {
            if (!request.cookies().isEmpty()) {
                LOGGER.error("cookies not empty! -> {}", request.cookies());
                halt(500);
            }
            return "";
        });

        Thread.sleep(500);
    }

    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }

    @Test
    public void testEmptyCookies() {
        httpPost("/assertNoCookies");
    }

    @Test
    public void testCreateCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertHasCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/removeCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertNoCookies");
    }

    @Test
    public void testRemoveCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/removeCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertNoCookies");
    }

    @Test
    public void testRemoveCookieWithPath() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/path/setCookieWithPath?cookieName=" + cookieName + "&cookieValue=" + cookieValue);

        // for sanity, check that cookie is not sent with request if path doesn't match
        httpPost("/assertNoCookies");

        // now remove cookie with matching path
        httpPost("/path/removeCookieWithPath?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/path/assertNoCookies");
    }

    private void httpPost(String relativePath) {
        HttpPost request = new HttpPost(DEFAULT_HOST_URL + relativePath);
        try {
            HttpResponse response = httpClient.execute(request);
            assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            request.releaseConnection();
        }
    }
}
