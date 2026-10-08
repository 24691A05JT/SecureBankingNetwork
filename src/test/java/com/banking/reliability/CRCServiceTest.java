package com.banking.reliability;

import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import com.banking.security.EncryptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CRCServiceTest {

    @Test
    @DisplayName("1. Known CRC-16-CCITT vector: '123456789' -> 0x29B1")
    void testKnownVectorCrc16Ccitt() {
        String testVector = "123456789";
        int expectedCrc = 0x29B1;

        int actualCrc = CRCService.calculateCRC(testVector);
        assertEquals(expectedCrc, actualCrc, "CRC-16-CCITT calculation for '123456789' must equal 0x29B1 (10673)");
        assertEquals("0x29B1", CRCService.formatCRC(actualCrc));
    }

    @Test
    @DisplayName("2. Empty input handling")
    void testEmptyInputHandling() {
        assertDoesNotThrow(() -> CRCService.calculateCRC(""));
        assertDoesNotThrow(() -> CRCService.calculateCRC(new byte[0]));
        int crc = CRCService.calculateCRC("");
        assertTrue(crc >= 0x0000 && crc <= 0xFFFF);
    }

    @Test
    @DisplayName("3. Null input handling")
    void testNullInputHandling() {
        assertThrows(IllegalArgumentException.class, () -> CRCService.calculateCRC((String) null));
        assertThrows(IllegalArgumentException.class, () -> CRCService.calculateCRC((byte[]) null));
        assertThrows(IllegalArgumentException.class, () -> CRCService.verifyCRC((String) null, 0x1234));
        assertThrows(IllegalArgumentException.class, () -> CRCService.verifyCRC((byte[]) null, 0x1234));
    }

    @Test
    @DisplayName("4. Same data produces same CRC")
    void testSameDataProducesSameCrc() {
        String data = "BANK_TRANSFER|ACC10001|ACC10002|5000.00";
        int crc1 = CRCService.calculateCRC(data);
        int crc2 = CRCService.calculateCRC(data);
        assertEquals(crc1, crc2, "Calculated CRC must be deterministic for identical input data");
    }

    @Test
    @DisplayName("5. Different data produces different CRC")
    void testDifferentDataProducesDifferentCrc() {
        String data1 = "BANK_TRANSFER|ACC10001|ACC10002|5000.00";
        String data2 = "BANK_TRANSFER|ACC10001|ACC10002|9000.00";
        int crc1 = CRCService.calculateCRC(data1);
        int crc2 = CRCService.calculateCRC(data2);
        assertNotEquals(crc1, crc2, "Different data payloads must produce distinct CRC checksums");
    }

    @Test
    @DisplayName("6. Original message verifies successfully")
    void testOriginalMessageVerificationPassed() {
        String data = "TRANSACTION_ID=TXN-1001\nAMOUNT=5000.00";
        int crc = CRCService.calculateCRC(data);
        assertTrue(CRCService.verifyCRC(data, crc), "Original message verification should pass");
    }

    @Test
    @DisplayName("7. Modified message fails verification")
    void testModifiedMessageVerificationFailed() {
        String originalData = "TRANSACTION_ID=TXN-1001\nAMOUNT=5000.00";
        String corruptedData = "TRANSACTION_ID=TXN-1001\nAMOUNT=9000.00";
        int originalCrc = CRCService.calculateCRC(originalData);

        assertFalse(CRCService.verifyCRC(corruptedData, originalCrc),
                "Verifying corrupted data against original CRC must fail");
    }

    @Test
    @DisplayName("8. Single-byte corruption is detected")
    void testSingleByteCorruptionDetected() {
        byte[] payload = "PAYMENT_INSTRUCTION_DATA".getBytes(StandardCharsets.UTF_8);
        int originalCrc = CRCService.calculateCRC(payload);

        // Corrupt single byte
        byte[] corruptedPayload = payload.clone();
        corruptedPayload[5] ^= 0x01;

        assertFalse(CRCService.verifyCRC(corruptedPayload, originalCrc),
                "CRC must detect single-byte transmission errors");
    }

    @Test
    @DisplayName("9. Multiple-byte corruption is detected")
    void testMultipleByteCorruptionDetected() {
        byte[] payload = "SECURE_FINANCIAL_MESSAGE_PAYLOAD".getBytes(StandardCharsets.UTF_8);
        int originalCrc = CRCService.calculateCRC(payload);

        byte[] corruptedPayload = payload.clone();
        corruptedPayload[0] = 'X';
        corruptedPayload[1] = 'Y';
        corruptedPayload[2] = 'Z';

        assertFalse(CRCService.verifyCRC(corruptedPayload, originalCrc),
                "CRC must detect multi-byte corruption");
    }

    @Test
    @DisplayName("10. CRC is always within unsigned 16-bit range")
    void testCrcWithinUnsigned16BitRange() {
        String[] samples = {"", "A", "123456789", "LONG_TEXT_PAYLOAD_TESTING_RANGE_CONSTRAINTS"};
        for (String sample : samples) {
            int crc = CRCService.calculateCRC(sample);
            assertTrue(crc >= 0x0000 && crc <= 0xFFFF,
                    "CRC value must stay within 0x0000 to 0xFFFF range");
        }
    }

    @Test
    @DisplayName("11. String and byte[] calculations produce equivalent results")
    void testStringAndByteArrayEquivalence() {
        String text = "EQUIVALENCE_TEST_PAYLOAD";
        int stringCrc = CRCService.calculateCRC(text);
        int byteCrc = CRCService.calculateCRC(text.getBytes(StandardCharsets.UTF_8));
        assertEquals(stringCrc, byteCrc, "String and UTF-8 byte array CRC calculations must match");
    }

    @Test
    @DisplayName("12. Banking transaction message CRC can be calculated and verified")
    void testBankingTransactionMessageCrc() {
        Transaction transaction = new Transaction("ACC10001", "ACC10002", 5000.00, TransactionType.TRANSFER, "CRC Test Wire");
        transaction.setStatus(TransactionStatus.SUCCESS);

        String serializedTxn = EncryptionService.serializeTransaction(transaction);
        CRCResult result = CRCService.process(serializedTxn);

        assertNotNull(result);
        assertNotNull(result.getCrcFormatted());
        assertTrue(result.verifyAgainst(result.getCrc()));

        // Simulate tampering
        String corruptedTxn = serializedTxn.replace("5000.00", "9000.00");
        assertFalse(result.verifyAgainst(CRCService.calculateCRC(corruptedTxn)));
    }
}
