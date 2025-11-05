package pl.panszelescik.proxy_protocol_support.shared.config;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents the structure of the config.toml file.
 * The variable names here MUST match the keys in the TOML file.
 *
 * @author PanSzelescik
 */
public class Config {

    // enableProxyProtocol key from config
    public boolean enableProxyProtocol = true;

    // proxyServerIPs list from config
    public List<String> proxyServerIPs = Arrays.asList("127.0.0.1");

    // directAccessIPs list from config
    public List<String> directAccessIPs = Arrays.asList("127.0.0.1", "192.168.0.0/16");

    // The old config options are now removed to avoid confusion.
    // public List<String> whitelistedIPs; (REMOVED)
    // public List<String> proxyIPs; (REMOVED)
    // public boolean whitelistTCPShieldServers; (REMOVED)
}