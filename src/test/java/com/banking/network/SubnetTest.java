package com.banking.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SubnetTest {

    @Test
    @DisplayName("Test standard Class C subnet /24 calculations")
    void testClassCSubnet() {
        Subnet subnet = new Subnet("192.168.10.0/24");

        assertEquals("255.255.255.0", subnet.getSubnetMask().getDottedDecimal());
        assertEquals("192.168.10.0", subnet.getNetworkAddress().getDottedDecimal());
        assertEquals("192.168.10.255", subnet.getBroadcastAddress().getDottedDecimal());
        assertEquals("192.168.10.1", subnet.getFirstUsableHost().getDottedDecimal());
        assertEquals("192.168.10.254", subnet.getLastUsableHost().getDottedDecimal());
        assertEquals(256, subnet.getTotalAddresses());
        assertEquals(254, subnet.getUsableHostCount());
    }

    @Test
    @DisplayName("Test Class A subnet /8 calculations")
    void testClassASubnet() {
        Subnet subnet = new Subnet("10.0.0.0/8");

        assertEquals("255.0.0.0", subnet.getSubnetMask().getDottedDecimal());
        assertEquals("10.0.0.0", subnet.getNetworkAddress().getDottedDecimal());
        assertEquals("10.255.255.255", subnet.getBroadcastAddress().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getFirstUsableHost().getDottedDecimal());
        assertEquals("10.255.255.254", subnet.getLastUsableHost().getDottedDecimal());
        assertEquals(16777216L, subnet.getTotalAddresses());
        assertEquals(16777214L, subnet.getUsableHostCount());
    }

    @Test
    @DisplayName("Test Class B subnet /16 calculations")
    void testClassBSubnet() {
        Subnet subnet = new Subnet("172.16.0.0/16");

        assertEquals("255.255.0.0", subnet.getSubnetMask().getDottedDecimal());
        assertEquals("172.16.0.0", subnet.getNetworkAddress().getDottedDecimal());
        assertEquals("172.16.255.255", subnet.getBroadcastAddress().getDottedDecimal());
        assertEquals("172.16.0.1", subnet.getFirstUsableHost().getDottedDecimal());
        assertEquals("172.16.255.254", subnet.getLastUsableHost().getDottedDecimal());
        assertEquals(65536L, subnet.getTotalAddresses());
        assertEquals(65534L, subnet.getUsableHostCount());
    }

    @Test
    @DisplayName("Test subnet /26 calculations")
    void testSlash26Subnet() {
        Subnet subnet = new Subnet("192.168.20.0/26");

        assertEquals("255.255.255.192", subnet.getSubnetMask().getDottedDecimal());
        assertEquals("192.168.20.0", subnet.getNetworkAddress().getDottedDecimal());
        assertEquals("192.168.20.63", subnet.getBroadcastAddress().getDottedDecimal());
        assertEquals("192.168.20.1", subnet.getFirstUsableHost().getDottedDecimal());
        assertEquals("192.168.20.62", subnet.getLastUsableHost().getDottedDecimal());
        assertEquals(64L, subnet.getTotalAddresses());
        assertEquals(62L, subnet.getUsableHostCount());
    }

    @Test
    @DisplayName("Test point-to-point /31 edge case")
    void testSlash31EdgeCase() {
        Subnet subnet = new Subnet("10.0.0.0/31");

        assertEquals("255.255.255.254", subnet.getSubnetMask().getDottedDecimal());
        assertEquals("10.0.0.0", subnet.getNetworkAddress().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getBroadcastAddress().getDottedDecimal());
        assertEquals("10.0.0.0", subnet.getFirstUsableHost().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getLastUsableHost().getDottedDecimal());
        assertEquals(2L, subnet.getTotalAddresses());
        assertEquals(2L, subnet.getUsableHostCount());
    }

    @Test
    @DisplayName("Test single host route /32 edge case")
    void testSlash32EdgeCase() {
        Subnet subnet = new Subnet("10.0.0.1/32");

        assertEquals("255.255.255.255", subnet.getSubnetMask().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getNetworkAddress().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getBroadcastAddress().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getFirstUsableHost().getDottedDecimal());
        assertEquals("10.0.0.1", subnet.getLastUsableHost().getDottedDecimal());
        assertEquals(1L, subnet.getTotalAddresses());
        assertEquals(1L, subnet.getUsableHostCount());
    }

    @Test
    @DisplayName("Test same subnet verification (true and false cases)")
    void testSameSubnet() {
        Subnet subnet = new Subnet("192.168.10.0/24");

        IPAddress ip1 = new IPAddress("192.168.10.20");
        IPAddress ip2 = new IPAddress("192.168.10.50");
        IPAddress ip3 = new IPAddress("192.168.11.20");

        assertTrue(subnet.contains(ip1));
        assertTrue(subnet.contains(ip2));
        assertFalse(subnet.contains(ip3));

        assertTrue(subnet.isSameSubnet(ip1, ip2));
        assertFalse(subnet.isSameSubnet(ip1, ip3));

        assertTrue(Subnet.isSameSubnet(ip1, ip2, 24));
        assertFalse(Subnet.isSameSubnet(ip1, ip3, 24));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "192.168.1.0/33",
            "192.168.1.0/-1",
            "invalid-ip/24",
            "192.168.1.0",
            "192.168.1.0/abc",
            ""
    })
    @DisplayName("Test invalid CIDR notations throw IllegalArgumentException")
    void testInvalidCIDR(String invalidCidr) {
        assertThrows(IllegalArgumentException.class, () -> new Subnet(invalidCidr));
    }
}
