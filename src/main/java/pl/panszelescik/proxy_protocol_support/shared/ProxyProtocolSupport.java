package pl.panszelescik.proxy_protocol_support.shared;

import pl.panszelescik.proxy_protocol_support.shared.config.CIDRMatcher;
import pl.panszelescik.proxy_protocol_support.shared.config.Config;
import pl.panszelescik.proxy_protocol_support.shared.config.TCPShieldIntegration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Main class for holding configuration and initializing the mod.
 *
 * @author PanSzelescik
 */
public class ProxyProtocolSupport {

    public static final String MODID = "proxy_protocol_support";

    public static Consumer<String> infoLogger;
    public static Consumer<String> warnLogger;
    public static Consumer<String> errorLogger;
    public static Consumer<String> debugLogger;

    public static boolean enableProxyProtocol = false;
    // IPs of trusted proxies that MUST send a PROXY header
    public static Collection<String> proxyServerIPs = new HashSet<>();
    // IPs/CIDRs that can connect directly WITHOUT a PROXY header
    public static Collection<CIDRMatcher> directAccessIPs = new ArrayList<>();

    public static void initialize(Config config) throws IOException {
        if (!config.enableProxyProtocol) {
            infoLogger.accept("Proxy Protocol Support is disabled in the config.");
            return;
        }

        infoLogger.accept("Proxy Protocol Support is enabled!");

        enableProxyProtocol = config.enableProxyProtocol;

        proxyServerIPs = new HashSet<>(config.proxyServerIPs);

        directAccessIPs = config.directAccessIPs
                .stream()
                .map(CIDRMatcher::new)
                .collect(Collectors.toList());

        if (config.whitelistTCPShieldServers) {
            infoLogger.accept("TCPShield integration enabled! Fetching official IPs...");
            try {
                Collection<String> tcpShieldIPs = TCPShieldIntegration.getWhitelistedIPs().stream()
                        .map(CIDRMatcher::toString) // Convert CIDRMatcher back to String if necessary
                        .collect(Collectors.toList());
                proxyServerIPs.addAll(tcpShieldIPs);
                infoLogger.accept("Successfully added " + tcpShieldIPs.size() + " TCPShield IPs to the trusted proxy list.");
            } catch (IOException e) {
                errorLogger.accept("Failed to fetch TCPShield IPs: " + e.getMessage());
            }
        }

        infoLogger.accept("Loaded " + proxyServerIPs.size() + " trusted proxy IPs: " + proxyServerIPs);
        infoLogger.accept("Loaded " + directAccessIPs.size() + " direct access rules: " + config.directAccessIPs);
    }

    static {
        try {
            org.slf4j.Logger slf4j = org.slf4j.LoggerFactory.getLogger(MODID);
            infoLogger = slf4j::info;
            warnLogger = slf4j::warn;
            errorLogger = slf4j::error;
            debugLogger = slf4j::debug;
        } catch (Throwable ignored) {
            try {
                org.apache.logging.log4j.Logger log4j = org.apache.logging.log4j.LogManager.getLogger(MODID);
                infoLogger = log4j::info;
                warnLogger = log4j::warn;
                errorLogger = log4j::error;
                debugLogger = log4j::debug;
            } catch (Throwable ignored2) {
                infoLogger = System.out::println;
                warnLogger = System.out::println;
                errorLogger = System.out::println;
                debugLogger = (s) -> {};
            }
        }
    }
}