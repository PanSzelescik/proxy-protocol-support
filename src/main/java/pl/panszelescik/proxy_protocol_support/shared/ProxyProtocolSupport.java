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

    public static Consumer<String> infoLogger = System.out::println;
    public static Consumer<String> warnLogger = System.out::println;
    public static Consumer<String> errorLogger = System.out::println;

    public static boolean enableProxyProtocol = false;
    public static Collection<CIDRMatcher> whitelistedIPs = new ArrayList<>();

    public static void initialize(Config config) throws IOException {
        loggerInitialize();

        if (!config.enableProxyProtocol) {
            ProxyProtocolSupport.infoLogger.accept("Proxy Protocol disabled!");
            return;
        }

        ProxyProtocolSupport.infoLogger.accept("Proxy Protocol enabled!");

        ProxyProtocolSupport.enableProxyProtocol = config.enableProxyProtocol;
        ProxyProtocolSupport.whitelistedIPs = config.whitelistedIPs
                .stream()
                .map(CIDRMatcher::new)
                .collect(Collectors.toSet());

        if (config.whitelistTCPShieldServers) {
            ProxyProtocolSupport.infoLogger.accept("TCPShield integration enabled!");
            whitelistedIPs = Stream
                    .concat(whitelistedIPs.stream(), TCPShieldIntegration.getWhitelistedIPs().stream())
                    .collect(Collectors.toSet());
        }

        ProxyProtocolSupport.infoLogger.accept("Using " + ProxyProtocolSupport.whitelistedIPs.size() + " whitelisted IPs: " + ProxyProtocolSupport.whitelistedIPs);
    }

    private static void loggerInitialize() {
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
