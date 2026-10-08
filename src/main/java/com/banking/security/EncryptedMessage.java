package com.banking.security;

import java.util.Base64;
import java.util.Objects;

/**
 * Immutable container holding Base64-encoded AES-GCM ciphertext and initialization vector (IV).
 */
public final class EncryptedMessage {

    private final String encryptedData;       // Base64-encoded ciphertext + GCM auth tag
    private final String initializationVector; // Base64-encoded 12-byte IV

    /**
     * Constructs an EncryptedMessage from Base64 string representations.
     *
     * @param encryptedData Base64 ciphertext string
     * @param initializationVector Base64 IV string
     */
    public EncryptedMessage(String encryptedData, String initializationVector) {
        this.encryptedData = Objects.requireNonNull(encryptedData, "Encrypted data cannot be null");
        this.initializationVector = Objects.requireNonNull(initializationVector, "Initialization vector cannot be null");
    }

    /**
     * Constructs an EncryptedMessage from raw byte arrays.
     *
     * @param encryptedBytes raw ciphertext bytes
     * @param ivBytes raw IV bytes
     */
    public EncryptedMessage(byte[] encryptedBytes, byte[] ivBytes) {
        Objects.requireNonNull(encryptedBytes, "Encrypted bytes cannot be null");
        Objects.requireNonNull(ivBytes, "IV bytes cannot be null");
        this.encryptedData = Base64.getEncoder().encodeToString(encryptedBytes);
        this.initializationVector = Base64.getEncoder().encodeToString(ivBytes);
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public String getInitializationVector() {
        return initializationVector;
    }

    public byte[] getEncryptedBytes() {
        return Base64.getDecoder().decode(encryptedData);
    }

    public byte[] getIvBytes() {
        return Base64.getDecoder().decode(initializationVector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptedMessage that = (EncryptedMessage) o;
        return Objects.equals(encryptedData, that.encryptedData) &&
                Objects.equals(initializationVector, that.initializationVector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptedData, initializationVector);
    }

    @Override
    public String toString() {
        return String.format("EncryptedMessage[ciphertextLen=%d, ivLen=%d]",
                encryptedData.length(), initializationVector.length());
    }
}
