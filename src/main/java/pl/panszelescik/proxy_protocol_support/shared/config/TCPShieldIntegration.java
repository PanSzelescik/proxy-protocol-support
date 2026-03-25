package pl.panszelescik.proxy_protocol_support.shared.config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;

/**
 * Adds TCPShield's servers to whitelisted IPs
 *
 * @author PanSzelescik
 */
public class TCPShieldIntegration {

    private static final URI IPV4 = URI.create("https://tcpshield.com/v4/");
    private static final URI IPV4_CF = URI.create("https://tcpshield.com/v4-cf/");

    public static HashSet<CIDRMatcher> getWhitelistedIPs() throws IOException, InterruptedException {
        final HashSet<CIDRMatcher> matchers = new HashSet<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            readFromUrl(matchers, client, IPV4);
            readFromUrl(matchers, client, IPV4_CF);
        }

        return matchers;
    }

    private static void readFromUrl(HashSet<CIDRMatcher> matchers, HttpClient client, URI uri) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest
                .newBuilder(uri)
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();

        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        final int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Failed to fetch " + uri + ": HTTP " + statusCode);
        }

        response.body()
                .lines()
                .map(String::trim)
                .filter(l -> !l.isEmpty() && !l.startsWith("#") && !l.startsWith("127.0.0.1"))
                .map(CIDRMatcher::new)
                .forEach(matchers::add);
    }
}
