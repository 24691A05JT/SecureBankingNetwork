package com.banking.integration;

/**
 * Configuration options for controlling execution flow and error simulations in SecureBankingPipeline.
 */
public class PipelineOptions {

    private boolean simulateCrcCorruption;
    private boolean simulateDecryptionFailure;
    private boolean simulateTcpFailure;
    private boolean simulateFrameLoss;

    public PipelineOptions() {
        this.simulateCrcCorruption = false;
        this.simulateDecryptionFailure = false;
        this.simulateTcpFailure = false;
        this.simulateFrameLoss = false;
    }

    public static PipelineOptions defaultOptions() {
        return new PipelineOptions();
    }

    public static PipelineOptions withCrcCorruption() {
        PipelineOptions opts = new PipelineOptions();
        opts.setSimulateCrcCorruption(true);
        return opts;
    }

    public static PipelineOptions withDecryptionFailure() {
        PipelineOptions opts = new PipelineOptions();
        opts.setSimulateDecryptionFailure(true);
        return opts;
    }

    public static PipelineOptions withTcpFailure() {
        PipelineOptions opts = new PipelineOptions();
        opts.setSimulateTcpFailure(true);
        return opts;
    }

    public static PipelineOptions withFrameLoss() {
        PipelineOptions opts = new PipelineOptions();
        opts.setSimulateFrameLoss(true);
        return opts;
    }

    public boolean isSimulateCrcCorruption() {
        return simulateCrcCorruption;
    }

    public void setSimulateCrcCorruption(boolean simulateCrcCorruption) {
        this.simulateCrcCorruption = simulateCrcCorruption;
    }

    public boolean isSimulateDecryptionFailure() {
        return simulateDecryptionFailure;
    }

    public void setSimulateDecryptionFailure(boolean simulateDecryptionFailure) {
        this.simulateDecryptionFailure = simulateDecryptionFailure;
    }

    public boolean isSimulateTcpFailure() {
        return simulateTcpFailure;
    }

    public void setSimulateTcpFailure(boolean simulateTcpFailure) {
        this.simulateTcpFailure = simulateTcpFailure;
    }

    public boolean isSimulateFrameLoss() {
        return simulateFrameLoss;
    }

    public void setSimulateFrameLoss(boolean simulateFrameLoss) {
        this.simulateFrameLoss = simulateFrameLoss;
    }
}
