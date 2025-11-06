# Proxy Protocol Support


Proxy Protocol Support is a [Fabric](https://fabricmc.net/) and [Quilt](https://quiltmc.org/) mod which adds support for [Proxy Protocol (HAProxy)](https://www.haproxy.com/blog/haproxy/proxy-protocol/ "Proxy Protocol (HAProxy)") for your Minecraft server. It allows to accept both proxied and direct connections simultaneously.

For example you can use [TCPShield](https://tcpshield.com/ "TCPShield") or other software ([Nginx](https://nginx.org/en/docs/stream/ngx_stream_proxy_module.html#proxy_protocol "Nginx"), [FRP](https://gofrp.org/)) to forward traffic, and hide your server's IP address. Without Proxy Protocol, your console will only show the proxy's IP address. By reading the Proxy Protocol packet, this mod makes showing the player's real IP address possible.

**This is a Server-Side only mod; it does nothing when installed on a client.**

Supports Minecraft versions 1.14-1.21.10 and probably later (untested).

---

## Secure Hybrid Mode

This mod allows your Minecraft server to simultaneously accept connections from a **trusted proxy** (using the PROXY protocol) and from **trusted direct-access IPs**. This is perfect for setups where you want to hide your server's IP behind a proxy, while still allowing admins or local players to connect directly.

The mod operates on a secure **"Default Deny"** principle. If an incoming connection's IP address is not explicitly whitelisted in one of the lists below, it will be **rejected**. This prevents unauthorized users from bypassing your proxy and connecting directly to your server's real IP address.

---

## Configuration

Configuration is handled in `config/proxy_protocol_support.json`.

### `enableProxyProtocol`
Set to `true` to enable the mod's functionality.
-   **Default**: `true`

### `proxyServerIPs`
A list of IP addresses that are your trusted proxy servers. Connections from these IPs **must** send a PROXY Protocol header.
-   **Default**: `["127.0.0.1"]`

### `directAccessIPs`
A list of IPs or CIDR ranges that are allowed to connect directly **without** a PROXY Protocol header. This is ideal for admins, local network players, and server-side tools.
-   **Default**: `["127.0.0.1", "192.168.0.0/16"]`

### `whitelistTCPShieldServers`
When `true`, the mod automatically fetches TCPShield's official proxy IPs and adds them to the trusted `proxyServerIPs` list.
-   **Default**: `false`

---

## Example Scenarios

#### Scenario 1: Server behind FRP on the same machine
You run `frpc` on the same machine as the Minecraft server. Players from the internet connect through the proxy. You want to connect directly from your gaming PC on the same LAN (`192.168.1.100`).

```toml
proxyServerIPs = ["127.0.0.1"]
directAccessIPs = ["127.0.0.1", "192.168.1.100"]
```

#### Scenario 2: Server protected by a remote proxy service
Your server is at home, and you use a VPS with the IP 203.0.113.10 as a proxy. You want your entire home network (192.168.0.0/24) to have direct access.

```toml
proxyServerIPs = ["203.0.113.10"]
directAccessIPs = ["127.0.0.1", "192.168.0.0/24"]
```

#### Scenario 3: Maximum Security (Proxy Only)
You only want to allow connections through your proxies and nothing else (not even from the local machine).
```toml
proxyServerIPs = ["1.2.3.4", "5.6.7.8"] # Your proxy IPs
directAccessIPs = [] # Leave empty
```

Looking for Minecraft server? Visit [BedrockHost.pl](https://bedrockhost.pl/)