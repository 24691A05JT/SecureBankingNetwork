package com.banking.network;

/**
 * Enumeration representing the functional node types within the banking network topology.
 */
public enum NodeType {
    ATM("ATM Terminal"),
    BRANCH("Branch Gateway"),
    ROUTER("Core Network Router"),
    SERVER("Bank Server"),
    MANAGER("Management Workstation"),
    CUSTOMER_SERVICE("Customer Service Terminal"),
    HEAD_OFFICE("Head Office Core Node");

    private final String description;

    NodeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}
