package com.banking.communication;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BankingClientServerTest {

    private BankingServer server;
    private BankingClient client;

    @BeforeEach
    void setUp() {
        server = new BankingServer(0); // Auto-assigned port
        server.start();
        client = new BankingClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("1. Server can start")
    void testServerCanStart() {
        assertTrue(server.isRunning(), "Server should be running after start()");
    }

    @Test
    @DisplayName("2. Server can bind to an available port")
    void testServerBindsToPort() {
        assertTrue(server.getPort() > 0, "Server port should be assigned > 0");
    }

    @Test
    @DisplayName("3. getPort() returns actual assigned port")
    void testGetPortReturnsActualPort() {
        int port = server.getPort();
        assertTrue(port > 1024 && port <= 65535, "Port should be a valid unprivileged port number");
    }

    @Test
    @DisplayName("4. Client can connect to server")
    void testClientConnectsToServer() {
        assertDoesNotThrow(() -> {
            try (Socket socket = new Socket("127.0.0.1", server.getPort())) {
                assertTrue(socket.isConnected());
            }
        });
    }

    @Test
    @DisplayName("5. Client can send a simple message")
    void testClientSendsSimpleMessage() throws IOException {
        String response = client.sendMessage("127.0.0.1", server.getPort(), "BANKING_TRANSACTION_REQUEST");
        assertNotNull(response);
    }

    @Test
    @DisplayName("6. Server receives complete message")
    void testServerReceivesCompleteMessage() throws IOException {
        String req = "BANKING_TRANSACTION_REQUEST|PAYLOAD_TEST";
        String response = client.sendMessage("127.0.0.1", server.getPort(), req);
        assertNotNull(response);
    }

    @Test
    @DisplayName("7. Server returns a response")
    void testServerReturnsResponse() throws IOException {
        String response = client.sendMessage("127.0.0.1", server.getPort(), "BANKING_TRANSACTION_REQUEST");
        assertTrue(response.startsWith("BANKING_TRANSACTION_RESPONSE"));
    }

    @Test
    @DisplayName("8. Client receives complete response")
    void testClientReceivesCompleteResponse() throws IOException {
        String response = client.sendMessage("127.0.0.1", server.getPort(), "BANKING_TRANSACTION_REQUEST");
        assertEquals("BANKING_TRANSACTION_RESPONSE|STATUS=RECEIVED", response);
    }

    @Test
    @DisplayName("9. UTF-8 message transmission works")
    void testUtf8MessageTransmission() throws IOException {
        String utf8Msg = "BANKING_TRANSACTION_REQUEST|SRC=ACC10001|AMT=₹5000.00|NOTE=Secure UTF-8 Wire Transfer 🔒";
        String response = client.sendMessage("127.0.0.1", server.getPort(), utf8Msg);
        assertNotNull(response);
        assertTrue(response.contains("STATUS=RECEIVED"));
    }

    @Test
    @DisplayName("10. Length-prefixed framing works")
    void testLengthPrefixedFraming() throws IOException {
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            byte[] body = "BANKING_TRANSACTION_REQUEST".getBytes(StandardCharsets.UTF_8);
            out.writeInt(body.length);
            out.write(body);
            out.flush();

            int respLength = in.readInt();
            assertEquals(44, respLength); // "BANKING_TRANSACTION_RESPONSE|STATUS=RECEIVED".length() == 44 bytes

            byte[] respBuf = new byte[respLength];
            in.readFully(respBuf);
            assertEquals("BANKING_TRANSACTION_RESPONSE|STATUS=RECEIVED", new String(respBuf, StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("11. Empty/invalid message handling works")
    void testInvalidMessageHandling() {
        assertThrows(IllegalArgumentException.class, () -> client.sendMessage("127.0.0.1", server.getPort(), ""));
        assertThrows(IllegalArgumentException.class, () -> client.sendMessage("127.0.0.1", server.getPort(), null));
        assertThrows(IllegalArgumentException.class, () -> client.sendMessage(null, server.getPort(), "TEST"));
        assertThrows(IllegalArgumentException.class, () -> client.sendMessage("127.0.0.1", -1, "TEST"));
    }

    @Test
    @DisplayName("12. Oversized message is rejected")
    void testOversizedMessageRejected() {
        StringBuilder largeSb = new StringBuilder();
        for (int i = 0; i < 1_050_000; i++) { // > 1 MB
            largeSb.append("A");
        }
        assertThrows(IllegalArgumentException.class, () ->
                client.sendMessage("127.0.0.1", server.getPort(), largeSb.toString()));
    }

    @Test
    @DisplayName("13. Multiple sequential client connections handled safely")
    void testMultipleSequentialConnections() throws IOException {
        for (int i = 0; i < 5; i++) {
            String resp = client.sendMessage("127.0.0.1", server.getPort(), "BANKING_TRANSACTION_REQUEST|" + i);
            assertNotNull(resp);
            assertTrue(resp.contains("STATUS=RECEIVED"));
        }
    }

    @Test
    @DisplayName("14. Server can stop cleanly")
    void testServerStopsCleanly() {
        server.stop();
        assertFalse(server.isRunning(), "Server should not be running after stop()");
    }

    @Test
    @DisplayName("15. Client/server resources closed correctly after disconnect")
    void testResourcesClosedCorrectly() throws IOException {
        String resp = client.sendMessage("127.0.0.1", server.getPort(), "BANKING_TRANSACTION_REQUEST");
        assertNotNull(resp);
        // Connecting again verifies socket was closed and new connection accepted cleanly
        String resp2 = client.sendMessage("127.0.0.1", server.getPort(), "BANKING_TRANSACTION_REQUEST");
        assertNotNull(resp2);
    }
}
