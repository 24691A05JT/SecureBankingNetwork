# Secure and Reliable Banking Network Using Routing and Encryption

A professional Java-based academic software system demonstrating secure, reliable end-to-end banking transactions over simulated network topologies with custom routing algorithms, encryption protocols, and error-control mechanics.

## 📌 Project Overview

This project simulates a enterprise-grade banking network infrastructure. It combines fundamental concepts from **Computer Networking**, **Cryptography**, **Database Management**, and **Distributed Systems** into a unified desktop application.

### Key Objectives
1. **Network Simulation & Routing**: Implements custom routing protocols (Dijkstra's Shortest Path & Distance Vector Routing) over IP subnetted nodes.
2. **Reliable Data Transfer**: Guarantees packet integrity and reliable transmission using CRC checksums, Sliding Window, and Go-Back-N ARQ.
3. **Cryptographic Security**: Enforces confidentiality and integrity via Java Cryptography Architecture (JCA) AES/RSA encryption.
4. **Banking Core & Persistence**: Handles accounts and financial transactions using SQLite JDBC persistence.
5. **Monitoring Dashboard**: Interactive JavaFX UI for real-time network topology visualization and monitoring.

---

## 🛠️ Technology Stack

- **Language**: Java / JDK 26
- **Build & Management Tool**: Maven
- **User Interface**: JavaFX (Controls & FXML)
- **Database**: SQLite & JDBC
- **Networking**: Java Socket Programming (TCP/IP)
- **Security**: Java Cryptography Architecture (JCA)
- **Testing**: JUnit 5
- **Packet Analysis**: Wireshark (for external protocol validation)

---

## 📁 Project Architecture

```
SecureBankingNetwork/
├── pom.xml
├── README.md
├── .gitignore
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── banking/
│   │   │           ├── Main.java
│   │   │           ├── network/
│   │   │           ├── security/
│   │   │           ├── reliability/
│   │   │           ├── communication/
│   │   │           ├── model/
│   │   │           ├── database/
│   │   │           ├── dashboard/
│   │   │           └── util/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── banking/
└── data/
    └── banking.db
```

---

## 🚀 Getting Started

### Prerequisites
- JDK 26 installed and set in `JAVA_HOME`.
- Apache Maven 3.9+ installed.

### Build and Test
To compile the project and execute unit tests:
```bash
mvn clean compile test
```

### Run the Application
To run the main initialization entry point:
```bash
mvn exec:java
```

---

## 📅 Development Roadmap

- [x] **Day 1**: Environment setup, project structure initialization, Maven configuration, and verification.
- [ ] **Day 2+**: Network topology & routing implementations (IPAddress, Subnet, Dijkstra, Distance Vector).
- [ ] **Security Layer**: AES/RSA encryption implementation via JCA.
- [ ] **Reliability Layer**: CRC validation, Sliding Window, Go-Back-N ARQ implementation.
- [ ] **Communication & Database**: Client-Server socket communication and SQLite persistence.
- [ ] **Dashboard**: JavaFX UI visualization & metrics.

---

## 📄 License

Academic / Educational Project - All Rights Reserved.
