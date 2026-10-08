package com.banking.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class IPAddressTest {

    @Test
    @DisplayName("Test valid IPv4 address parsing")
    void testValidIPv4() {
        IPAddress ip1 = new IPAddress("192.168.1.1");
        assertEquals("192.168.1.1", ip1.getDottedDecimal());
        assertEquals("192.168.1.1", ip1.toString());
        assertEquals(192, ip1.getOctet(1));
        assertEquals(168, ip1.getOctet(2));
        assertEquals(1, ip1.getOctet(3));
        assertEquals(1, ip1.getOctet(4));

        IPAddress ip2 = new IPAddress("10.0.0.1");
        assertEquals("10.0.0.1", ip2.getDottedDecimal());

        IPAddress ip3 = new IPAddress("172.16.5.10");
        assertEquals("172.16.5.10", ip3.getDottedDecimal());

        IPAddress ip4 = new IPAddress("10.10.20.100");
        assertEquals("10.10.20.100", ip4.getDottedDecimal());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "256.1.1.1",
            "192.168.1",
            "192.168.1.999",
            "abc.def.1.1",
            "192.168.-1.1",
            "192.168.1.1.1",
            "",
            "   ",
            "192.168.01.1"
    })
    @DisplayName("Test invalid IPv4 addresses throw IllegalArgumentException")
    void testInvalidIPv4(String invalidIp) {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress(invalidIp));
        assertFalse(IPAddress.isValid(invalidIp));
    }

    @Test
    @DisplayName("Test null IPv4 input throws IllegalArgumentException")
    void testNullIPv4() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress((String) null));
        assertFalse(IPAddress.isValid(null));
    }

    @Test
    @DisplayName("Test boundary IP addresses")
    void testBoundaryValues() {
        IPAddress minIp = new IPAddress("0.0.0.0");
        assertEquals(0L, minIp.getValue());
        assertEquals("0.0.0.0", minIp.getDottedDecimal());

        IPAddress maxIp = new IPAddress("255.255.255.255");
        assertEquals(0xFFFFFFFFL, maxIp.getValue());
        assertEquals("255.255.255.255", maxIp.getDottedDecimal());
    }

    @Test
    @DisplayName("Test 32-bit long constructor and getValue()")
    void testLongConstructor() {
        long value = (192L << 24) | (168L << 16) | (10L << 8) | 5L;
        IPAddress ip = new IPAddress(value);
        assertEquals("192.168.10.5", ip.getDottedDecimal());
        assertEquals(value, ip.getValue());

        assertThrows(IllegalArgumentException.class, () -> new IPAddress(-1L));
        assertThrows(IllegalArgumentException.class, () -> new IPAddress(0x100000000L));
    }

    @Test
    @DisplayName("Test octet access methods")
    void testOctetAccess() {
        IPAddress ip = new IPAddress("10.20.30.40");
        assertEquals(10, ip.getOctet1());
        assertEquals(20, ip.getOctet2());
        assertEquals(30, ip.getOctet3());
        assertEquals(40, ip.getOctet4());

        assertThrows(IllegalArgumentException.class, () -> ip.getOctet(0));
        assertThrows(IllegalArgumentException.class, () -> ip.getOctet(5));
    }

    @Test
    @DisplayName("Test equals(), hashCode(), and compareTo()")
    void testEqualsAndHashCode() {
        IPAddress ip1 = new IPAddress("192.168.1.1");
        IPAddress ip2 = new IPAddress("192.168.1.1");
        IPAddress ip3 = new IPAddress("192.168.1.2");

        assertEquals(ip1, ip2);
        assertEquals(ip1.hashCode(), ip2.hashCode());
        assertNotEquals(ip1, ip3);

        assertTrue(ip1.compareTo(ip3) < 0);
        assertTrue(ip3.compareTo(ip1) > 0);
        assertEquals(0, ip1.compareTo(ip2));
    }
}
