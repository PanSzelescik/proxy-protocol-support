package pl.panszelescik.proxy_protocol_support.shared;

import pl.panszelescik.proxy_protocol_support.shared.config.CIDRMatcher;
import pl.panszelescik.proxy_protocol_support.shared.config.Config;
import pl.panszelescik.proxy_protocol_support.shared.config.TCPShieldIntegration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple class for getting settings
 *
 * @author PanSzelescik
 */
public class ProxyProtocolSupport {

    public static final String MODID = "proxy_protocol_support";

    public static Consumer<String> infoLogger;
    public static Consumer<String> warnLogger;
    public static Consumer<String> errorLogger;

    public static boolean enableProxyProtocol = false;
    public static Collection<CIDRMatcher> whitelistedIPs = new ArrayList<>();

    public static void initialize(Config config) throws IOException {
        if (!config.enableProxyProtocol) {
            infoLogger.accept("Proxy Protocol disabled!");
            return;
        }

        infoLogger.accept("Proxy Protocol enabled!");

        enableProxyProtocol = config.enableProxyProtocol;
        whitelistedIPs = config.whitelistedIPs
                .stream()
                .map(CIDRMatcher::new)
                .collect(Collectors.toSet());

        if (config.whitelistTCPShieldServers) {
            infoLogger.accept("TCPShield integration enabled!");
            whitelistedIPs = Stream
                    .concat(whitelistedIPs.stream(), TCPShieldIntegration.getWhitelistedIPs().stream())
                    .collect(Collectors.toSet());
        }

        infoLogger.accept("Using " + whitelistedIPs.size() + " whitelisted IPs: " + whitelistedIPs);
    }

    static {
        try {
            org.slf4j.Logger slf4j = org.slf4j.LoggerFactory.getLogger(MODID);
            infoLogger = slf4j::info;
            warnLogger = slf4j::warn;
            errorLogger = slf4j::error;
        } catch (Throwable ignored) {
            try {
                org.apache.logging.log4j.Logger log4j = org.apache.logging.log4j.LogManager.getLogger(MODID);
                infoLogger = log4j::info;
                warnLogger = log4j::warn;
                errorLogger = log4j::error;
            } catch (Throwable ignored2) {
                infoLogger = System.out::println;
                warnLogger = System.out::println;
                errorLogger = System.out::println;
            }
        }
    }
}
