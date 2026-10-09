# Live Demonstration Script (5–7 Minutes)
**Project:** Secure Banking Network  
**Target Audience:** Project Evaluators, Faculty & External Examiners  

---

## Overview
This script provides an exact step-by-step walkthrough for performing a 5 to 7 minute live demonstration of the **Secure Banking Network** platform. 

For each step, follow the three guidance prompts: **WHAT TO CLICK**, **WHAT TO SHOW**, and **WHAT TO SAY**.

---

## Step 1: Launch the Application
- **WHAT TO CLICK**: Open your terminal in the project directory and run:
  ```powershell
  mvn javafx:run
  ```
- **WHAT TO SHOW**: The 1420x880 JavaFX desktop dashboard window opening with dark slate theme, header badges, topology canvas, sidebar cards, and activity log.
- **WHAT TO SAY**:  
  *"Good morning/afternoon. Today I am demonstrating the Secure Banking Network project. This system integrates Computer Networking, Applied Cryptography, Reliable ARQ protocols, TCP Sockets, and SQLite database persistence into one unified banking platform."*

---

## Step 2: Explain Network Topology
- **WHAT TO CLICK**: Point your mouse to the central network topology canvas.
- **WHAT TO SHOW**: The 10-node banking network diagram displaying Branch 1 Gateway, Branch 1 Router, HQ Core Router, Database Server, ATM Gateway, Branch 2 Gateway, etc.
- **WHAT TO SAY**:  
  *"Here on the main canvas is our simulated 10-node enterprise banking topology. It models head office systems, branch routers, ATM gateways, customer service terminals, management workstations, and a central database server. Inter-router backbones have redundant links with weighted link costs."*

---

## Step 3: Compute & Visualize Shortest Path Routing
- **WHAT TO CLICK**: Click the **⚡ FIND SHORTEST ROUTE (DIJKSTRA)** button.
- **WHAT TO SHOW**: The canvas instantly highlights the path from `Branch 1 Gateway` to `Database Server` in glowing emerald lines. The routing action bar displays: `Path: Branch 1 Gateway -> Branch 1 Router -> HQ Core Router -> Database Server | Cost: 8.0 | Next Hop: Branch 1 Router`.
- **WHAT TO SAY**:  
  *"When I click 'Find Shortest Route', our Link-State Dijkstra algorithm evaluates all available graph paths and selects the lowest-cost route. Notice how the visual path illuminates instantly on the canvas, showing a total path cost of 8.0 with Branch 1 Router as the immediate next hop."*

---

## Step 4: Show Distance Vector Algorithm Comparison
- **WHAT TO CLICK**: Point to the Routing Comparison bar below the canvas.
- **WHAT TO SHOW**: The label displaying: `Routing Status | Dijkstra Cost: 8.0 | Distance Vector Cost: 8.0 | RESULT: ROUTES MATCH ✓`.
- **WHAT TO SAY**:  
  *"Simultaneously, our Distance Vector routing engine executes Bellman-Ford iterative vector exchanges. As shown here, both independent routing algorithms converge on the exact same cost of 8.0, confirming optimal path selection."*

---

## Step 5: Review Security & Reliability Cards
- **WHAT TO CLICK**: Scroll down the right sidebar panel to the Security and Reliability cards.
- **WHAT TO SHOW**:
  - Security Card: `AES-256-GCM`, `12-Byte IV`, `128-bit Auth Tag`, `CRC-16-CCITT Checksum`.
  - Reliability Card: `Sliding Window (Size: 2)`, `Go-Back-N ARQ`, `TCP Socket 127.0.0.1`.
- **WHAT TO SAY**:  
  *"On the right sidebar, our Security status card confirms AES-256-GCM encryption with random IVs and 128-bit authentication tags. CRC-16-CCITT provides link-layer checksum error detection. The Reliability card shows Sliding Window flow control and Go-Back-N ARQ active."*

---

## Step 6: Process End-to-End Secure Transaction
- **WHAT TO CLICK**: Point to the Account Summary card showing initial balances (`ACC10001`: ₹50,000.00, `ACC10002`: ₹25,000.00), then click **🔒 PROCESS SECURE TRANSACTION**.
- **WHAT TO SHOW**:
  1. The pipeline checklist indicators update live from `READY` to `✓ SUCCESS` across all 9 stages.
  2. The account balances dynamically update: `ACC10001` becomes **₹45,000.00** and `ACC10002` becomes **₹30,000.00**.
  3. The bottom Activity Log streams timestamped execution events.
- **WHAT TO SAY**:  
  *"Now we process a ₹5,000.00 transfer from ACC10001 to ACC10002. When I click 'Process Secure Transaction', our 12-stage pipeline creates the request, routes it, encrypts it with AES-256-GCM, generates a CRC-16 checksum, frames it with Sliding Window/Go-Back-N, sends it over a real TCP socket, verifies CRC, decrypts the payload, executes the atomic transfer in BankingService, and commits to SQLite. Notice how the account balances update live in the UI from the database."*

---

## Step 7: Explain Wireshark Evidence & Packet Analysis
- **WHAT TO CLICK**: Point to the TCP status card and activity log displaying `TCP_CAPTURE_TARGET=127.0.0.1:<PORT>`.
- **WHAT TO SHOW**: Refer to `docs/WIRESHARK_ANALYSIS.md` and `docs/WIRESHARK_EVIDENCE.md`.
- **WHAT TO SAY**:  
  *"Our TCP server logs the target loopback port on startup. In Wireshark, filtering by this port captures the TCP 3-way handshake, 4-byte length-prefixed application data, and 4-way teardown. Because AES-256-GCM encrypts the payload before socket transport, sensitive account numbers and amounts never appear as plaintext on the wire."*

---

## Step 8: Show Automated Test Suite Metrics
- **WHAT TO CLICK**: Open a secondary terminal window and run:
  ```powershell
  mvn test
  ```
- **WHAT TO SHOW**: Maven test execution output concluding with:  
  `Tests run: 156, Failures: 0, Errors: 0, Skipped: 0 | BUILD SUCCESS`.
- **WHAT TO SAY**:  
  *"Finally, our entire platform is backed by a rigorous automated test suite. All 156 unit and integration tests across 19 test classes pass with zero failures and a 100% pass rate."*

---

## Step 9: Conclude Presentation
- **WHAT TO CLICK**: Return to the dashboard or slide deck.
- **WHAT TO SHOW**: Summary screen or live dashboard.
- **WHAT TO SAY**:  
  *"In conclusion, the Secure Banking Network successfully demonstrates a complete end-to-end financial transaction pipeline combining custom routing, authenticated encryption, error detection, ARQ reliability, real TCP sockets, SQLite persistence, and JavaFX monitoring. Thank you. I am now open for viva questions."*
