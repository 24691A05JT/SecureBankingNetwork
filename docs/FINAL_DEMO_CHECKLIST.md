# Final Presentation & Demonstration Checklist
**Project:** Secure Banking Network  
**Usage:** Verification checklist to be completed prior to, during, and after project presentation.  

---

## 1. BEFORE DEMO (Preparation & Sanity Checks)

- [ ] **[1] Environment & JDK**: Java JDK 26 installed and verified (`java -version`).
- [ ] **[2] Build System**: Apache Maven installed and operational (`mvn -version`).
- [ ] **[3] Local Storage**: Project workspace clean; `data/` directory writable.
- [ ] **[4] Compilation & Package**: `mvn clean compile test package` completes with `BUILD SUCCESS`.
- [ ] **[5] Test Suite Metrics**: All 156 automated unit and integration tests pass cleanly (`156/156 PASS`).
- [ ] **[6] Main CLI Execution**: `mvn exec:java "-Dexec.mainClass=com.banking.Main"` executes all 11 Day demos.
- [ ] **[7] JavaFX Dashboard Launch**: `mvn javafx:run` opens 1420x880 dashboard UI cleanly without exceptions.
- [ ] **[8] Wireshark Setup**: Wireshark installed; Npcap Loopback Adapter ready on Windows host.
- [ ] **[9] Presentation Files Ready**: Slide deck (`FINAL_PPT_CONTENT.md`), Report (`FINAL_PROJECT_REPORT.md`), Demo Script (`DEMO_SCRIPT.md`), Viva QA (`VIVA_QA.md`) available.

---

## 2. DURING DEMO (Execution Walkthrough)

- [ ] **[10] Dashboard Display**: Launch JavaFX dashboard UI (`DashboardApplication`).
- [ ] **[11] Network Topology Inspection**: Point out 10-node network canvas and status badges.
- [ ] **[12] Shortest Path Computation**: Click **⚡ FIND SHORTEST ROUTE (DIJKSTRA)**; verify path highlight (`NODE-B1` $\rightarrow$ `ROUTER-B1` $\rightarrow$ `ROUTER-HQ` $\rightarrow$ `NODE-DB`, Cost: 8.0).
- [ ] **[13] Routing Comparison**: Highlight Dijkstra vs Distance Vector route equality (`ROUTES MATCH ✓`).
- [ ] **[14] Security & Reliability Cards**: Review AES-256-GCM, CRC-16-CCITT, Sliding Window (Size 2), and Go-Back-N ARQ cards.
- [ ] **[15] Process Transaction**: Submit ₹5,000.00 transfer from `ACC10001` to `ACC10002` via **🔒 PROCESS SECURE TRANSACTION**.
- [ ] **[16] Stage Checklist Verification**: Confirm all 9 stage checklist labels update to `✓ SUCCESS`.
- [ ] **[17] Dynamic Balance Update**: Confirm account balances update live in SQLite (`ACC10001` = ₹45,000.00, `ACC10002` = ₹30,000.00).
- [ ] **[18] Activity Log Streaming**: Show real-time timestamped log output in bottom console feed.
- [ ] **[19] Wireshark Analysis**: Show Wireshark capture procedure and payload confidentiality proof.

---

## 3. AFTER DEMO (Post-Presentation Wrap-Up)

- [ ] **[20] Command Line Verification**: Re-run `mvn test` in terminal to confirm tests pass cleanly.
- [ ] **[21] Answer Viva Questions**: Reference `VIVA_QA.md` for technical defense queries.
- [ ] **[22] Clean Application Closure**: Close JavaFX window; verify server socket and SQLite connection close cleanly.
- [ ] **[23] Git Repository Verification**: Check `git status` to ensure repository working tree is clean.
