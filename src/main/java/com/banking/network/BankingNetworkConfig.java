package com.banking.network;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Predefined logical subnet configuration for the Secure Banking Network.
 * Uses private IPv4 address allocations for simulated banking infrastructure.
 */
public final class BankingNetworkConfig {

    public static final Subnet HEAD_OFFICE = new Subnet("10.0.0.0/24");
    public static final Subnet BRANCH_1 = new Subnet("10.0.1.0/24");
    public static final Subnet BRANCH_2 = new Subnet("10.0.2.0/24");
    public static final Subnet ATM_NETWORK = new Subnet("10.0.10.0/24");
    public static final Subnet CUSTOMER_SERVICE = new Subnet("10.0.20.0/24");
    public static final Subnet MANAGEMENT = new Subnet("10.0.30.0/24");
    public static final Subnet DATABASE = new Subnet("10.0.40.0/24");

    private static final Map<String, Subnet> ALL_SUBNETS;

    static {
        Map<String, Subnet> map = new LinkedHashMap<>();
        map.put("HEAD OFFICE", HEAD_OFFICE);
        map.put("BRANCH 1", BRANCH_1);
        map.put("BRANCH 2", BRANCH_2);
        map.put("ATM NETWORK", ATM_NETWORK);
        map.put("CUSTOMER SERVICE", CUSTOMER_SERVICE);
        map.put("MANAGEMENT", MANAGEMENT);
        map.put("DATABASE SERVER NETWORK", DATABASE);
        ALL_SUBNETS = Collections.unmodifiableMap(map);
    }

    private BankingNetworkConfig() {
        // Utility class
    }

    /**
     * Gets an unmodifiable map of all logical subnets in the banking network.
     *
     * @return map of subnet name to Subnet instance
     */
    public static Map<String, Subnet> getAllSubnets() {
        return ALL_SUBNETS;
    }

    /**
     * Identifies which banking network subnet an IP address belongs to, if any.
     *
     * @param ip target IPAddress
     * @return subnet name if matched, or "UNKNOWN / EXTERNAL" if none
     */
    public static String findSubnetName(IPAddress ip) {
        if (ip == null) return "UNKNOWN";
        for (Map.Entry<String, Subnet> entry : ALL_SUBNETS.entrySet()) {
            if (entry.getValue().contains(ip)) {
                return entry.getKey();
            }
        }
        return "UNKNOWN / EXTERNAL";
    }
}
