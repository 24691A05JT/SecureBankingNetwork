package com.banking.network;

import java.util.Objects;

/**
 * Immutable representation of an IPv4 address.
 * Provides validation, 32-bit conversion, octet access, and string formatting.
 */
public final class IPAddress implements Comparable<IPAddress> {

    private final long ipValue; // Unsigned 32-bit value (0L to 4294967295L)
    private final String dottedDecimal;

    /**
     * Constructs an IPAddress from a dotted decimal string (e.g., "192.168.1.1").
     *
     * @param ipStr the IP address string
     * @throws IllegalArgumentException if the IP address string is null, malformed, or out of range
     */
    public IPAddress(String ipStr) {
        this.ipValue = parseToLong(ipStr);
        this.dottedDecimal = longToDottedDecimal(this.ipValue);
    }

    /**
     * Constructs an IPAddress from a 32-bit numeric value.
     *
     * @param ipValue the 32-bit integer representation (0L to 4294967295L)
     * @throws IllegalArgumentException if ipValue is outside [0, 4294967295]
     */
    public IPAddress(long ipValue) {
        if (ipValue < 0 || ipValue > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("IP value out of 32-bit range [0, 4294967295]: " + ipValue);
        }
        this.ipValue = ipValue;
        this.dottedDecimal = longToDottedDecimal(this.ipValue);
    }

    /**
     * Static factory method to parse an IP address string.
     *
     * @param ipStr dotted decimal IP string
     * @return a new IPAddress instance
     */
    public static IPAddress of(String ipStr) {
        return new IPAddress(ipStr);
    }

    /**
     * Static factory method to create an IPAddress from a 32-bit value.
     *
     * @param ipValue 32-bit long value
     * @return a new IPAddress instance
     */
    public static IPAddress of(long ipValue) {
        return new IPAddress(ipValue);
    }

    /**
     * Validates whether a given string is a valid IPv4 address.
     *
     * @param ipStr the string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String ipStr) {
        if (ipStr == null) {
            return false;
        }
        String trimmed = ipStr.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String[] parts = trimmed.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || (part.length() > 1 && part.startsWith("0"))) {
                // Reject empty octets or leading zeros like "01" (except single "0")
                return false;
            }
            try {
                int val = Integer.parseInt(part);
                if (val < 0 || val > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static long parseToLong(String ipStr) {
        if (ipStr == null) {
            throw new IllegalArgumentException("IP address string cannot be null.");
        }
        String trimmed = ipStr.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("IP address string cannot be empty.");
        }
        String[] parts = trimmed.split("\\.", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 format: '" + ipStr + "'. IP address must contain exactly 4 octets separated by dots.");
        }
        long result = 0;
        for (int i = 0; i < 4; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Invalid IPv4 format: '" + ipStr + "'. Octet " + (i + 1) + " is empty.");
            }
            if (part.length() > 1 && part.startsWith("0")) {
                throw new IllegalArgumentException("Invalid IPv4 format: '" + ipStr + "'. Octet " + (i + 1) + " has leading zeros.");
            }
            try {
                int octet = Integer.parseInt(part);
                if (octet < 0 || octet > 255) {
                    throw new IllegalArgumentException("Octet " + (i + 1) + " value '" + octet + "' is out of range (0-255) in IP address '" + ipStr + "'.");
                }
                result = (result << 8) | octet;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid octet '" + part + "' at position " + (i + 1) + " in IP address '" + ipStr + "': must be an integer.");
            }
        }
        return result;
    }

    private static String longToDottedDecimal(long value) {
        return ((value >> 24) & 0xFF) + "." +
               ((value >> 16) & 0xFF) + "." +
               ((value >> 8) & 0xFF) + "." +
               (value & 0xFF);
    }

    /**
     * Gets the 32-bit unsigned numeric value of the IP address.
     *
     * @return 32-bit long value
     */
    public long getValue() {
        return ipValue;
    }

    /**
     * Returns an individual octet by index (1 to 4).
     *
     * @param index octet position (1 = highest octet, 4 = lowest octet)
     * @return integer octet value (0-255)
     * @throws IllegalArgumentException if index is not between 1 and 4
     */
    public int getOctet(int index) {
        return switch (index) {
            case 1 -> (int) ((ipValue >> 24) & 0xFF);
            case 2 -> (int) ((ipValue >> 16) & 0xFF);
            case 3 -> (int) ((ipValue >> 8) & 0xFF);
            case 4 -> (int) (ipValue & 0xFF);
            default -> throw new IllegalArgumentException("Octet index must be between 1 and 4, got: " + index);
        };
    }

    public int getOctet1() { return getOctet(1); }
    public int getOctet2() { return getOctet(2); }
    public int getOctet3() { return getOctet(3); }
    public int getOctet4() { return getOctet(4); }

    /**
     * Returns the dotted decimal string representation of this IP address.
     *
     * @return dotted decimal string (e.g. "192.168.1.1")
     */
    public String getDottedDecimal() {
        return dottedDecimal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPAddress ipAddress = (IPAddress) o;
        return ipValue == ipAddress.ipValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipValue);
    }

    @Override
    public String toString() {
        return dottedDecimal;
    }

    @Override
    public int compareTo(IPAddress other) {
        return Long.compare(this.ipValue, other.ipValue);
    }
}
