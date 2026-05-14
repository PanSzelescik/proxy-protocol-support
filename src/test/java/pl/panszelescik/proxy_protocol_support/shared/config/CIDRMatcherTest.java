package pl.panszelescik.proxy_protocol_support.shared.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class CIDRMatcherTest {

    // --- IPv4 exact match (no mask) ---

    @Test
    void ipv4ExactMatch() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("192.168.1.1");
        assertTrue(matcher.matches(InetAddress.getByName("192.168.1.1")));
        assertFalse(matcher.matches(InetAddress.getByName("192.168.1.2")));
    }

    // --- IPv4 /32 (full mask) ---

    @Test
    void ipv4FullMask() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("192.168.1.1/32");
        assertTrue(matcher.matches(InetAddress.getByName("192.168.1.1")));
        assertFalse(matcher.matches(InetAddress.getByName("192.168.1.2")));
    }

    // --- IPv4 /24 ---

    @Test
    void ipv4Slash24Match() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("192.168.1.0/24");
        assertTrue(matcher.matches(InetAddress.getByName("192.168.1.1")));
        assertTrue(matcher.matches(InetAddress.getByName("192.168.1.254")));
        assertFalse(matcher.matches(InetAddress.getByName("192.168.2.1")));
    }

    // --- IPv4 /16 ---

    @Test
    void ipv4Slash16Match() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("10.0.0.0/16");
        assertTrue(matcher.matches(InetAddress.getByName("10.0.1.1")));
        assertFalse(matcher.matches(InetAddress.getByName("10.1.0.1")));
    }

    // --- IPv4 /0 (match everything) ---

    @Test
    void ipv4Slash0MatchesAll() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("0.0.0.0/0");
        assertTrue(matcher.matches(InetAddress.getByName("1.2.3.4")));
        assertTrue(matcher.matches(InetAddress.getByName("255.255.255.255")));
    }

    // --- IPv6 exact match (no mask) ---

    @Test
    void ipv6ExactMatch() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("::1");
        assertTrue(matcher.matches(InetAddress.getByName("::1")));
        assertFalse(matcher.matches(InetAddress.getByName("::2")));
    }

    // --- IPv6 /128 (full mask) ---

    @Test
    void ipv6FullMask() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("::1/128");
        assertTrue(matcher.matches(InetAddress.getByName("::1")));
        assertFalse(matcher.matches(InetAddress.getByName("::2")));
    }

    // --- IPv6 /32 (the bugfix test) ---

    @Test
    void ipv6Slash32PrefixMatch() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("2001:db8::/32");
        assertTrue(matcher.matches(InetAddress.getByName("2001:db8::1")));
        assertTrue(matcher.matches(InetAddress.getByName("2001:db8:abcd::1")));
        assertFalse(matcher.matches(InetAddress.getByName("2001:db9::1")));
    }

    // --- IPv6 /64 ---

    @Test
    void ipv6Slash64Match() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("2001:db8:abcd::/64");
        assertTrue(matcher.matches(InetAddress.getByName("2001:db8:abcd::1")));
        assertTrue(matcher.matches(InetAddress.getByName("2001:db8:abcd:0:0:0:0:1")));
        assertFalse(matcher.matches(InetAddress.getByName("2001:db8:abce::1")));
    }

    // --- IPv6 /48 ---

    @Test
    void ipv6Slash48Match() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("2001:db8:abcd::/48");
        assertTrue(matcher.matches(InetAddress.getByName("2001:db8:abcd:ef01::1")));
        assertFalse(matcher.matches(InetAddress.getByName("2001:db8:abce::1")));
    }

    // --- Cross-family mismatch ---

    @Test
    void ipv4DoesNotMatchIpv6() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("0.0.0.0/0");
        assertFalse(matcher.matches(InetAddress.getByName("::1")));
    }

    @Test
    void ipv6DoesNotMatchIpv4() throws Exception {
        CIDRMatcher matcher = new CIDRMatcher("::/0");
        assertFalse(matcher.matches(InetAddress.getByName("1.2.3.4")));
    }

    // --- toString ---

    @Test
    void toStringSimple() {
        CIDRMatcher matcher = new CIDRMatcher("10.0.0.1");
        assertEquals("10.0.0.1", matcher.toString());
    }

    @Test
    void toStringWithMask() {
        CIDRMatcher matcher = new CIDRMatcher("10.0.0.0/24");
        assertEquals("10.0.0.0/24", matcher.toString());
    }

    // --- Edge: non-aligned mask boundary ---

    @ParameterizedTest
    @CsvSource({
        "192.168.1.0/23, 192.168.0.1,  true",
        "192.168.1.0/23, 192.168.2.1,  false",
        "192.168.1.128/25, 192.168.1.129, true",
        "192.168.1.128/25, 192.168.1.127, false"
    })
    void ipv4NonAlignedMask(String cidr, String testIp, boolean expected) throws Exception {
        CIDRMatcher matcher = new CIDRMatcher(cidr);
        assertEquals(expected, matcher.matches(InetAddress.getByName(testIp)));
    }

    @ParameterizedTest
    @CsvSource({
        "2001:db8:abcd:ef00::/49, 2001:db8:abcd:ef00::1,  true",
        "2001:db8:abcd:ef00::/49, 2001:db8:abcd:6f00::1,  false"
    })
    void ipv6NonAlignedMask(String cidr, String testIp, boolean expected) throws Exception {
        CIDRMatcher matcher = new CIDRMatcher(cidr);
        assertEquals(expected, matcher.matches(InetAddress.getByName(testIp)));
    }
}
