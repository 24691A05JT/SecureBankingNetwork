package com.banking.reliability;

import com.banking.util.LoggerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Professional Sliding Window protocol flow control implementation for reliable banking frame transmission.
 */
public class SlidingWindow {

    private final int totalFrames;
    private final int windowSize;
    private int base;
    private int nextSeqNum;
    private final List<Frame> frames;

    /**
     * Constructs a SlidingWindow with the specified total frame count and window size.
     *
     * @param totalFrames total number of frames to transmit
     * @param windowSize maximum number of outstanding unacknowledged frames
     */
    public SlidingWindow(int totalFrames, int windowSize) {
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
        this.frames = new ArrayList<>(totalFrames);

        for (int i = 0; i < totalFrames; i++) {
            this.frames.add(new Frame(i, "FRAME_PAYLOAD_" + i));
        }
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

    public int getNextSeqNum() {
        return nextSeqNum;
    }

    /**
     * Checks whether the sender can transmit a new frame within the current window bounds.
     *
     * @return true if nextSeqNum < totalFrames and nextSeqNum < base + windowSize
     */
    public boolean canSend() {
        return nextSeqNum < totalFrames && (nextSeqNum < base + windowSize);
    }

    /**
     * Checks if the window is currently full (number of outstanding frames equals window size).
     *
     * @return true if nextSeqNum >= base + windowSize
     */
    public boolean isWindowFull() {
        return (nextSeqNum - base) >= windowSize;
    }

    /**
     * Checks if there are pending unacknowledged frames remaining.
     *
     * @return true if base < totalFrames
     */
    public boolean hasPendingFrames() {
        return base < totalFrames;
    }

    /**
     * Checks if all frames have been transmitted and acknowledged.
     *
     * @return true if base >= totalFrames
     */
    public boolean isComplete() {
        return base >= totalFrames;
    }

    /**
     * Transmits the next available frame in the window.
     *
     * @return transmitted Frame
     */
    public Frame sendNextFrame() {
        if (!canSend()) {
            throw new IllegalStateException("Cannot send frame: Window is full or all frames have been sent.");
        }
        Frame frame = frames.get(nextSeqNum);
        frame.setStatus(FrameStatus.SENT);
        LoggerUtil.info(String.format("Frame #%d sent. (Window: [%d..%d])",
                frame.getSequenceNumber(), base, Math.min(base + windowSize - 1, totalFrames - 1)));
        nextSeqNum++;
        return frame;
    }

    /**
     * Transmits the next available frame with a custom payload.
     *
     * @param payload custom payload string
     * @return transmitted Frame
     */
    public Frame sendNextFrame(String payload) {
        if (!canSend()) {
            throw new IllegalStateException("Cannot send frame: Window is full or all frames have been sent.");
        }
        Frame frame = frames.get(nextSeqNum);
        frame.setPayload(payload);
        frame.setStatus(FrameStatus.SENT);
        LoggerUtil.info(String.format("Frame #%d sent with payload: '%s'. (Window: [%d..%d])",
                frame.getSequenceNumber(), payload, base, Math.min(base + windowSize - 1, totalFrames - 1)));
        nextSeqNum++;
        return frame;
    }

    /**
     * Simulates the loss of a frame during transmission.
     *
     * @param sequenceNumber frame sequence number
     */
    public void simulateFrameLoss(int sequenceNumber) {
        if (sequenceNumber < 0 || sequenceNumber >= totalFrames) {
            throw new IllegalArgumentException("Invalid sequence number for frame loss: " + sequenceNumber);
        }
        Frame frame = frames.get(sequenceNumber);
        frame.setStatus(FrameStatus.LOST);
        LoggerUtil.warn(String.format("Frame #%d LOST during transmission simulation.", sequenceNumber));
    }

    /**
     * Processes an Acknowledgement for a frame sequence number.
     * Advances the window base if the base frame is acknowledged.
     *
     * @param sequenceNumber acknowledged sequence number
     * @return true if ACK was valid and accepted, false otherwise
     */
    public boolean receiveAcknowledgement(int sequenceNumber) {
        if (sequenceNumber < 0 || sequenceNumber >= totalFrames) {
            LoggerUtil.warn(String.format("Invalid ACK #%d received (Out of bounds). Ignored.", sequenceNumber));
            return false;
        }

        if (sequenceNumber < base) {
            LoggerUtil.warn(String.format("Outdated/Duplicate ACK #%d received (Base: %d). Ignored.", sequenceNumber, base));
            return false;
        }

        Frame frame = frames.get(sequenceNumber);
        if (frame.getStatus() == FrameStatus.ACKNOWLEDGED) {
            LoggerUtil.warn(String.format("Duplicate ACK #%d received. Ignored.", sequenceNumber));
            return false;
        }

        frame.setStatus(FrameStatus.ACKNOWLEDGED);
        LoggerUtil.info(String.format("ACK #%d received successfully.", sequenceNumber));

        // Slide window forward for consecutive ACKed frames starting from base
        int oldBase = base;
        while (base < totalFrames && frames.get(base).getStatus() == FrameStatus.ACKNOWLEDGED) {
            base++;
        }

        if (base > oldBase) {
            LoggerUtil.info(String.format("Sliding window base advanced from #%d to #%d.", oldBase, base));
        }

        return true;
    }

    /**
     * Processes an Acknowledgement object.
     *
     * @param ack Acknowledgement object
     * @return true if accepted, false otherwise
     */
    public boolean receiveAcknowledgement(Acknowledgement ack) {
        if (ack == null) {
            throw new IllegalArgumentException("Acknowledgement cannot be null");
        }
        if (!ack.isAcknowledged()) {
            return false;
        }
        return receiveAcknowledgement(ack.getSequenceNumber());
    }

    /**
     * Gets a specific Frame by sequence number.
     *
     * @param sequenceNumber sequence number
     * @return Frame
     */
    public Frame getFrame(int sequenceNumber) {
        if (sequenceNumber < 0 || sequenceNumber >= totalFrames) {
            throw new IllegalArgumentException("Frame sequence number out of bounds: " + sequenceNumber);
        }
        return frames.get(sequenceNumber);
    }

    /**
     * Returns an unmodifiable list of all frames.
     *
     * @return list of frames
     */
    public List<Frame> getAllFrames() {
        return Collections.unmodifiableList(frames);
    }

    /**
     * Returns frames currently within the active window range [base .. min(base + windowSize - 1, totalFrames - 1)].
     *
     * @return list of active window frames
     */
    public List<Frame> getWindowFrames() {
        if (base >= totalFrames) {
            return Collections.emptyList();
        }
        int end = Math.min(base + windowSize, totalFrames);
        return Collections.unmodifiableList(frames.subList(base, end));
    }

    /**
     * Returns sent frames that are currently outstanding (unacknowledged).
     *
     * @return list of outstanding frames
     */
    public List<Frame> getOutstandingFrames() {
        return frames.stream()
                .filter(f -> f.getSequenceNumber() >= base && f.getSequenceNumber() < nextSeqNum)
                .filter(f -> f.getStatus() != FrameStatus.ACKNOWLEDGED)
                .collect(Collectors.toList());
    }

    /**
     * Formats the current sliding window state into a clean readable string visualization.
     *
     * @return formatted state string
     */
    public String getFormattedWindowState() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Base: %d | NextSeqNum: %d | WindowSize: %d | TotalFrames: %d%n",
                base, nextSeqNum, windowSize, totalFrames));
        sb.append("Window Frames: ");

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
}
