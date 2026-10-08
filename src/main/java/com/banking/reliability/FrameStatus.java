package com.banking.reliability;

/**
 * Status enumeration for a network frame in the Sliding Window protocol.
 */
public enum FrameStatus {
    UNSENT,
    SENT,
    ACKNOWLEDGED,
    LOST,
    RETRANSMITTED
}
