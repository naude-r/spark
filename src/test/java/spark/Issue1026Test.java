package spark;
import org.junit.Before;
import org.junit.Test;

import spark.util.SparkTestUtil;

import static org.junit.Assert.*;
import static spark.Spark.*;
/**
 * @since 2022/08/11.
 */
public class Issue1026Test {
    private static final String ROUTE = "/api/v1/管理者/";
    private static SparkTestUtil http;

    @Before
    public void setup() {
        http = new SparkTestUtil(4567);
        get(ROUTE, (q,a)-> "Get filter matched");
        awaitInitialization();
    }

    @Test
    public void testUrl() throws Exception {
        SparkTestUtil.UrlResponse response = http.get(ROUTE);
        assertEquals(200,response.status);
    }
}
