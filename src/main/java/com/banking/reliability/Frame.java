package com.banking.reliability;

import java.util.Objects;

/**
 * Model class representing a data frame transmitted over the banking network.
 */
public class Frame {

    private final int sequenceNumber;
    private String payload;
    private FrameStatus status;

    public Frame(int sequenceNumber, String payload) {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number cannot be negative: " + sequenceNumber);
        }
        this.sequenceNumber = sequenceNumber;
        this.payload = payload != null ? payload : "";
        this.status = FrameStatus.UNSENT;
    }

    public Frame(int sequenceNumber) {
        this(sequenceNumber, "");
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload != null ? payload : "";
    }

    public FrameStatus getStatus() {
        return status;
    }

    public void setStatus(FrameStatus status) {
        this.status = Objects.requireNonNull(status, "Frame status cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frame frame = (Frame) o;
        return sequenceNumber == frame.sequenceNumber &&
                Objects.equals(payload, frame.payload) &&
                status == frame.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, payload, status);
    }

    @Override
    public String toString() {
        return String.format("Frame #%d [%s] (Payload: '%s')", sequenceNumber, status, payload);
    }
}
