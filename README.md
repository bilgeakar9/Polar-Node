
# PolarNode 

## Project Overview
**PolarNode** is a Java-based desktop application developed to monitor and control specialized cold-storage chambers. The system interacts with an embedded module designed for commercial refrigeration units that store high-sensitivity, thermally unstable materials such as biological samples and pharmaceuticals.

The application follows communicates with the device through a WebSocket connection. Its primary goal is to maintain stable thermal conditions within a critical 0 °C – 10 °C temperature range.

---

## Features

### Real-Time Telemetry
Continuously monitors internal temperature, battery level, and actuator states providing up-to-date system information.

### Auto-Pilot Control
Supports an automatic control loop that engages the **fan** or **heater** to maintain thermal stability without manual intervention.

### Energy Visualization
Displays the current battery level (**0–100%**)

### CSV Data Logging
Automatically records all incoming telemetry frames into a persistent CSV file. This allows historical data analysis and thermal modeling.

### System Diagnostics
Tracks device status indicators including:
- Too Cold  
- Too Hot  
- Power Issues  
- Control Errors  

---

## Architecture

### Model
Responsible for data processing and system logic.

**Telemetry Parsing**  
Processes binary serial data frames identified by the `0xAB` header.

**Command Logic**  
Implements the command protocol used to control the **Fan** and **Heater** actuators.

**Data Integrity**  
Uses a **CRC16 checksum algorithm** to validate all incoming and outgoing messages.

---

### View
Handles the graphical user interface.

**Java Swing UI**

Includes:
- Real-time **temperature graph** displaying the last **60 seconds** of data
- **Actuator status indicators** showing the current state of:
  - Circulation Fan *(Off / Low / High)*
  - Micro-Heater *(Off / Low / High)*

---

### Controller
Manages communication and application flow.

**WebSocket Interface**  
Maintains the binary connection to:

```
wss://polarnode.alsoft.nl
```

**State Management**

Coordinates between:

- Manual user control
- Automatic **Auto-Pilot thermal regulation loop**

---

## Technologies Used

- **Java** – Core programming language  
- **Java Swing** – Graphical User Interface framework  
- **WebSockets** – Binary communication with the PolarNode device  
- **CRC16 Algorithm** – Protocol data integrity validation  
- **MVC Architecture** – Structured software design pattern  
- **IntelliJ IDEA** – Development environment

