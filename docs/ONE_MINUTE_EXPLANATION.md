# One-Minute Project Presentation Pitch
**Spoken Time:** ~60 Seconds  
**Usage:** Opening project introduction during Viva Voce or presentation defense.  

---

## The Pitch (Read Aloud Script)

> *"Good morning/afternoon. My project is titled **Secure and Reliable Banking Network Using Routing and Encryption**.*
> 
> *In real-world banking, transmitting transactions across multi-hop networks exposes sensitive financial data to eavesdropping, packet loss, bit flips, and database inconsistencies.*
> 
> *To solve this, I engineered an integrated Java platform that coordinates networking, security, reliability, socket transport, and databases across a 12-stage pipeline.*
> 
> *First, our system subnets a 10-node enterprise topology. Custom Link-State Dijkstra and Distance Vector algorithms calculate the optimal path from Branch 1 to the Database Server with a cost of 8.0.*
> 
> *Next, transactions are encrypted using **AES-256-GCM** with random 12-byte IVs and authenticated tags, protected by **CRC-16-CCITT** error checksums, and framed via **Sliding Window** and **Go-Back-N ARQ** protocols.*
> 
> *The payload is transmitted over real TCP sockets using 4-byte length prefixing. After CRC and AES-GCM verification on the server, the transfer executes atomically in **SQLite**, updating account balances safely.*
> 
> *Finally, our system features an interactive **JavaFX dashboard** for live network visualization, route highlighting, and activity logging, backed by **156 automated JUnit tests** with a 100% pass rate. Thank you."*
