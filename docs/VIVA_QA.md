# Comprehensive Viva Voce Questions & Answers (50 Q&As)
**Project:** Secure Banking Network  
**Target:** Project Defense, Technical Viva, Oral Examinations  

---

## 1. General Questions

### Q1: Why did you choose this project?
**Answer:** Banking applications require high security, reliable transport, efficient routing, and data consistency. This project integrates core concepts from Networking, Cryptography, Operating Systems, Databases, and Software Engineering into a single working platform.

### Q2: What core problem does your project solve?
**Answer:** It solves the vulnerability of financial data transmitted over unsecure networks by combining AES-256-GCM encryption, CRC-16 error checking, Sliding Window/Go-Back-N ARQ flow control, length-prefixed TCP sockets, and SQLite ACID transaction persistence.

### Q3: What is the main objective of the system?
**Answer:** To demonstrate an end-to-end 12-stage pipeline where a banking transaction is routed over a network graph, encrypted, framed, reliably transmitted over TCP sockets, verified, decrypted, and persisted atomically in a database.

### Q4: What core technology stack did you use?
**Answer:** Java 26 (JDK), Maven, JavaFX for GUI visualization, SQLite JDBC for persistence, Java Cryptography Architecture (JCA) for AES-GCM, and Java Socket API for TCP transport.

---

## 2. Computer Networking & Subnetting

### Q5: What is an IP address?
**Answer:** A 32-bit numerical identifier assigned to each device connected to a network using the Internet Protocol (e.g., `10.0.1.1`).

### Q6: What is subnetting?
**Answer:** Subnetting is the practice of logically partitioning a single physical IP network into multiple smaller network sub-networks (subnets) to improve routing efficiency and security.

### Q7: What is CIDR notation?
**Answer:** Classless Inter-Domain Routing (CIDR) specifies an IP address and its associated network prefix mask length (e.g., `10.0.1.0/24` means the first 24 bits represent the network ID).

### Q8: Why did you use `/24` subnets for your banking network?
**Answer:** A `/24` subnet provides 254 usable host addresses (`.1` to `.254`), which is ideal for branch networks, ATM clusters, and server subnets while maintaining clear boundary isolation.

### Q9: What is a router?
**Answer:** A network device that forwards data packets between computer networks based on IP addresses and routing tables.

### Q10: What is a network topology?
**Answer:** The physical or logical arrangement of nodes (routers, servers, terminals) and connecting links (cables, wireless) in a network graph.

---

## 3. Routing Algorithms

### Q11: What is Dijkstra's algorithm?
**Answer:** A Link-State routing algorithm that calculates the shortest (lowest cost) path from a source node to all other nodes in a weighted graph using global topology knowledge.

### Q12: What is the time complexity of Dijkstra's algorithm?
**Answer:** Using a PriorityQueue (min-heap), the time complexity is $\mathcal{O}((V + E) \log V)$, where $V$ is the number of nodes (vertices) and $E$ is the number of links (edges).

### Q13: What is Distance Vector routing?
**Answer:** A decentralized routing algorithm based on the Bellman-Ford equation where each router maintains a vector of estimated costs to all destinations and periodically exchanges vectors with immediate neighbors.

### Q14: What is the Bellman-Ford equation?
**Answer:** $D_x(y) = \min_v \{ c(x,v) + D_v(y) \}$, where cost from node $x$ to $y$ is the minimum over all neighbors $v$ of the link cost $c(x,v)$ plus neighbor $v$'s distance to $y$.

### Q15: Key differences between Link-State (Dijkstra) and Distance Vector (Bellman-Ford)?
**Answer:** Link-State requires every router to have complete global topology knowledge, whereas Distance Vector relies on iterative, local vector exchanges with direct neighbors only.

### Q16: What is a "Next Hop"?
**Answer:** The immediate neighboring router to which a packet should be forwarded along the calculated shortest path to reach its final destination.

### Q17: Why did your calculated shortest path cost equal `8.0`?
**Answer:** In our topology graph, the path from `Branch 1 Gateway` to `Database Server` goes: Branch 1 Gateway $\rightarrow$ Branch 1 Router (cost 2) $\rightarrow$ HQ Core Router (cost 4) $\rightarrow$ Database Server (cost 2), giving $2 + 4 + 2 = 8.0$.

---

## 4. Security & Cryptography

### Q18: What is AES?
**Answer:** Advanced Encryption Standard, a symmetric block cipher selected by NIST that operates on 128-bit data blocks using 128, 192, or 256-bit key sizes.

### Q19: Why did you use AES-256?
**Answer:** AES-256 provides 256-bit key security, making brute-force key search computationally impossible with current or near-future computing power.

### Q20: What is GCM mode?
**Answer:** Galois/Counter Mode is an authenticated encryption mode that provides both data confidentiality (encryption) and data authenticity/integrity (via an authentication tag).

### Q21: What is an Initialization Vector (IV)?
**Answer:** An IV is a random nonce byte array passed to the cipher. It ensures that encrypting the same plaintext twice produces completely different ciphertexts.

### Q22: Why must an IV be unique for every AES-GCM operation?
**Answer:** Reusing an IV with the same AES key in GCM mode destroys authentication guarantees and can allow attackers to recover plaintext.

### Q23: What is a GCM Authentication Tag?
**Answer:** A 128-bit cryptographic MAC generated during encryption. During decryption, GCM recalculates this tag; if even 1 bit of ciphertext was altered, tag verification fails and decryption is rejected.

### Q24: Difference between symmetric and asymmetric encryption?
**Answer:** Symmetric encryption (AES) uses the same secret key for both encryption and decryption. Asymmetric encryption (RSA) uses a public key for encryption and a private key for decryption.

### Q25: Why didn't you use CRC for cryptographic security?
**Answer:** CRC is an unkeyed linear checksum designed to detect accidental transmission bit noise. Attackers can easily forge a valid CRC for tampered data. AES-GCM provides cryptographic authentication.

---

## 5. Cyclic Redundancy Check (CRC)

### Q26: What is CRC?
**Answer:** Cyclic Redundancy Check is a non-cryptographic error-detecting code based on polynomial division in Galois Field $GF(2)$.

### Q27: Which CRC variant did you implement?
**Answer:** CRC-16-CCITT using polynomial `0x1021` ($x^{16} + x^{12} + x^5 + 1$) and initial value `0xFFFF`.

### Q28: What is the output of `"123456789"` for CRC-16-CCITT?
**Answer:** `0x29B1`, which is the standard international test vector confirming correct implementation.

### Q29: What happens if CRC verification fails at Stage 8?
**Answer:** The pipeline immediately marks CRC status as FAILED, logs a warning, aborts processing, and does NOT decrypt or execute the banking transaction.

---

## 6. Reliable Transmission & ARQ

### Q30: What is Sliding Window flow control?
**Answer:** A protocol mechanism that allows a sender to transmit multiple frames up to a window size $N$ before receiving an acknowledgement, preventing network saturation.

### Q31: What is Go-Back-N ARQ?
**Answer:** An Automatic Repeat reQuest protocol where the receiver accepts frames strictly in order. If frame $k$ is lost, subsequent frames are discarded, and upon timeout, the sender retransmits all unacknowledged frames starting from $k$.

### Q32: What is a Cumulative Acknowledgement?
**Answer:** An ACK sequence number $k$ indicating that all frames up to and including $k$ have been received correctly by the destination.

### Q33: What happens when Frame #1 is lost during Go-Back-N simulation?
**Answer:** Frame #0 is accepted. Frame #1 is lost. Frame #2 is discarded by receiver (out of order). Timeout fires at base #1, and the sender retransmits Frame #1 and Frame #2.

---

## 7. TCP Socket Communication

### Q34: TCP vs UDP?
**Answer:** TCP is a connection-oriented, reliable, stream-based protocol providing ordered byte delivery and error recovery. UDP is connectionless and unreliable.

### Q35: Why use TCP for banking transactions?
**Answer:** Financial transfers require 100% reliable, ordered packet delivery where no byte can be dropped or duplicated.

### Q36: What is Java `ServerSocket`?
**Answer:** A server-side class that binds to a port and listens for incoming TCP client connection requests via `accept()`.

### Q37: What is Java `Socket`?
**Answer:** A client-side or server endpoint class used to establish connection and transfer stream bytes via `InputStream` and `OutputStream`.

### Q38: Why did you use 4-byte length-prefixed framing?
**Answer:** TCP is a byte-stream protocol without message boundaries. Prefixing each payload with a 4-byte big-endian integer payload length allows the receiver to read exactly the required byte count.

### Q39: Why UTF-8 encoding?
**Answer:** UTF-8 is a standard variable-width character encoding that reliably represents ASCII and special characters across platforms.

### Q40: What is the TCP 3-way handshake?
**Answer:** The connection setup sequence: Client sends `SYN`, Server replies with `SYN-ACK`, Client completes with `ACK`.

---

## 8. Database Persistence & Banking Logic

### Q41: Why SQLite for banking persistence?
**Answer:** SQLite is a lightweight, zero-configuration, ACID-compliant relational database engine that supports embedded JDBC transactions.

### Q42: What is an ACID transaction?
**Answer:** Atomicity (all or nothing), Consistency (valid state transitions), Isolation (independent transactions), and Durability (saved permanently).

### Q43: What is database rollback?
**Answer:** Reverting all uncommitted database SQL changes executed during a transaction if an error occurs, preventing partial data corruption.

### Q44: Why use `PreparedStatement` instead of plain `Statement`?
**Answer:** `PreparedStatement` pre-compiles SQL and binds parameters safely, preventing SQL injection attacks and improving performance.

---

## 9. Pipeline Integration & System Behavior

### Q45: Explain the 12-stage pipeline execution flow.
**Answer:** Creation $\rightarrow$ Routing $\rightarrow$ AES Encryption $\rightarrow$ CRC Checksum $\rightarrow$ Frame Delivery $\rightarrow$ TCP Socket Transport $\rightarrow$ Server Receipt $\rightarrow$ CRC Verification $\rightarrow$ AES Decryption $\rightarrow$ Banking Service Transfer $\rightarrow$ SQLite Persistence $\rightarrow$ Pipeline Response.

### Q46: What happens if an invalid negative amount is submitted?
**Answer:** Stage 1 validation catches the invalid amount ($\le 0$), aborts early, records error message, and leaves database balances untouched.

### Q47: What happens if an account has insufficient funds?
**Answer:** The pipeline proceeds through routing, encryption, TCP, and decryption, but Stage 10 (`BankingService.transfer`) detects insufficient funds, executes SQL rollback, logs transaction failure, and leaves balances untouched.

### Q48: How does the JavaFX UI interact with backend modules?
**Answer:** The UI calls existing backend classes (`DijkstraRouter`, `SecureBankingPipeline`, `AccountRepository`) directly, receiving immutable `PipelineResult` objects and updating UI nodes on the JavaFX Application Thread.

### Q49: What is your automated test count and pass rate?
**Answer:** **156 out of 156 tests passing (100% pass rate)** across 19 JUnit 5 test classes.

### Q50: What are the current limitations of your system?
**Answer:** Localhost socket demonstration scope (`127.0.0.1`), simulated 10-node topology graph, embedded SQLite rather than cloud distributed database, and manual Wireshark capture setup.
