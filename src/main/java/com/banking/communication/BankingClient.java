package com.banking.communication;

import com.banking.util.LoggerUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Professional TCP Client for sending length-prefixed messages over the banking network.
 */
public class BankingClient {

    public static final int MAX_MESSAGE_SIZE = 1_048_576; // 1 MB limit

    /**
     * Sends a message to a TCP server on host and port using length-prefixed UTF-8 framing.
     *
     * @param host server host IP or hostname
     * @param port server port
     * @param message text message to send
     * @return response string from server
     * @throws IOException on network/socket error
     */
    public String sendMessage(String host, int port, String message) throws IOException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
        if (payloadBytes.length == 0) {
            throw new IllegalArgumentException("Message payload cannot be empty");
        }
        if (payloadBytes.length > MAX_MESSAGE_SIZE) {
            throw new IllegalArgumentException("Message payload exceeds maximum size limit of " + MAX_MESSAGE_SIZE + " bytes");
        }

        LoggerUtil.info("Client initiating TCP connection to " + host + ":" + port);
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            socket.setSoTimeout(5000); // 5 second socket read timeout

            // Write 4-byte length prefix + UTF-8 payload
            out.writeInt(payloadBytes.length);
            out.write(payloadBytes);
            out.flush();

            LoggerUtil.info("Client sent message to " + host + ":" + port + " (" + payloadBytes.length + " bytes)");

            // Read 4-byte length response prefix
            int respLength = in.readInt();
            if (respLength <= 0 || respLength > MAX_MESSAGE_SIZE) {
                throw new IOException("Invalid response length received from server: " + respLength);
            }

            byte[] respBuffer = new byte[respLength];
            in.readFully(respBuffer);

            String response = new String(respBuffer, StandardCharsets.UTF_8);
            LoggerUtil.info("Client received response (" + respLength + " bytes): " + response);
            return response;
        }
    }
}
