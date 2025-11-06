package pl.panszelescik.proxy_protocol_support.shared.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;

/**
 * Adds TCPShield's servers to whitelisted IPs
 *
 * @author PanSzelescik
 */
public class TCPShieldIntegration {

    private static final URI IPV4 = URI.create("https://tcpshield.com/v4/");
    private static final URI IPV4_CF = URI.create("https://tcpshield.com/v4-cf/");

    public static HashSet<CIDRMatcher> getWhitelistedIPs() throws IOException {
        final HashSet<CIDRMatcher> matchers = new HashSet<>();

        readFromUrl(matchers, IPV4);
        readFromUrl(matchers, IPV4_CF);

        return matchers;
    }

    private static void readFromUrl(HashSet<CIDRMatcher> matchers, URI uri) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream()))) {
            while (reader.ready()) {
                final String line = reader.readLine().trim();
                if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("127.0.0.1")) {
                    matchers.add(new CIDRMatcher(line));
                }
            }
        }
    }
}
