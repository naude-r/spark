package spark.route;

import org.junit.Test;

import spark.utils.SparkUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RouteEntryTest {

    @Test
    public void testMatches_BeforeAndAllPaths() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.before;
        entry.path = SparkUtils.ALL_PATHS;

        assertTrue(
                "Should return true because HTTP method is \"Before\", the methods of route and match request match," +
                        " and the path provided is same as ALL_PATHS (+/*paths)",
                entry.matches(HttpMethod.before, SparkUtils.ALL_PATHS)
        );
    }

    @Test
    public void testMatches_AfterAndAllPaths() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.after;
        entry.path = SparkUtils.ALL_PATHS;

        assertTrue(
                "Should return true because HTTP method is \"After\", the methods of route and match request match," +
                        " and the path provided is same as ALL_PATHS (+/*paths)",
                entry.matches(HttpMethod.after, SparkUtils.ALL_PATHS)
        );
    }

    @Test
    public void testMatches_NotAllPathsAndDidNotMatchHttpMethod() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.post;
        entry.path = "/test";

        assertFalse("Should return false because path names did not match",
                    entry.matches(HttpMethod.get, "/path"));
    }

    @Test
    public void testMatches_MatchingPaths() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/";

        assertTrue("Should return true because route path and path is exactly the same",
                   entry.matches(HttpMethod.get, "/test/"));
    }

    @Test
    public void testMatches_WithWildcardOnEntryPath() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/*";

        assertTrue("Should return true because path specified is covered by the route path wildcard",
                   entry.matches(HttpMethod.get, "/test/me"));
    }

    @Test
    public void testMatches_PathsDoNotMatch() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/me";

        assertFalse("Should return false because path does not match route path",
                    entry.matches(HttpMethod.get, "/test/other"));
    }

    @Test
    public void testMatches_PathsMatchWithParam() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/:name";

        assertTrue("Should return true because path matches route path",
                    entry.matches(HttpMethod.get, "/test/other"));
    }

    @Test
    public void testMatches_RegexContains() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "~/private";

        assertTrue("Should return true because path matches route path",
                   entry.matches(HttpMethod.get, "/panel/private-room/my.page"));
        assertFalse("Should return false because path does not matches route path",
                    entry.matches(HttpMethod.get, "/there/is/no/privacy/here.page"));
    }
    @Test
    public void testMatches_RegexBeginning() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "~/^\\/(user|login)";

        assertTrue("Should return true because path matches route path",
                   entry.matches(HttpMethod.get, "/users.list"));
        assertFalse("Should return false because path does not matches route path",
                    entry.matches(HttpMethod.get, "/admin.jsp"));
    }
    @Test
    public void testMatches_RegexFull() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "~/^\\/(?<topic>[^-]+)-(?<code>[^-]+)-(?<text>[^.]+)\\.(?<ext>.*)$/";

        assertTrue("Should return true because path matches route path",
                   entry.matches(HttpMethod.get, "/login-1233-shake%20hands.html"));
        assertFalse("Should return false because path does not matches route path",
                    entry.matches(HttpMethod.get, "/login-1233.html"));
    }
    @Test
    public void testMatches_RegexEnding() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "~/\\.html?$/";

        assertTrue("Should return true because path matches route path",
                   entry.matches(HttpMethod.get, "/beer/hold-into-my-glass.htm"));
        assertFalse("Should return false because path does not matches route path",
                   entry.matches(HttpMethod.get, "/beer/hold-into-my-glass.pdf"));
    }

    @Test
    public void testMatches_longRoutePathWildcard() {

        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/this/resource/*";

        assertTrue("Should return true because path specified is covered by the route path wildcard",
                   entry.matches(HttpMethod.get, "/test/this/resource/child/id"));
    }

    //CS304 (manually written) Issue link: https://github.com/perwendel/spark/issues/1151
    @Test
    public void testMatches_WithoutOptionalParameters() {
        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/:name?";

        assertTrue("Should return true because the :name? section in path is optional",
            entry.matches(HttpMethod.get, "/test"));
    }
    //CS304 (manually written) Issue link: https://github.com/perwendel/spark/issues/1151
    @Test
    public void testMatches_WithOptionalParameters() {
        RouteEntry entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/:name?";

        assertTrue("Should return true because the :name? section in path is optional," +
                "the statement can match /test/*",
            entry.matches(HttpMethod.get, "/test/foo"));
    }

}
