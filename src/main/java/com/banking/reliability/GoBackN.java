package com.banking.reliability;

import com.banking.util.LoggerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Professional Go-Back-N Automatic Repeat reQuest (ARQ) reliability protocol implementation.
 * Features cumulative ACKs, receiver in-order enforcement, out-of-order frame rejection,
 * frame loss simulation, and Go-Back-N window retransmission.
 */
public class GoBackN {

    private final int totalFrames;
    private final int windowSize;
    private int base;
    private int nextSeqNum;
    private int expectedSeqNum;

    private int originalTransmissions;
    private int retransmissions;

    private final List<Frame> frames;
    private final List<String> eventLog;

    /**
     * Constructs a GoBackN ARQ engine with total frame count and window size.
     *
     * @param totalFrames total frames to transmit
     * @param windowSize sliding window capacity
     */
    public GoBackN(int totalFrames, int windowSize) {
        if (totalFrames <= 0) {
            throw new IllegalArgumentException("Total frames must be greater than zero, got: " + totalFrames);
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be greater than zero, got: " + windowSize);
        }
        this.totalFrames = totalFrames;
        this.windowSize = windowSize;
        this.base = 0;
        this.nextSeqNum = 0;
        this.expectedSeqNum = 0;
        this.originalTransmissions = 0;
        this.retransmissions = 0;
        this.frames = new ArrayList<>(totalFrames);
        this.eventLog = new ArrayList<>();

        for (int i = 0; i < totalFrames; i++) {
            this.frames.add(new Frame(i, "GBN_PAYLOAD_" + i));
        }

        logEvent("Go-Back-N ARQ initialized. Total Frames: " + totalFrames + ", Window Size: " + windowSize);
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getBase() {
        return base;
    }

    public int getNextSequenceNumber() {
        return nextSeqNum;
    }

    public int getExpectedSequenceNumber() {
        return expectedSeqNum;
    }

    public int getOriginalTransmissions() {
        return originalTransmissions;
    }

    public int getRetransmissions() {
        return retransmissions;
    }

    public int getTotalTransmissions() {
        return originalTransmissions + retransmissions;
    }

    public boolean canSend() {
        return nextSeqNum < totalFrames && (nextSeqNum < base + windowSize);
    }

    public boolean isWindowFull() {
        return (nextSeqNum - base) >= windowSize;
    }

    public boolean hasPendingFrames() {
        return base < totalFrames;
    }

    public boolean isComplete() {
        return base >= totalFrames;
    }

    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    /**
     * Transmits the next frame in sequence with a default payload.
     *
     * @return transmitted Frame
     */
    public Frame sendNextFrame() {
        if (!canSend()) {
            throw new IllegalStateException("Cannot send frame: Window is full or all frames sent.");
        }
        Frame frame = frames.get(nextSeqNum);
        return sendFrameInternal(frame);
    }

    /**
     * Transmits the next frame in sequence with a custom payload.
     *
     * @param payload payload string
     * @return transmitted Frame
     */
    public Frame sendNextFrame(String payload) {
        if (!canSend()) {
            throw new IllegalStateException("Cannot send frame: Window is full or all frames sent.");
        }
        Frame frame = frames.get(nextSeqNum);
        frame.setPayload(payload);
        return sendFrameInternal(frame);
    }

    private Frame sendFrameInternal(Frame frame) {
        originalTransmissions++;
        nextSeqNum++;

        if (frame.getStatus() == FrameStatus.LOST) {
            logEvent(String.format("Frame #%d SENT, but was LOST in transit. (Base: %d, Next: %d)",
                    frame.getSequenceNumber(), base, nextSeqNum));
            LoggerUtil.warn("Frame #" + frame.getSequenceNumber() + " lost during network transmission.");
            return frame;
        }

        frame.setStatus(FrameStatus.SENT);
        logEvent(String.format("Frame #%d SENT. (Payload: '%s', Window: [%d..%d])",
                frame.getSequenceNumber(), frame.getPayload(), base, Math.min(base + windowSize - 1, totalFrames - 1)));

        // Deliver frame to receiver logic
        processReceiver(frame);
        return frame;
    }

    /**
     * Simulates the loss of a specific frame during transmission.
     *
     * @param sequenceNumber sequence number of frame to be lost
     */
    public void simulateFrameLoss(int sequenceNumber) {
        if (sequenceNumber < 0 || sequenceNumber >= totalFrames) {
            throw new IllegalArgumentException("Invalid sequence number for frame loss: " + sequenceNumber);
        }
        Frame frame = frames.get(sequenceNumber);
        frame.setStatus(FrameStatus.LOST);
        logEvent(String.format("SIMULATION: Frame #%d set to LOST state.", sequenceNumber));
        LoggerUtil.warn(String.format("Frame #%d simulated as LOST in network.", sequenceNumber));
    }

    /**
     * Receiver processing logic for incoming frame according to Go-Back-N rules.
     * Accepts frame ONLY if sequence number matches expectedSequenceNumber.
     * Discards out-of-order frames.
     *
     * @param frame received frame
     * @return Acknowledgement
     */
    public Acknowledgement processReceiver(Frame frame) {
        if (frame == null) {
            throw new IllegalArgumentException("Frame cannot be null");
        }

        int seq = frame.getSequenceNumber();

        if (seq == expectedSeqNum) {
            // In-order frame received: ACCEPT
            frame.setStatus(FrameStatus.ACKNOWLEDGED);
            expectedSeqNum++;
            base = expectedSeqNum; // Cumulative ACK advances sender base

            logEvent(String.format("Receiver: Frame #%d ACCEPTED. Cumulative ACK #%d issued. Base advanced to %d.",
                    seq, seq, base));
            LoggerUtil.info(String.format("Receiver ACCEPTED Frame #%d. Base advanced to %d.", seq, base));
            return new Acknowledgement(seq, true, "Cumulative ACK " + seq);
        } else {
            // Out-of-order frame received: DISCARD
            logEvent(String.format("Receiver: Out-of-order Frame #%d DISCARDED (Expected: #%d). Sending duplicate ACK #%d.",
                    seq, expectedSeqNum, expectedSeqNum - 1));
            LoggerUtil.warn(String.format("Receiver DISCARDED out-of-order Frame #%d (Expected #%d).", seq, expectedSeqNum));
            int lastAck = Math.max(-1, expectedSeqNum - 1);
            return new Acknowledgement(lastAck >= 0 ? lastAck : 0, false, "Duplicate ACK for expected #" + expectedSeqNum);
        }
    }

    /**
     * Handles timeout for unacknowledged frames at the sender base.
     * Retransmits the base frame AND ALL subsequent unacknowledged frames currently in the window.
     *
     * @return number of frames retransmitted
     */
    public int handleTimeout() {
        if (base >= totalFrames || base >= nextSeqNum) {
            return 0;
        }

        logEvent(String.format("TIMEOUT at sender base #%d. Triggering Go-Back-N window retransmission.", base));
        LoggerUtil.warn(String.format("Timeout detected at base #%d. Retransmitting window frames...", base));

        int retransmittedCount = 0;
        int end = Math.min(nextSeqNum, totalFrames);

        for (int i = base; i < end; i++) {
            Frame frame = frames.get(i);
            frame.setStatus(FrameStatus.RETRANSMITTED);
            retransmissions++;
            retransmittedCount++;

            logEvent(String.format("Go-Back-N Retransmitting Frame #%d (Payload: '%s')",
                    frame.getSequenceNumber(), frame.getPayload()));
            LoggerUtil.info(String.format("Retransmitting Frame #%d...", frame.getSequenceNumber()));

            // Deliver retransmitted frame to receiver
            processReceiver(frame);
        }

        return retransmittedCount;
    }

    public Frame getFrame(int sequenceNumber) {
        if (sequenceNumber < 0 || sequenceNumber >= totalFrames) {
            throw new IllegalArgumentException("Sequence number out of bounds: " + sequenceNumber);
        }
        return frames.get(sequenceNumber);
    }

    public List<Frame> getAllFrames() {
        return Collections.unmodifiableList(frames);
    }

    public List<Frame> getOutstandingFrames() {
        return frames.stream()
                .filter(f -> f.getSequenceNumber() >= base && f.getSequenceNumber() < nextSeqNum)
                .filter(f -> f.getStatus() != FrameStatus.ACKNOWLEDGED)
                .collect(Collectors.toList());
    }

    public String getFormattedState() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Sender Base: %d | NextSeqNum: %d | Receiver Expected: %d | WindowSize: %d | TotalFrames: %d%n",
                base, nextSeqNum, expectedSeqNum, windowSize, totalFrames));
        sb.append(String.format("Transmissions: %d (Original: %d, Retransmissions: %d)%n",
                getTotalTransmissions(), originalTransmissions, retransmissions));
        sb.append("Frame States: ");

        int end = Math.min(base + windowSize, totalFrames);
        for (int i = 0; i < totalFrames; i++) {
            Frame f = frames.get(i);
            if (i >= base && i < end) {
                sb.append(String.format("[%d:%s] ", f.getSequenceNumber(), f.getStatus()));
            } else {
                sb.append(String.format("%d:%s ", f.getSequenceNumber(), f.getStatus()));
            }
        }
        return sb.toString().trim();
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }
}
