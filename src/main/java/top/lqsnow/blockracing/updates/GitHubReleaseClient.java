package top.lqsnow.blockracing.updates;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

/** A bounded, read-only request. An unavailable or unrecognised release is silent. */
public final class GitHubReleaseClient {
    private static final URI ENDPOINT = URI.create("https://api.github.com/repos/LQSnow/BlockRacing/releases/latest");
    public static final String RELEASE_URL = "https://github.com/LQSnow/BlockRacing/releases/latest";
    private static final Pattern VERSION = Pattern.compile("[vV]?[0-9]{1,9}(\\.[0-9]{1,9}){1,3}");
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;

    public record Release(String version) {}

    public Optional<Release> check(String currentVersion) {
        return check(ENDPOINT, currentVersion);
    }

    // Package-private endpoint injection lets tests exercise real HTTP failures without GitHub.
    Optional<Release> check(URI endpoint, String currentVersion) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) endpoint.toURL().openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            connection.setRequestProperty("User-Agent", "BlockRacing-UpdateChecker");
            if (connection.getResponseCode() != 200) return Optional.empty();
            try (InputStream stream = connection.getInputStream()) {
                byte[] bytes = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
                if (bytes.length > MAX_RESPONSE_BYTES) return Optional.empty();
                return parse(new String(bytes, StandardCharsets.UTF_8), currentVersion);
            }
        } catch (Exception ignored) {
            return Optional.empty();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    static Optional<Release> parse(String json, String currentVersion) {
        try {
            Object parsed = new JSONParser().parse(json);
            if (!(parsed instanceof JSONObject release)
                    || !Boolean.FALSE.equals(release.get("draft"))
                    || !Boolean.FALSE.equals(release.get("prerelease"))
                    || !(release.get("tag_name") instanceof String tag)
                    || !isNewer(tag, currentVersion)) return Optional.empty();
            return Optional.of(new Release(tag.replaceFirst("^[vV]", "")));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    static boolean isNewer(String remote, String current) {
        if (remote == null || current == null || !VERSION.matcher(remote).matches()
                || !VERSION.matcher(current).matches()) return false;
        String[] left = remote.replaceFirst("^[vV]", "").split("\\.");
        String[] right = current.replaceFirst("^[vV]", "").split("\\.");
        for (int i = 0; i < Math.max(left.length, right.length); i++) {
            BigInteger a = i < left.length ? new BigInteger(left[i]) : BigInteger.ZERO;
            BigInteger b = i < right.length ? new BigInteger(right[i]) : BigInteger.ZERO;
            int comparison = a.compareTo(b);
            if (comparison != 0) return comparison > 0;
        }
        return false;
    }
}
