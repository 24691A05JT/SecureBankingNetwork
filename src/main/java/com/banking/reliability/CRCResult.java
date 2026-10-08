package com.banking.reliability;

import java.util.Objects;

/**
 * Immutable container encapsulating data payload and its calculated CRC-16-CCITT checksum.
 */
public final class CRCResult {

    private final String data;
    private final int crc;
    private final String crcFormatted;

    public CRCResult(String data, int crc) {
        this.data = Objects.requireNonNull(data, "Data cannot be null");
        this.crc = crc & 0xFFFF;
        this.crcFormatted = String.format("0x%04X", this.crc);
    }

    public String getData() {
        return data;
    }

    public int getCrc() {
        return crc;
    }

    public String getCrcFormatted() {
        return crcFormatted;
    }

    public boolean verifyAgainst(int expectedCrc) {
        return this.crc == (expectedCrc & 0xFFFF);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CRCResult crcResult = (CRCResult) o;
        return crc == crcResult.crc && Objects.equals(data, crcResult.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, crc);
    }

    @Override
    public String toString() {
        return String.format("CRCResult[dataLen=%d, CRC-16-CCITT=%s]", data.length(), crcFormatted);
    }
}
