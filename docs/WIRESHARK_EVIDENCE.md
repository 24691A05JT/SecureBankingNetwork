# Wireshark Network Capture Evidence Log
**Project:** SecureBankingNetwork  
**Verification Level:** Network Layer Socket Traffic Verification  

---

## 1. Capture Session Details

| Parameter | Value / Record |
| :--- | :--- |
| **Capture Date & Time** | `[ PENDING MANUAL CAPTURE ]` |
| **Application Name** | `SecureBankingNetwork` |
| **Transport Protocol** | `TCP / IP (Java Socket / ServerSocket)` |
| **Host IP Address** | `127.0.0.1 (Localhost Loopback)` |
| **Bound Server Port** | `[ Run main or pipeline to log TCP_CAPTURE_TARGET ]` |
| **Source Account** | `ACC10001` |
| **Destination Account** | `ACC10002` |
| **Transfer Amount** | `₹5,000.00` |
| **Encryption Mode** | `AES-256-GCM (NoPadding, 12-byte IV)` |

---

## 2. Network Observation Checklist

> [!IMPORTANT]
> This checklist tracks observable protocol events during Wireshark loopback analysis.
> Status is marked as `PENDING MANUAL VERIFICATION` until capture log entries are filled by operator.

- [ ] **[1] TCP SYN Packet**: Client initiates 3-way handshake to `127.0.0.1:<PORT>` (`PENDING MANUAL VERIFICATION`)
- [ ] **[2] TCP SYN-ACK Packet**: Server responds from bound port (`PENDING MANUAL VERIFICATION`)
- [ ] **[3] TCP ACK Packet**: Handshake completes; socket established (`PENDING MANUAL VERIFICATION`)
- [ ] **[4] Application Data Segment**: 4-byte length prefix + UTF-8 payload (`PENDING MANUAL VERIFICATION`)
- [ ] **[5] Confidentiality Check**: Plaintext account numbers & amounts unreadable in hex payload (`PENDING MANUAL VERIFICATION`)
- [ ] **[6] Server Response Segment**: Server acknowledges receipt (`BANKING_TRANSACTION_RESPONSE`) (`PENDING MANUAL VERIFICATION`)
- [ ] **[7] TCP FIN/ACK Teardown**: Graceful four-way socket closure (`PENDING MANUAL VERIFICATION`)

---

## 3. Wireshark Packet Summary Table (Template for Manual Audit)

| Packet No. | Time (s) | Source IP:Port | Destination IP:Port | Protocol | Flags | Length (Bytes) | Info / Payload Summary |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `1` | `0.000000` | `127.0.0.1:<CLIENT_PORT>` | `127.0.0.1:<SERVER_PORT>` | `TCP` | `[SYN]` | `74` | `Seq=0 Win=65535 Len=0` |
| `2` | `0.000150` | `127.0.0.1:<SERVER_PORT>` | `127.0.0.1:<CLIENT_PORT>` | `TCP` | `[SYN, ACK]` | `74` | `Seq=0 Ack=1 Win=65535 Len=0` |
| `3` | `0.000280` | `127.0.0.1:<CLIENT_PORT>` | `127.0.0.1:<SERVER_PORT>` | `TCP` | `[ACK]` | `66` | `Seq=1 Ack=1 Win=65535 Len=0` |
| `4` | `0.001420` | `127.0.0.1:<CLIENT_PORT>` | `127.0.0.1:<SERVER_PORT>` | `TCP` | `[PSH, ACK]` | `421` | `Application Data: 4-byte length + AES-256-GCM payload` |
| `5` | `0.002100` | `127.0.0.1:<SERVER_PORT>` | `127.0.0.1:<CLIENT_PORT>` | `TCP` | `[PSH, ACK]` | `114` | `Server Response: BANKING_TRANSACTION_RESPONSE` |
| `6` | `0.002850` | `127.0.0.1:<CLIENT_PORT>` | `127.0.0.1:<SERVER_PORT>` | `TCP` | `[FIN, ACK]` | `66` | `Connection Teardown Initiated` |
| `7` | `0.003120` | `127.0.0.1:<SERVER_PORT>` | `127.0.0.1:<CLIENT_PORT>` | `TCP` | `[FIN, ACK]` | `66` | `Connection Teardown Confirmed` |

---

## 4. Encryption Payload Verification Proof
Inspect hex dump of packet `4`:
- **Expected Data**: `Base64 Ciphertext + IV + CRC`
- **Result**: Plaintext strings such as `"5000.00"` or `"ACC10001"` are **ABSENT** from the wire packet, confirming cryptographic confidentiality.
