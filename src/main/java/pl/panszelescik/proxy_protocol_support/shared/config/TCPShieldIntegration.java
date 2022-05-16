package pl.panszelescik.proxy_protocol_support.shared.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Adds TCPShield's servers to whitelisted IPs
 *
 * @author PanSzelescik
 */
public class TCPShieldIntegration {

    private static final String IPV4 = "https://tcpshield.com/v4/";

    public static Collection<CIDRMatcher> getWhitelistedIPs() throws IOException {
        Collection<CIDRMatcher> matchers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(IPV4).openStream()))) {
            while (reader.ready()) {
                String line = reader.readLine().trim();
                if (!line.isEmpty()) {
                    matchers.add(new CIDRMatcher(line));
                }
            }
        }

        return matchers;
    }
}
