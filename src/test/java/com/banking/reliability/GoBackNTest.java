package com.banking.reliability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoBackNTest {

    @Test
    @DisplayName("1. Go-Back-N initializes correctly")
    void testGoBackNInitialization() {
        GoBackN gbn = new GoBackN(8, 4);
        assertEquals(8, gbn.getTotalFrames());
        assertEquals(4, gbn.getWindowSize());
        assertEquals(0, gbn.getBase());
        assertEquals(0, gbn.getNextSequenceNumber());
        assertEquals(0, gbn.getExpectedSequenceNumber());
        assertEquals(0, gbn.getOriginalTransmissions());
        assertEquals(0, gbn.getRetransmissions());
        assertEquals(0, gbn.getTotalTransmissions());
        assertFalse(gbn.isComplete());
        assertTrue(gbn.hasPendingFrames());
    }

    @Test
    @DisplayName("2. Input validation for parameters")
    void testInputValidation() {
        assertThrows(IllegalArgumentException.class, () -> new GoBackN(0, 4));
        assertThrows(IllegalArgumentException.class, () -> new GoBackN(8, 0));
        assertThrows(IllegalArgumentException.class, () -> new GoBackN(-1, -1));
        assertThrows(IllegalArgumentException.class, () -> new GoBackN(8, 4).processReceiver(null));
    }

    @Test
    @DisplayName("3. In-order frame delivery accepts frames and advances sequence numbers")
    void testInOrderFrameDelivery() {
        GoBackN gbn = new GoBackN(5, 3);
        Frame f0 = gbn.sendNextFrame("PAYLOAD_0");

        assertEquals(FrameStatus.ACKNOWLEDGED, f0.getStatus());
        assertEquals(1, gbn.getBase());
        assertEquals(1, gbn.getExpectedSequenceNumber());

        Frame f1 = gbn.sendNextFrame("PAYLOAD_1");
        assertEquals(FrameStatus.ACKNOWLEDGED, f1.getStatus());
        assertEquals(2, gbn.getBase());
        assertEquals(2, gbn.getExpectedSequenceNumber());
    }

    @Test
    @DisplayName("4. Out-of-order frame is rejected by receiver")
    void testOutOfOrderFrameRejection() {
        GoBackN gbn = new GoBackN(5, 3);

        // Expecting 0, but process frame 2 directly
        Frame f2 = gbn.getFrame(2);
        f2.setStatus(FrameStatus.SENT);

        Acknowledgement ack = gbn.processReceiver(f2);
        assertFalse(ack.isAcknowledged());
        assertNotEquals(FrameStatus.ACKNOWLEDGED, f2.getStatus());
        assertEquals(0, gbn.getBase(), "Base should not advance on out-of-order frame");
        assertEquals(0, gbn.getExpectedSequenceNumber(), "Expected sequence number should remain 0");
    }

    @Test
    @DisplayName("5. Cumulative ACK advances sender base correctly")
    void testCumulativeAckAdvancesBase() {
        GoBackN gbn = new GoBackN(6, 4);

        gbn.sendNextFrame(); // Frame 0 -> ACK 0 -> Base 1
        gbn.sendNextFrame(); // Frame 1 -> ACK 1 -> Base 2

        assertEquals(2, gbn.getBase());
        assertEquals(2, gbn.getExpectedSequenceNumber());
    }

    @Test
    @DisplayName("6. Lost frame causes out-of-order rejection for subsequent frames")
    void testLostFrameCausesOutOfOrderRejection() {
        GoBackN gbn = new GoBackN(6, 4);

        gbn.sendNextFrame(); // Frame 0 -> delivered
        gbn.simulateFrameLoss(1); // Frame 1 marked LOST

        gbn.sendNextFrame(); // Frame 1 -> lost
        assertEquals(1, gbn.getBase(), "Base should be 1");

        gbn.sendNextFrame(); // Frame 2 -> rejected because receiver expects 1!
        assertEquals(1, gbn.getBase(), "Base should stay 1 because Frame 2 was discarded");
        assertEquals(1, gbn.getExpectedSequenceNumber(), "Expected sequence number should stay 1");
    }

    @Test
    @DisplayName("7. Timeout retransmits base frame AND all subsequent unacknowledged frames")
    void testTimeoutRetransmitsWindow() {
        GoBackN gbn = new GoBackN(6, 4);

        gbn.sendNextFrame(); // Frame 0 -> ACKed, Base=1
        gbn.simulateFrameLoss(1);

        gbn.sendNextFrame(); // Frame 1 -> LOST
        gbn.sendNextFrame(); // Frame 2 -> DISCARDED

        assertEquals(1, gbn.getBase());
        assertEquals(3, gbn.getNextSequenceNumber()); // Sent 0, 1, 2

        // Retransmit window (Frames 1 and 2)
        int retransmittedCount = gbn.handleTimeout();
        assertEquals(2, retransmittedCount, "Should retransmit both Frame 1 and Frame 2");

        assertEquals(3, gbn.getBase(), "Base should advance to 3 after retransmitting Frames 1 and 2");
        assertEquals(3, gbn.getExpectedSequenceNumber());
        assertEquals(2, gbn.getRetransmissions());
    }

    @Test
    @DisplayName("8. Retransmission counters increment accurately")
    void testRetransmissionCounters() {
        GoBackN gbn = new GoBackN(5, 3);

        gbn.sendNextFrame(); // Original 1
        gbn.sendNextFrame(); // Original 2

        assertEquals(2, gbn.getOriginalTransmissions());
        assertEquals(0, gbn.getRetransmissions());
        assertEquals(2, gbn.getTotalTransmissions());

        gbn.simulateFrameLoss(2);
        gbn.sendNextFrame(); // Original 3 (Frame 2 lost)

        gbn.handleTimeout(); // Retransmits Frame 2
        assertTrue(gbn.getRetransmissions() > 0);
        assertEquals(gbn.getOriginalTransmissions() + gbn.getRetransmissions(), gbn.getTotalTransmissions());
    }

    @Test
    @DisplayName("9. Entire transmission completes successfully when all frames acknowledged")
    void testEntireTransmissionCompletes() {
        int total = 5;
        GoBackN gbn = new GoBackN(total, 3);

        while (gbn.canSend()) {
            gbn.sendNextFrame();
        }

        assertTrue(gbn.isComplete());
        assertEquals(total, gbn.getBase());
        assertFalse(gbn.hasPendingFrames());
    }

    @Test
    @DisplayName("10. Event log records transmission events")
    void testEventLogRecordsEvents() {
        GoBackN gbn = new GoBackN(4, 2);
        gbn.sendNextFrame();

        List<String> log = gbn.getEventLog();
        assertNotNull(log);
        assertFalse(log.isEmpty(), "Event log should record initialization and transmission events");
    }

    @Test
    @DisplayName("11. End-to-end Go-Back-N loss recovery simulation")
    void testEndToEndLossRecovery() {
        GoBackN gbn = new GoBackN(6, 3);

        // Send frame 0
        gbn.sendNextFrame("TXN_HEADER");
        assertEquals(1, gbn.getBase());

        // Simulate loss of frame 1
        gbn.simulateFrameLoss(1);
        gbn.sendNextFrame("TXN_BODY_1"); // Frame 1 lost
        gbn.sendNextFrame("TXN_BODY_2"); // Frame 2 discarded

        assertEquals(1, gbn.getBase());

        // Trigger Go-Back-N retransmission
        gbn.handleTimeout();
        assertEquals(3, gbn.getBase());

        // Send remaining frames
        while (gbn.canSend()) {
            gbn.sendNextFrame("TXN_FOOTER");
        }

        assertTrue(gbn.isComplete());
    }
}
