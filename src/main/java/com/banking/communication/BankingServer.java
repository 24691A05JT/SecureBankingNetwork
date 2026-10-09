package com.banking.communication;

import com.banking.util.LoggerUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Professional TCP Server for the Secure Banking Network using length-prefixed framing.
 * Listens on localhost only and handles client requests safely.
 */
public class BankingServer {

    public static final int MAX_MESSAGE_SIZE = 1_048_576; // 1 MB limit
    private static final String DEFAULT_HOST = "127.0.0.1";

    private final int configuredPort;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running;

    /**
     * Constructs a BankingServer on an automatically assigned available port (port 0).
     */
    public BankingServer() {
        this(0);
    }

    /**
     * Constructs a BankingServer on a specified port.
     *
     * @param port port number (0 for auto-assigned port)
     */
    public BankingServer(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        this.configuredPort = port;
        this.running = false;
    }

    /**
     * Starts the TCP Server in a dedicated background thread.
     */
    public synchronized void start() {
        if (running) {
            return;
        }

        try {
            serverSocket = new ServerSocket(configuredPort, 50, InetAddress.getByName(DEFAULT_HOST));
            running = true;

            LoggerUtil.info("Banking TCP Server started on " + DEFAULT_HOST + ":" + getPort());
            LoggerUtil.info("TCP_CAPTURE_TARGET=" + DEFAULT_HOST + ":" + getPort());

            serverThread = new Thread(this::listenLoop, "BankingServer-Loop");
            serverThread.setDaemon(true);
            serverThread.start();
        } catch (IOException e) {
            running = false;
            LoggerUtil.error("Failed to start Banking TCP Server: " + e.getMessage());
            throw new RuntimeException("Could not start Banking TCP Server", e);
        }
    }

    private void listenLoop() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                if (!running || serverSocket.isClosed()) {
                    break; // Server stopped cleanly
                }
                LoggerUtil.warn("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket socket) {
        LoggerUtil.info("TCP Server accepted connection from " + socket.getRemoteSocketAddress());
        try (Socket clientSocket = socket;
             DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            clientSocket.setSoTimeout(5000); // 5 second socket timeout

            // Read 4-byte length prefix
            int length = in.readInt();
            if (length <= 0 || length > MAX_MESSAGE_SIZE) {
                LoggerUtil.warn("Server received invalid payload length: " + length);
                sendResponse(out, "BANKING_TRANSACTION_RESPONSE|STATUS=ERROR|REASON=INVALID_LENGTH");
                return;
            }

            // Read payload bytes
            byte[] payloadBytes = new byte[length];
            in.readFully(payloadBytes);
            String request = new String(payloadBytes, StandardCharsets.UTF_8);

            LoggerUtil.info("Server received payload (" + length + " bytes): " + request);

            // Generate response
            String response;
            if (request.contains("BANKING_TRANSACTION_REQUEST")) {
                response = "BANKING_TRANSACTION_RESPONSE|STATUS=RECEIVED";
            } else {
                response = "BANKING_TRANSACTION_RESPONSE|STATUS=RECEIVED|DATA=" + request;
            }

            sendResponse(out, response);

        } catch (IOException e) {
            LoggerUtil.warn("Server socket communication error: " + e.getMessage());
        }
    }

    private void sendResponse(DataOutputStream out, String responseText) throws IOException {
        byte[] respBytes = responseText.getBytes(StandardCharsets.UTF_8);
        out.writeInt(respBytes.length);
        out.write(respBytes);
        out.flush();
        LoggerUtil.info("Server sent response (" + respBytes.length + " bytes): " + responseText);
    }

    /**
     * Gets the actual port number bound by the server socket.
     *
     * @return bound port number
     */
    public int getPort() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            return serverSocket.getLocalPort();
        }
        return configuredPort;
    }

    /**
     * Checks if the server is actively running.
     *
     * @return true if running and bound
     */
    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }

    /**
     * Stops the server and releases all bound network resources.
     */
    public synchronized void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LoggerUtil.warn("Error closing server socket: " + e.getMessage());
            }
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
        LoggerUtil.info("Banking TCP Server stopped cleanly.");
    }
}
