# Running the Protocol Servers

## IntelliJ Run Configurations

### 1. AWS Protocol Server
- **Name**: AWS Protocol Server
- **Main class**: com.watchtower.protocol.CloudLogSourceProtocolServer
- **Program arguments**: AWS 8001
- **Working directory**: $ProjectFileDir$

### 2. GCP Protocol Server
- **Name**: GCP Protocol Server
- **Main class**: com.watchtower.protocol.CloudLogSourceProtocolServer
- **Program arguments**: GCP 8002
- **Working directory**: $ProjectFileDir$

## Command Line Alternative

Terminal 1:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.watchtower.protocol.CloudLogSourceProtocolServer" -Dexec.args="AWS 8001"
```

Terminal 2:
```bash
mvn exec:java -Dexec.mainClass="com.watchtower.protocol.CloudLogSourceProtocolServer" -Dexec.args="GCP 8002"
```

## Demo Flow

1. Start both servers (they'll show "listening on port...")
2. Run WatchTowerAgentProtocolTest
3. Watch the servers log connections
4. See the same agent code work with both clouds