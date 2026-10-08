package com.banking.security;

import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import com.banking.util.LoggerUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Professional AES-GCM encryption and decryption service for securing banking transactions.
 * Uses AES/GCM/NoPadding with 256-bit keys and 12-byte random IVs per operation.
 */
public class EncryptionService {

    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES/GCM/NoPadding";
    public static final int KEY_SIZE_BITS = 256;
    public static final int GCM_TAG_LENGTH_BITS = 128;
    public static final int IV_LENGTH_BYTES = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a new secure 256-bit AES SecretKey using Java Cryptography Architecture.
     *
     * @return AES SecretKey
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE_BITS, SECURE_RANDOM);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES algorithm not supported by JCA provider", e);
        }
    }

    /**
     * Generates a secure random 12-byte Initialization Vector (IV) for AES-GCM.
     *
     * @return 12-byte array
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    /**
     * Encrypts a plaintext string using AES-GCM and the provided secret key.
     * A unique random IV is generated for every call.
     *
     * @param plainText text to encrypt
     * @param key AES SecretKey
     * @return EncryptedMessage containing Base64 ciphertext and Base64 IV
     */
    public static EncryptedMessage encrypt(String plainText, SecretKey key) {
        if (plainText == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Secret key cannot be null");
        }

        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            LoggerUtil.info("AES encryption completed successfully.");
            return new EncryptedMessage(cipherTextBytes, iv);
        } catch (GeneralSecurityException e) {
            LoggerUtil.error("Encryption failed: " + e.getMessage());
            throw new SecurityException("Encryption failed due to cryptographic error", e);
        }
    }

    /**
     * Decrypts an EncryptedMessage container using AES-GCM and the provided secret key.
     *
     * @param encryptedMessage container with Base64 ciphertext and Base64 IV
     * @param key AES SecretKey
     * @return decrypted plaintext string
     */
    public static String decrypt(EncryptedMessage encryptedMessage, SecretKey key) {
        if (encryptedMessage == null) {
            throw new IllegalArgumentException("Encrypted message cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Secret key cannot be null");
        }

        try {
            byte[] cipherTextBytes = encryptedMessage.getEncryptedBytes();
            byte[] ivBytes = encryptedMessage.getIvBytes();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
            LoggerUtil.info("AES decryption completed successfully.");
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            LoggerUtil.error("Decryption failed (tampered ciphertext or invalid key/IV): " + e.getMessage());
            throw new SecurityException("Decryption failed: authentication tag mismatch or invalid parameters", e);
        }
    }

    /**
     * Serializes a Transaction object into a deterministic key-value string format.
     *
     * @param transaction Transaction object
     * @return formatted string representation
     */
    public static String serializeTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TRANSACTION_ID=").append(transaction.getTransactionId() != null ? transaction.getTransactionId() : "").append("\n");
        sb.append("SOURCE_ACCOUNT=").append(transaction.getSourceAccount() != null ? transaction.getSourceAccount() : "").append("\n");
        sb.append("DESTINATION_ACCOUNT=").append(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount() : "").append("\n");
        sb.append("AMOUNT=").append(String.format(Locale.US, "%.2f", transaction.getAmount())).append("\n");
        sb.append("TYPE=").append(transaction.getTransactionType() != null ? transaction.getTransactionType().name() : "").append("\n");
        sb.append("STATUS=").append(transaction.getStatus() != null ? transaction.getStatus().name() : "").append("\n");
        sb.append("TIMESTAMP=").append(transaction.getTimestamp() != null ? transaction.getTimestamp() : "").append("\n");
        sb.append("DESCRIPTION=").append(transaction.getDescription() != null ? transaction.getDescription() : "");

        return sb.toString();
    }

    /**
     * Deserializes a key-value formatted string back into a Transaction object.
     *
     * @param serialized String representation
     * @return reconstructed Transaction
     */
    public static Transaction deserializeTransaction(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            throw new IllegalArgumentException("Serialized transaction string cannot be null or empty");
        }

        Map<String, String> map = new HashMap<>();
        String[] lines = serialized.split("\n");
        for (String line : lines) {
            int idx = line.indexOf('=');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                map.put(key, val);
            }
        }

        String transactionId = map.getOrDefault("TRANSACTION_ID", "");
        String sourceAccount = map.getOrDefault("SOURCE_ACCOUNT", "");
        String destinationAccount = map.getOrDefault("DESTINATION_ACCOUNT", "");
        double amount = Double.parseDouble(map.getOrDefault("AMOUNT", "0.0"));
        TransactionType type = TransactionType.valueOf(map.getOrDefault("TYPE", "TRANSFER"));
        TransactionStatus status = TransactionStatus.valueOf(map.getOrDefault("STATUS", "PENDING"));
        String timestamp = map.getOrDefault("TIMESTAMP", null);
        String description = map.getOrDefault("DESCRIPTION", "");

        return new Transaction(0L, transactionId, sourceAccount, destinationAccount, amount, type, status, timestamp, description);
    }

    /**
     * Serializes and encrypts a Transaction object.
     *
     * @param transaction Transaction object
     * @param key AES SecretKey
     * @return EncryptedMessage
     */
    public static EncryptedMessage encryptTransaction(Transaction transaction, SecretKey key) {
        String serialized = serializeTransaction(transaction);
        EncryptedMessage encryptedMessage = encrypt(serialized, key);
        LoggerUtil.info("Transaction message encrypted successfully");
        return encryptedMessage;
    }

    /**
     * Decrypts and deserializes an EncryptedMessage into a Transaction object.
     *
     * @param encryptedMessage EncryptedMessage
     * @param key AES SecretKey
     * @return Transaction object
     */
    public static Transaction decryptTransaction(EncryptedMessage encryptedMessage, SecretKey key) {
        String plainText = decrypt(encryptedMessage, key);
        Transaction transaction = deserializeTransaction(plainText);
        LoggerUtil.info("Transaction message decrypted successfully");
        return transaction;
    }
}
