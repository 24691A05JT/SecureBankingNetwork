package com.banking.reliability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowTest {

    @Test
    @DisplayName("1. Window initializes correctly")
    void testWindowInitialization() {
        SlidingWindow window = new SlidingWindow(8, 4);
        assertEquals(8, window.getTotalFrames());
        assertEquals(4, window.getWindowSize());
        assertEquals(0, window.getBase());
        assertEquals(0, window.getNextSequenceNumber());
        assertFalse(window.isComplete());
        assertTrue(window.hasPendingFrames());
    }

    @Test
    @DisplayName("2. Window size validation")
    void testWindowSizeValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindow(8, 0));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindow(8, -1));
    }

    @Test
    @DisplayName("3. Total frame count validation")
    void testTotalFrameCountValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindow(0, 4));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindow(-5, 4));
    }

    @Test
    @DisplayName("4. Initial base is 0")
    void testInitialBaseIsZero() {
        SlidingWindow window = new SlidingWindow(10, 3);
        assertEquals(0, window.getBase());
    }

    @Test
    @DisplayName("5. First frames are available for transmission")
    void testFirstFramesAvailable() {
        SlidingWindow window = new SlidingWindow(5, 3);
        assertTrue(window.canSend(), "Sender should be able to send initial frame in window");
        assertFalse(window.isWindowFull(), "Window should not be full initially");
    }

    @Test
    @DisplayName("6. Sender cannot exceed window size")
    void testSenderCannotExceedWindowSize() {
        SlidingWindow window = new SlidingWindow(6, 3);
        window.sendNextFrame(); // Frame 0
        window.sendNextFrame(); // Frame 1
        window.sendNextFrame(); // Frame 2

        assertTrue(window.isWindowFull(), "Window should be full after sending windowSize frames");
        assertFalse(window.canSend(), "canSend() should return false when window is full");
        assertThrows(IllegalStateException.class, window::sendNextFrame,
                "sendNextFrame() should throw IllegalStateException when window is full");
    }

    @Test
    @DisplayName("7. ACK moves window forward")
    void testAckMovesWindowForward() {
        SlidingWindow window = new SlidingWindow(6, 3);
        window.sendNextFrame(); // 0
        window.sendNextFrame(); // 1

        assertEquals(0, window.getBase());
        boolean ackResult = window.receiveAcknowledgement(0);

        assertTrue(ackResult);
        assertEquals(1, window.getBase(), "Window base should advance to 1 after receiving ACK 0");
    }

    @Test
    @DisplayName("8. Multiple sequential ACKs move window correctly")
    void testMultipleSequentialAcks() {
        SlidingWindow window = new SlidingWindow(8, 4);
        window.sendNextFrame(); // 0
        window.sendNextFrame(); // 1
        window.sendNextFrame(); // 2

        window.receiveAcknowledgement(0);
        window.receiveAcknowledgement(1);
        window.receiveAcknowledgement(2);

        assertEquals(3, window.getBase(), "Window base should advance to 3 after receiving ACKs 0, 1, 2");
    }

    @Test
    @DisplayName("9. New frames become available after ACKs")
    void testNewFramesAvailableAfterAck() {
        SlidingWindow window = new SlidingWindow(8, 3);
        window.sendNextFrame(); // 0
        window.sendNextFrame(); // 1
        window.sendNextFrame(); // 2

        assertFalse(window.canSend());

        window.receiveAcknowledgement(0); // Window slides from [0..2] to [1..3]
        assertTrue(window.canSend(), "Sending should be enabled after window slides forward");

        Frame frame3 = window.sendNextFrame();
        assertEquals(3, frame3.getSequenceNumber());
    }

    @Test
    @DisplayName("10. Duplicate ACK does not corrupt state")
    void testDuplicateAckDoesNotCorruptState() {
        SlidingWindow window = new SlidingWindow(5, 3);
        window.sendNextFrame(); // 0
        window.receiveAcknowledgement(0);

        assertEquals(1, window.getBase());

        boolean dupResult = window.receiveAcknowledgement(0);
        assertFalse(dupResult, "Duplicate ACK should be rejected");
        assertEquals(1, window.getBase(), "Window base should remain unchanged on duplicate ACK");
    }

    @Test
    @DisplayName("11. Invalid ACK is handled safely")
    void testInvalidAckHandledSafely() {
        SlidingWindow window = new SlidingWindow(5, 3);
        window.sendNextFrame(); // 0

        assertFalse(window.receiveAcknowledgement(-1), "Negative ACK should be ignored");
        assertFalse(window.receiveAcknowledgement(99), "Out-of-bounds ACK should be ignored");
        assertEquals(0, window.getBase());
    }

    @Test
    @DisplayName("12. Last window handles fewer frames correctly")
    void testLastWindowFewerFrames() {
        SlidingWindow window = new SlidingWindow(5, 3);
        // Transmit and ACK 0, 1, 2
        for (int i = 0; i < 3; i++) {
            window.sendNextFrame();
            window.receiveAcknowledgement(i);
        }

        assertEquals(3, window.getBase());
        List<Frame> windowFrames = window.getWindowFrames();
        assertEquals(2, windowFrames.size(), "Last window should contain remaining 2 frames (3, 4)");
        assertEquals(3, windowFrames.get(0).getSequenceNumber());
        assertEquals(4, windowFrames.get(1).getSequenceNumber());
    }

    @Test
    @DisplayName("13. Lost frame remains outstanding")
    void testLostFrameRemainsOutstanding() {
        SlidingWindow window = new SlidingWindow(5, 3);
        window.sendNextFrame(); // 0
        window.sendNextFrame(); // 1
        window.sendNextFrame(); // 2

        window.simulateFrameLoss(1); // Frame 1 lost

        window.receiveAcknowledgement(0); // ACK 0
        assertEquals(1, window.getBase(), "Base should stop at unacknowledged lost Frame 1");

        List<Frame> outstanding = window.getOutstandingFrames();
        assertTrue(outstanding.stream().anyMatch(f -> f.getSequenceNumber() == 1 && f.getStatus() == FrameStatus.LOST),
                "Lost Frame 1 should remain in outstanding list");
    }

    @Test
    @DisplayName("14. Pending frames are tracked correctly")
    void testPendingFramesTrackedCorrectly() {
        SlidingWindow window = new SlidingWindow(3, 2);
        assertTrue(window.hasPendingFrames());

        window.sendNextFrame(); // 0
        window.receiveAcknowledgement(0);
        assertTrue(window.hasPendingFrames());

        window.sendNextFrame(); // 1
        window.receiveAcknowledgement(1);
        assertTrue(window.hasPendingFrames());

        window.sendNextFrame(); // 2
        window.receiveAcknowledgement(2);
        assertFalse(window.hasPendingFrames());
    }

    @Test
    @DisplayName("15. Complete transmission reaches expected final state")
    void testCompleteTransmission() {
        int total = 5;
        int windowSize = 3;
        SlidingWindow window = new SlidingWindow(total, windowSize);

        for (int i = 0; i < total; i++) {
            assertTrue(window.canSend());
            window.sendNextFrame();
            window.receiveAcknowledgement(i);
        }

        assertTrue(window.isComplete(), "Transmission must be complete after ACKing all frames");
        assertEquals(total, window.getBase(), "Final base must equal total frame count");
        assertFalse(window.hasPendingFrames());
    }
}
