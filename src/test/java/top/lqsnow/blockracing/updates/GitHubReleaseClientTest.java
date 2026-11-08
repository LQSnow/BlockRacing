package top.lqsnow.blockracing.updates;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GitHubReleaseClientTest {
    @Test
    void comparesNumericVersionsAndOptionalPrefixes() {
        assertTrue(GitHubReleaseClient.isNewer("v26.3.10", "26.3.9"));
        assertTrue(GitHubReleaseClient.isNewer("26.4.0", "26.3.99"));
        assertTrue(GitHubReleaseClient.isNewer("26.3.1", "3.5"));
        assertFalse(GitHubReleaseClient.isNewer("26.3", "26.3.0"));
        assertFalse(GitHubReleaseClient.isNewer("26.2.99", "26.3.0"));
        assertFalse(GitHubReleaseClient.isNewer("26.3.0", "26.3.0"));
    }

    @Test
    void ignoresDevelopmentAndUnrecognisedVersions() {
        for (String version : new String[]{"26.x", "26.4.0-beta", "26.4-SNAPSHOT", "latest", "", " 26.4.0", null}) {
            assertFalse(GitHubReleaseClient.isNewer(version, "26.3.0"));
            assertFalse(GitHubReleaseClient.isNewer("26.4.0", version));
        }
    }

    @Test
    void onlyAcceptsNewerPublishedStableReleases() {
        assertEquals("26.4.0", GitHubReleaseClient.parse(release("v26.4.0"), "26.3.0").orElseThrow().version());
        assertTrue(GitHubReleaseClient.parse(release("26.3.0"), "26.3.0").isEmpty());
        assertTrue(GitHubReleaseClient.parse(release("26.2.1"), "26.3.0").isEmpty());
        assertTrue(GitHubReleaseClient.parse(release("26.4.0").replace("\"draft\":false", "\"draft\":true"), "26.3.0").isEmpty());
        assertTrue(GitHubReleaseClient.parse(release("26.4.0").replace("\"prerelease\":false", "\"prerelease\":true"), "26.3.0").isEmpty());
        for (String invalid : new String[]{"not json", "[]", "{}", "null", "{\"tag_name\":123}"}) {
            assertTrue(GitHubReleaseClient.parse(invalid, "26.3.0").isEmpty());
        }
    }

    @Test
    void handlesActualHttpResponsesAndFailuresSilently() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            int status = switch (path) { case "/missing" -> 404; case "/limited" -> 403; case "/redirect" -> 302; default -> 200; };
            byte[] body = (path.equals("/large") ? " ".repeat(65537) : path.equals("/bad") ? "oops" : release("v26.4.0"))
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            try (var out = exchange.getResponseBody()) { out.write(body); }
        });
        server.start();
        URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        GitHubReleaseClient client = new GitHubReleaseClient();
        try {
            assertEquals("26.4.0", client.check(base.resolve("/ok"), "26.3.0").orElseThrow().version());
            for (String path : new String[]{"/missing", "/limited", "/redirect", "/large", "/bad"}) {
                assertTrue(client.check(base.resolve(path), "26.3.0").isEmpty(), path);
            }
        } finally { server.stop(0); }
        assertTrue(client.check(base.resolve("/offline"), "26.3.0").isEmpty());
    }

    private static String release(String tag) {
        return "{\"tag_name\":\"" + tag + "\",\"draft\":false,\"prerelease\":false}";
    }
}
