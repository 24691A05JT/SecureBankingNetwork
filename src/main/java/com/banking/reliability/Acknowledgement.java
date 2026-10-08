package com.banking.reliability;

import java.util.Objects;

/**
 * Model class representing an Acknowledgement (ACK) packet returned by the receiver.
 */
public class Acknowledgement {

    private final int sequenceNumber;
    private final boolean acknowledged;
    private final String message;

    public Acknowledgement(int sequenceNumber, boolean acknowledged, String message) {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number cannot be negative: " + sequenceNumber);
        }
        this.sequenceNumber = sequenceNumber;
        this.acknowledged = acknowledged;
        this.message = message != null ? message : "";
    }

    public Acknowledgement(int sequenceNumber) {
        this(sequenceNumber, true, "ACK " + sequenceNumber);
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Acknowledgement that = (Acknowledgement) o;
        return sequenceNumber == that.sequenceNumber &&
                acknowledged == that.acknowledged &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, acknowledged, message);
    }

    @Override
    public String toString() {
        return String.format("ACK #%d (%s)", sequenceNumber, acknowledged ? "SUCCESS" : "FAILED");
    }
}
