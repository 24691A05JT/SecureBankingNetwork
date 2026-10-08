package com.banking.network;

import java.util.Objects;

/**
 * Reusable IPv4 Subnet representation and calculation engine.
 * Supports CIDR notation, subnet masks, network & broadcast address calculations,
 * host ranges, and same-subnet verification.
 */
public final class Subnet {

    private final IPAddress ipAddress;
    private final int prefixLength; // 0 to 32
    private final IPAddress subnetMask;
    private final IPAddress networkAddress;
    private final IPAddress broadcastAddress;
    private final IPAddress firstUsableHost;
    private final IPAddress lastUsableHost;
    private final long totalAddresses;
    private final long usableHostCount;

    /**
     * Constructs a Subnet from CIDR notation (e.g., "192.168.10.0/24").
     *
     * @param cidrStr CIDR format string
     * @throws IllegalArgumentException if the CIDR format or IP/prefix is invalid
     */
    public Subnet(String cidrStr) {
        if (cidrStr == null) {
            throw new IllegalArgumentException("CIDR string cannot be null.");
        }
        String trimmed = cidrStr.trim();
        int slashIndex = trimmed.indexOf('/');
        if (slashIndex == -1) {
            throw new IllegalArgumentException("Invalid CIDR notation: '" + cidrStr + "'. Missing prefix length separator '/'.");
        }
        String ipPart = trimmed.substring(0, slashIndex);
        String prefixPart = trimmed.substring(slashIndex + 1);

        this.ipAddress = new IPAddress(ipPart);

        try {
            this.prefixLength = Integer.parseInt(prefixPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CIDR prefix length '" + prefixPart + "' in CIDR: '" + cidrStr + "'. Must be an integer between 0 and 32.");
        }

        if (this.prefixLength < 0 || this.prefixLength > 32) {
            throw new IllegalArgumentException("Prefix length must be between 0 and 32, got: " + this.prefixLength + " in CIDR: '" + cidrStr + "'.");
        }

        // Initialize calculated fields
        long maskVal = calculateMask(this.prefixLength);
        this.subnetMask = new IPAddress(maskVal);

        long netVal = this.ipAddress.getValue() & maskVal;
        this.networkAddress = new IPAddress(netVal);

        long invMask = (~maskVal) & 0xFFFFFFFFL;
        long bcastVal = netVal | invMask;
        this.broadcastAddress = new IPAddress(bcastVal);

        if (this.prefixLength == 32) {
            this.totalAddresses = 1;
            this.usableHostCount = 1;
            this.firstUsableHost = this.networkAddress;
            this.lastUsableHost = this.networkAddress;
        } else if (this.prefixLength == 31) {
            this.totalAddresses = 2;
            this.usableHostCount = 2;
            this.firstUsableHost = this.networkAddress;
            this.lastUsableHost = this.broadcastAddress;
        } else {
            this.totalAddresses = 1L << (32 - this.prefixLength);
            this.usableHostCount = this.totalAddresses - 2;
            this.firstUsableHost = new IPAddress(netVal + 1);
            this.lastUsableHost = new IPAddress(bcastVal - 1);
        }
    }

    /**
     * Constructs a Subnet from an IPAddress object and prefix length.
     *
     * @param ipAddress IP address object
     * @param prefixLength CIDR prefix length (0-32)
     */
    public Subnet(IPAddress ipAddress, int prefixLength) {
        this((ipAddress != null ? ipAddress.getDottedDecimal() : null) + "/" + prefixLength);
    }

    private static long calculateMask(int prefix) {
        if (prefix == 0) {
            return 0L;
        }
        return (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
    }

    /**
     * Checks if a given IP address belongs to this subnet.
     *
     * @param ip target IPAddress
     * @return true if the IP belongs to this subnet, false otherwise
     */
    public boolean contains(IPAddress ip) {
        if (ip == null) {
            return false;
        }
        return (ip.getValue() & subnetMask.getValue()) == networkAddress.getValue();
    }

    /**
     * Checks if a given IP address string belongs to this subnet.
     *
     * @param ipStr target IP address string
     * @return true if valid and belongs to this subnet, false otherwise
     */
    public boolean contains(String ipStr) {
        if (!IPAddress.isValid(ipStr)) {
            return false;
        }
        return contains(new IPAddress(ipStr));
    }

    /**
     * Checks if two IP addresses both belong to this subnet.
     *
     * @param ip1 first IP
     * @param ip2 second IP
     * @return true if both belong to this subnet
     */
    public boolean isSameSubnet(IPAddress ip1, IPAddress ip2) {
        return contains(ip1) && contains(ip2);
    }

    /**
     * Static utility to check if two IP addresses belong to the same subnet under a given prefix length.
     *
     * @param ip1 first IP address
     * @param ip2 second IP address
     * @param prefixLength CIDR prefix length (0-32)
     * @return true if both IPs share the same network address for that prefix length
     */
    public static boolean isSameSubnet(IPAddress ip1, IPAddress ip2, int prefixLength) {
        if (ip1 == null || ip2 == null) {
            return false;
        }
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Prefix length must be between 0 and 32, got: " + prefixLength);
        }
        long mask = calculateMask(prefixLength);
        return (ip1.getValue() & mask) == (ip2.getValue() & mask);
    }

    /**
     * Static utility to check if two IP addresses belong to the same subnet given a subnet mask.
     *
     * @param ip1 first IP address
     * @param ip2 second IP address
     * @param subnetMask subnet mask IPAddress
     * @return true if both IPs share the same network address
     */
    public static boolean isSameSubnet(IPAddress ip1, IPAddress ip2, IPAddress subnetMask) {
        if (ip1 == null || ip2 == null || subnetMask == null) {
            return false;
        }
        long mask = subnetMask.getValue();
        return (ip1.getValue() & mask) == (ip2.getValue() & mask);
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    public int getCidrPrefix() {
        return prefixLength;
    }

    public IPAddress getSubnetMask() {
        return subnetMask;
    }

    public IPAddress getNetworkAddress() {
        return networkAddress;
    }

    public IPAddress getBroadcastAddress() {
        return broadcastAddress;
    }

    public IPAddress getFirstUsableHost() {
        return firstUsableHost;
    }

    public IPAddress getLastUsableHost() {
        return lastUsableHost;
    }

    public long getTotalAddresses() {
        return totalAddresses;
    }

    public long getUsableHostCount() {
        return usableHostCount;
    }

    public String getCidrNotation() {
        return networkAddress.getDottedDecimal() + "/" + prefixLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subnet subnet = (Subnet) o;
        return prefixLength == subnet.prefixLength && Objects.equals(networkAddress, subnet.networkAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkAddress, prefixLength);
    }

    @Override
    public String toString() {
        return getCidrNotation();
    }
}
