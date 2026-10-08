package com.banking.reliability;

import com.banking.util.LoggerUtil;

import java.nio.charset.StandardCharsets;

/**
 * Professional Cyclic Redundancy Check (CRC-16-CCITT) service for banking network transmission error detection.
 * Uses polynomial 0x1021 with initial value 0xFFFF.
 */
public class CRCService {

    public static final int POLYNOMIAL = 0x1021;
    public static final int INITIAL_VALUE = 0xFFFF;

    /**
     * Calculates the 16-bit CRC-16-CCITT checksum for a byte array.
     *
     * @param bytes input byte array
     * @return 16-bit unsigned integer CRC value (0x0000 to 0xFFFF)
     */
    public static int calculateCRC(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Input bytes cannot be null");
        }

        int crc = INITIAL_VALUE;
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
            }
        }
        int finalCrc = crc & 0xFFFF;
        LoggerUtil.info("CRC-16-CCITT calculation completed. Result: " + formatCRC(finalCrc));
        return finalCrc;
    }

    /**
     * Calculates the 16-bit CRC-16-CCITT checksum for a String payload.
     *
     * @param data input string
     * @return 16-bit unsigned integer CRC value
     */
    public static int calculateCRC(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        return calculateCRC(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Formats a 16-bit CRC value as an unsigned hexadecimal string (e.g., "0x29B1").
     *
     * @param crc 16-bit integer CRC
     * @return formatted hex string
     */
    public static String formatCRC(int crc) {
        return String.format("0x%04X", crc & 0xFFFF);
    }

    /**
     * Verifies if the calculated CRC for the byte array matches the expected CRC.
     *
     * @param bytes byte array data
     * @param expectedCrc expected 16-bit CRC integer
     * @return true if matches, false if transmission error detected
     */
    public static boolean verifyCRC(byte[] bytes, int expectedCrc) {
        if (bytes == null) {
            throw new IllegalArgumentException("Input bytes cannot be null");
        }
        int actualCrc = calculateCRC(bytes);
        boolean match = actualCrc == (expectedCrc & 0xFFFF);
        if (match) {
            LoggerUtil.info("CRC verification passed.");
        } else {
            LoggerUtil.warn("CRC verification failed: transmission corruption detected. (Expected: "
                    + formatCRC(expectedCrc) + ", Actual: " + formatCRC(actualCrc) + ")");
        }
        return match;
    }

    /**
     * Verifies if the calculated CRC for the string matches the expected CRC.
     *
     * @param data string data
     * @param expectedCrc expected 16-bit CRC integer
     * @return true if matches, false if transmission error detected
     */
    public static boolean verifyCRC(String data, int expectedCrc) {
        if (data == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        return verifyCRC(data.getBytes(StandardCharsets.UTF_8), expectedCrc);
    }

    /**
     * Processes a string input into a CRCResult container.
     *
     * @param data input string
     * @return CRCResult container
     */
    public static CRCResult process(String data) {
        int crc = calculateCRC(data);
        return new CRCResult(data, crc);
    }
}
