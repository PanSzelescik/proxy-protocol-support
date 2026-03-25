package pl.panszelescik.proxy_protocol_support.shared.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Taken & modified from TCPShield, licensed under MIT. See https://github.com/TCPShield/RealIP/blob/master/LICENSE
 *
 * https://github.com/TCPShield/RealIP/blob/32d422a9523cb6e25b571072851f3306bb8bbc4f/src/main/java/net/tcpshield/tcpshield/validation/cidr/CIDRMatcher.java
 */
public class CIDRMatcher {
    private final int maskBits;
    private final int maskBytes;
    private final boolean simpleCIDR;
    private final InetAddress cidrAddress;

    public CIDRMatcher(String ipAddress) {
        String[] split = ipAddress.split("/", 2);

        String parsedIPAddress;
        if (split.length == 2) {
            parsedIPAddress = split[0];

            this.maskBits = Integer.parseInt(split[1]);
            this.simpleCIDR = maskBits == 32;
        } else {
            parsedIPAddress = ipAddress;

            this.maskBits = -1;
            this.simpleCIDR = true;
        }

        this.maskBytes = simpleCIDR ? -1 : maskBits / 8;

        try {
            cidrAddress = InetAddress.getByName(parsedIPAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matches(InetAddress inetAddress) {
        // check if IP is IPv4 or IPv6
        if (cidrAddress.getClass() != inetAddress.getClass()) {
            return false;
        }

        // check for equality if it's a simple CIDR
        if (simpleCIDR) {
            return inetAddress.equals(cidrAddress);
        }

        byte[] inetAddressBytes = inetAddress.getAddress();
        byte[] requiredAddressBytes = cidrAddress.getAddress();

        byte finalByte = (byte) (0xFF00 >> (maskBits & 0x07));

        for (int i = 0; i < maskBytes; i++) {
            if (inetAddressBytes[i] != requiredAddressBytes[i]) {
                return false;
            }
        }

        if (finalByte != 0) {
            return (inetAddressBytes[maskBytes] & finalByte) == (requiredAddressBytes[maskBytes] & finalByte);
        }

        return true;
    }

    @Override
    public String toString() {
        return simpleCIDR ? cidrAddress.toString().substring(1) : cidrAddress.toString().substring(1) + "/" + maskBits;
    }
}
