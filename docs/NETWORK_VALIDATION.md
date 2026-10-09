# Network Architecture & Module Validation Report
**Project:** SecureBankingNetwork  
**Documentation Phase:** Day 13 Network Validation & Verification  

---

## Executive Summary
This document summarizes the architecture, implementation details, verification methods, and validation results across all 12 core functional domains of the **SecureBankingNetwork** system.

---

## 1. IP Addressing
- **Purpose**: Provide structured 32-bit IPv4 address representation and validation for network nodes.
- **Implementation**: Custom `IPAddress` class handling dotted-decimal parsing, 32-bit integer conversion, and validation.
- **Verification Method**: `IPAddressTest` unit test suite executing range boundary and format checks.
- **Result**: `PASSED (11/11 tests)`

---

## 2. Subnetting
- **Purpose**: Define logical network boundaries and usable host address ranges across simulated bank branches and servers.
- **Implementation**: Custom `Subnet` class implementing CIDR calculations, broadcast/network address derivation, and containment logic.
- **Verification Method**: `SubnetTest` & `BankingNetworkConfigTest` validating subnets (Head Office `10.0.0.0/24`, Branch 1 `10.0.1.0/24`, Branch 2 `10.0.2.0/24`, ATM `10.0.10.0/24`, etc.).
- **Result**: `PASSED (24/24 tests)`

---

## 3. Dijkstra Routing
- **Purpose**: Compute global link-state shortest paths across the topology graph.
- **Implementation**: `DijkstraRouter` using non-negative weighted graph link costs and PriorityQueue relaxation.
- **Verification Method**: `DijkstraRouterTest` asserting shortest path (`NODE-B1` -> `ROUTER-B1` -> `ROUTER-HQ` -> `NODE-DB`, Cost: 8.0, Next Hop: `ROUTER-B1`).
- **Result**: `PASSED (11/11 tests)`

---

## 4. Distance Vector Routing
- **Purpose**: Compute decentralized Bellman-Ford distance vector routing tables via iterative table convergence.
- **Implementation**: `DistanceVectorRouter` with iterative table exchange and convergence detection.
- **Verification Method**: `DistanceVectorRouterTest` verifying convergence equality with Dijkstra shortest path cost.
- **Result**: `PASSED (11/11 tests)`

---

## 5. TCP Communication
- **Purpose**: Provide real network transport over localhost using Java TCP Sockets.
- **Implementation**: `BankingClient` and `BankingServer` using 4-byte big-endian integer length-prefixed UTF-8 message framing.
- **Verification Method**: `BankingClientServerTest` testing client-server socket exchanges, framing, UTF-8 payloads, and clean resource teardown.
- **Result**: `PASSED (15/15 tests)`

---

## 6. AES-GCM Security
- **Purpose**: Protect transaction payload confidentiality and authenticity over unsecure network links.
- **Implementation**: `EncryptionService` implementing 256-bit AES-GCM encryption with 12-byte random IVs and 128-bit authentication tags.
- **Verification Method**: `EncryptionServiceTest` testing encryption, decryption, IV uniqueness, and authentication tag mismatch failure on tampered data.
- **Result**: `PASSED (14/14 tests)`

---

## 7. CRC Error Detection
- **Purpose**: Detect physical/link-layer bit corruption during message transmission.
- **Implementation**: `CRCService` implementing standard CRC-16-CCITT polynomial algorithm (`0x1021`, initial `0xFFFF`).
- **Verification Method**: `CRCServiceTest` validating known vector `"123456789"` (`0x29B1`) and detecting payload alterations.
- **Result**: `PASSED (10/10 tests)`

---

## 8. Sliding Window Protocol
- **Purpose**: Enforce flow control and window management during frame transmission.
- **Implementation**: `SlidingWindow` protocol managing sequence numbers, active window bounds, and ACK sliding base logic.
- **Verification Method**: `SlidingWindowTest` validating window capacity limits, out-of-order ACK handling, and frame loss simulation.
- **Result**: `PASSED (15/15 tests)`

---

## 9. Go-Back-N ARQ Protocol
- **Purpose**: Provide reliable retransmission error control over lossy connections.
- **Implementation**: `GoBackN` protocol enforcing in-order receiver acceptance, cumulative ACKs, timeout handling, and window retransmissions.
- **Verification Method**: `GoBackNTest` testing out-of-order frame rejection, timeout retransmission, and transmission completion.
- **Result**: `PASSED (11/11 tests)`

---

## 10. SQLite Persistence
- **Purpose**: Provide transactional ACID persistence for accounts and audit logs.
- **Implementation**: `DatabaseManager`, `AccountRepository`, `TransactionRepository`, and `BankingService`.
- **Verification Method**: `BankingServiceTest` & `AccountRepositoryTest` testing atomic transfers, rollbacks, and balance updates.
- **Result**: `PASSED (24/24 tests)`

---

## 11. JavaFX Visualization
- **Purpose**: Offer an interactive monitoring dashboard displaying real-time topology, routing highlights, security status, and transaction controls.
- **Implementation**: `DashboardApplication` built using JavaFX controls, canvas node drawing, status cards, and live log feeds.
- **Verification Method**: Manual UI launching (`DashboardApplication.main`) and button interaction testing.
- **Result**: `VERIFIED & OPERATIONAL`

---

## 12. Wireshark Manual Verification
- **Purpose**: Capture live TCP loopback packets to verify socket handshake, length-prefixed framing, teardown, and payload encryption on the wire.
- **Implementation**: Wireshark packet capture using Npcap Loopback Adapter on target port logged by `TCP_CAPTURE_TARGET`.
- **Verification Method**: Manual packet capture using display filter `tcp.port == <PORT>`.
- **Result**: `PENDING MANUAL CAPTURE` *(Awaiting operator Wireshark session)*
