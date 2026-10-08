package com.banking.security;

import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = EncryptionService.generateKey();
    }

    @Test
    @DisplayName("1. Key generation succeeds")
    void testKeyGenerationSucceeds() {
        SecretKey key = EncryptionService.generateKey();
        assertNotNull(key, "Generated key should not be null");
    }

    @Test
    @DisplayName("2. Generated key uses AES and is 256 bits")
    void testGeneratedKeyAlgorithmAndSize() {
        SecretKey key = EncryptionService.generateKey();
        assertEquals("AES", key.getAlgorithm(), "Key algorithm should be AES");
        assertEquals(32, key.getEncoded().length, "AES key size should be 32 bytes (256 bits)");
    }

    @Test
    @DisplayName("3. Plain text can be encrypted")
    void testPlainTextCanBeEncrypted() {
        String plainText = "CONFIDENTIAL_BANKING_DATA";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);
        assertNotNull(msg, "EncryptedMessage should not be null");
        assertNotNull(msg.getEncryptedData(), "Encrypted data string should not be null");
        assertNotNull(msg.getInitializationVector(), "IV string should not be null");
    }

    @Test
    @DisplayName("4. Encrypted output is not equal to plain text")
    void testEncryptedOutputNotEqualToPlainText() {
        String plainText = "SENSITIVE_ACCOUNT_BALANCE";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);
        assertNotEquals(plainText, msg.getEncryptedData(), "Encrypted data should not equal plain text");
    }

    @Test
    @DisplayName("5. Encrypted data can be decrypted back to original text")
    void testRoundTripEncryptionDecryption() {
        String plainText = "TRANSFER_AMOUNT=5000.00";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);
        String decrypted = EncryptionService.decrypt(msg, secretKey);
        assertEquals(plainText, decrypted, "Decrypted text must match original plain text");
    }

    @Test
    @DisplayName("6. Encrypting same plaintext twice produces different ciphertext due to random IVs")
    void testDifferentCiphertextForSamePlainText() {
        String plainText = "DUPLICATE_PAYMENT_REQUEST";
        EncryptedMessage msg1 = EncryptionService.encrypt(plainText, secretKey);
        EncryptedMessage msg2 = EncryptionService.encrypt(plainText, secretKey);
        assertNotEquals(msg1.getEncryptedData(), msg2.getEncryptedData(),
                "Two encryptions of the same plaintext must produce different ciphertext");
    }

    @Test
    @DisplayName("7. Each encryption generates a different IV")
    void testDifferentIvGeneratedPerEncryption() {
        String plainText = "TEST_PAYLOAD";
        EncryptedMessage msg1 = EncryptionService.encrypt(plainText, secretKey);
        EncryptedMessage msg2 = EncryptionService.encrypt(plainText, secretKey);
        assertNotEquals(msg1.getInitializationVector(), msg2.getInitializationVector(),
                "Each encryption operation must generate a distinct random IV");
    }

    @Test
    @DisplayName("8. Wrong key cannot decrypt the message")
    void testWrongKeyFailsDecryption() {
        SecretKey wrongKey = EncryptionService.generateKey();
        String plainText = "TOP_SECRET_WIRE_TRANSFER";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);

        assertThrows(SecurityException.class, () -> EncryptionService.decrypt(msg, wrongKey),
                "Decrypting with a wrong key must throw SecurityException");
    }

    @Test
    @DisplayName("9. Modified/tampered ciphertext cannot be decrypted successfully")
    void testTamperedCiphertextFailsDecryption() {
        String plainText = "AUTHENTICATED_TRANSACTION_PAYLOAD";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);

        byte[] cipherBytes = msg.getEncryptedBytes();
        cipherBytes[0] ^= 0xFF; // Flip bits in ciphertext
        EncryptedMessage tamperedMsg = new EncryptedMessage(cipherBytes, msg.getIvBytes());

        assertThrows(SecurityException.class, () -> EncryptionService.decrypt(tamperedMsg, secretKey),
                "Tampered ciphertext must cause GCM authentication failure");
    }

    @Test
    @DisplayName("10. Modified IV cannot decrypt successfully")
    void testTamperedIvFailsDecryption() {
        String plainText = "AUTHENTICATED_TRANSACTION_PAYLOAD";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);

        byte[] ivBytes = msg.getIvBytes();
        ivBytes[0] ^= 0xFF; // Flip bits in IV
        EncryptedMessage tamperedIvMsg = new EncryptedMessage(msg.getEncryptedBytes(), ivBytes);

        assertThrows(SecurityException.class, () -> EncryptionService.decrypt(tamperedIvMsg, secretKey),
                "Tampered IV must cause GCM decryption/authentication failure");
    }

    @Test
    @DisplayName("11. Empty plaintext behavior is handled correctly")
    void testEmptyPlaintextHandling() {
        String plainText = "";
        EncryptedMessage msg = EncryptionService.encrypt(plainText, secretKey);
        assertNotNull(msg);
        String decrypted = EncryptionService.decrypt(msg, secretKey);
        assertEquals("", decrypted, "Empty plaintext should round-trip correctly");
    }

    @Test
    @DisplayName("12. Null input behavior is handled correctly")
    void testNullInputHandling() {
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.encrypt(null, secretKey));
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.encrypt("TEXT", null));
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.decrypt(null, secretKey));
        assertThrows(IllegalArgumentException.class, () -> EncryptionService.decrypt(new EncryptedMessage("data", "iv"), null));
    }

    @Test
    @DisplayName("13. Sample banking transaction can be serialized and encrypted")
    void testBankingTransactionSerializationAndEncryption() {
        Transaction transaction = new Transaction("ACC10001", "ACC10002", 5000.00, TransactionType.TRANSFER, "Day 6 Test Wire");
        transaction.setStatus(TransactionStatus.SUCCESS);

        EncryptedMessage msg = EncryptionService.encryptTransaction(transaction, secretKey);
        assertNotNull(msg);
        assertNotNull(msg.getEncryptedData());
        assertNotNull(msg.getInitializationVector());
    }

    @Test
    @DisplayName("14. Encrypted transaction can be decrypted back to original transaction message")
    void testEncryptedTransactionRoundTrip() {
        Transaction originalTxn = new Transaction("ACC10001", "ACC10002", 5000.00, TransactionType.TRANSFER, "Day 6 Test Wire");
        originalTxn.setStatus(TransactionStatus.SUCCESS);

        EncryptedMessage msg = EncryptionService.encryptTransaction(originalTxn, secretKey);
        Transaction decryptedTxn = EncryptionService.decryptTransaction(msg, secretKey);

        assertNotNull(decryptedTxn);
        assertEquals(originalTxn.getTransactionId(), decryptedTxn.getTransactionId());
        assertEquals(originalTxn.getSourceAccount(), decryptedTxn.getSourceAccount());
        assertEquals(originalTxn.getDestinationAccount(), decryptedTxn.getDestinationAccount());
        assertEquals(originalTxn.getAmount(), decryptedTxn.getAmount(), 0.001);
        assertEquals(originalTxn.getTransactionType(), decryptedTxn.getTransactionType());
        assertEquals(originalTxn.getStatus(), decryptedTxn.getStatus());
    }
}
