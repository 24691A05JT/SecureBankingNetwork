package com.banking.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BankingNetworkConfigTest {

    @Test
    @DisplayName("Verify predefined banking subnets configuration")
    void testBankingSubnets() {
        assertEquals("10.0.0.0/24", BankingNetworkConfig.HEAD_OFFICE.getCidrNotation());
        assertEquals("10.0.1.0/24", BankingNetworkConfig.BRANCH_1.getCidrNotation());
        assertEquals("10.0.2.0/24", BankingNetworkConfig.BRANCH_2.getCidrNotation());
        assertEquals("10.0.10.0/24", BankingNetworkConfig.ATM_NETWORK.getCidrNotation());
        assertEquals("10.0.20.0/24", BankingNetworkConfig.CUSTOMER_SERVICE.getCidrNotation());
        assertEquals("10.0.30.0/24", BankingNetworkConfig.MANAGEMENT.getCidrNotation());
        assertEquals("10.0.40.0/24", BankingNetworkConfig.DATABASE.getCidrNotation());

        Map<String, Subnet> allSubnets = BankingNetworkConfig.getAllSubnets();
        assertEquals(7, allSubnets.size());
    }

    @Test
    @DisplayName("Test subnet lookup by IP address")
    void testFindSubnetName() {
        IPAddress hoIp = new IPAddress("10.0.0.15");
        assertEquals("HEAD OFFICE", BankingNetworkConfig.findSubnetName(hoIp));

        IPAddress atmIp = new IPAddress("10.0.10.101");
        assertEquals("ATM NETWORK", BankingNetworkConfig.findSubnetName(atmIp));

        IPAddress extIp = new IPAddress("192.168.1.1");
        assertEquals("UNKNOWN / EXTERNAL", BankingNetworkConfig.findSubnetName(extIp));
    }
}
