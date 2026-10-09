# Technical Parameters & Constants Reference Card
**Project:** Secure Banking Network  
**Usage:** Quick technical lookup during viva questions and technical reviews.  

---

## Technical Specifications Matrix

| System Domain | Specification / Parameter | Exact Technical Value |
| :--- | :--- | :--- |
| **Java Platform** | Development Kit | Java 26 (Release 26) |
| **Build Management** | Maven Plugins | Compiler 3.13.0, Surefire 3.5.2, JavaFX 0.0.8, Exec 3.5.0 |
| **GUI Framework** | OpenJFX Version | JavaFX 22.0.2 (`javafx-controls`, `javafx-fxml`) |
| **Database Engine** | SQLite JDBC Driver | SQLite 3.47.2.0 (`jdbc:sqlite:data/banking.db`) |
| **Test Framework** | JUnit 5 Jupiter | 5.11.4 (`junit-jupiter-api`, `junit-jupiter-engine`) |
| **Symmetric Cipher** | AES Transformation | `AES/GCM/NoPadding` |
| **AES Key Size** | SecretKey Bit Length | **256 bits** (`KeyGenerator.getInstance("AES")`) |
| **GCM Nonce (IV)** | Initialization Vector | **12 bytes** (Random per operation via `SecureRandom`) |
| **GCM Tag Length** | Authentication Tag | **128 bits** (`GCMParameterSpec(128, iv)`) |
| **CRC Algorithm** | Error Checksum | **CRC-16-CCITT** |
| **CRC Polynomial** | Generator Polynomial | **`0x1021`** ($x^{16} + x^{12} + x^5 + 1$) |
| **CRC Initial Value** | Bit Register Initial | **`0xFFFF`** |
| **CRC Test Vector** | Standard Test Input | `"123456789"` $\rightarrow$ **`0x29B1`** |
| **TCP Host Scope** | Socket Binding Host | `127.0.0.1` (Localhost Loopback) |
| **TCP Port Binding** | Server Socket Port | Dynamic auto-assigned port (`ServerSocket(0)`) |
| **TCP Message Limit**| Payload Capacity | **1 MB** (`1,048,576 bytes`) |
| **TCP Socket Timeout**| Read Timeout | **5,000 ms** (`setSoTimeout(5000)`) |
| **TCP Framing** | Length Prefix | **4-byte Big-Endian Integer** (`DataOutputStream.writeInt`) |
| **Text Encoding** | Character Set | **UTF-8** (`StandardCharsets.UTF_8`) |
| **Graph Topology** | Total Nodes / Links | **10 Enterprise Nodes / 10 Weighted Links** |
| **Shortest Route** | Computed Path | `NODE-B1` $\rightarrow$ `ROUTER-B1` $\rightarrow$ `ROUTER-HQ` $\rightarrow$ `NODE-DB` |
| **Shortest Path Cost**| Dijkstra & DV Cost | **`8.0`** (Next Hop: `Branch 1 Router`) |
| **DV Convergence** | Bellman-Ford Iterations| **3 Iterations** |
| **Sliding Window** | Window Capacity | **`2`** (Default active window size) |
| **Go-Back-N ARQ** | Reliability Mechanics | Cumulative ACKs, In-order enforcement, Timeout retransmission |
| **Demo Accounts** | Initial Balances | `ACC10001` (₹50,000.00), `ACC10002` (₹25,000.00) |
| **Demo Transfer** | Transfer Amount | **₹5,000.00** (`ACC10001` $\rightarrow$ ₹45,000, `ACC10002` $\rightarrow$ ₹30,000) |
| **Automated Tests** | Total Passed Tests | **156 / 156 PASS** (100% Pass Rate across 19 suites) |
| **Build Status** | Maven Output | **`BUILD SUCCESS`** |
