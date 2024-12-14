# Mine Safety Monitoring Simulation with iFogSim

This project simulates a **Mine Safety Monitoring System** using the **iFogSim** toolkit. The simulation models a network of sensors and processing modules for monitoring safety parameters in a mine environment. It evaluates sensor data and raises alerts when thresholds are exceeded, all while placing various modules across devices like routers, proxy servers, and the cloud.

## Project Overview
The system consists of the following components:

1. **Gas Sensors (gas)**: Measure gas levels.
2. **Chemical Sensors (ch)**: Measure chemical levels.
3. **Environmental Sensors (sr)**: Measure other safety-related parameters.
4. **Master Module**: A central router that collects and processes sensor data.
5. **GasInfo, ChInfo, SrInfo Modules**: Process respective sensor data.
6. **Response Module**: Generates alerts and sends responses to safety-critical events.
7. **Proxy Server**: Hosts the Response Module.
8. **Cloud Storage**: Stores backup data and logs.

The simulation calculates the cost of execution in the cloud over time and evaluates the system's resource allocation.

## Architecture

- **Sensors**: 
   - `gas`  
   - `ch` (chemical sensors)  
   - `sr` (environmental sensors)
- **Master Module**: Resides in the router.
- **GasInfo/ChInfo/SrInfo Modules**: Placed on local edge devices for minimal latency.
- **Response Module**: Hosted on a proxy server.
- **Cloud**: Acts as backup for logging and storage.

## Key Features
- Simulates fog computing-based mine safety monitoring.
- Distributed placement of modules for efficient resource utilization.
- Monitors power consumption using `FogLinearPowerModel`.
- Calculates execution cost for cloud operations.
- Uses iFogSim to evaluate latency, network usage, and execution time.

## Prerequisites
Make sure you have the following software installed:

- **Java Development Kit (JDK)** - Version 8 or above.
- **Eclipse IDE** or any Java IDE.
- **CloudSim Library** (JAR files).
- **iFogSim Library** (download from [iFogSim GitHub](https://github.com/Cloudslab/iFogSim)).


## Simulation Results
The output of the simulation includes:

1. **Latency**: Time taken for sensor data to reach the master module.
2. **Power Consumption**: Energy usage of fog devices.
3. **Execution Cost**: Cost of data processing and storage in the cloud.
<img width="711" alt="image" src="https://github.com/user-attachments/assets/509c667d-1a75-4a96-8a51-f71a56857ecf" />
<img width="658" alt="image" src="https://github.com/user-attachments/assets/7682cc31-a0de-4afa-ab00-1982a3d05dc7" />

## Future Enhancements
- Add dynamic task scheduling for real-time sensor data processing.
- Integrate a dashboard to visualize simulation results.
- Support for additional sensors and modules.

## References
- [CloudSim Toolkit](https://github.com/Cloudslab/cloudsim)
- [iFogSim Toolkit](https://github.com/Cloudslab/iFogSim)

## License
This project is licensed under the MIT License.

---
Developed as part of a mine safety monitoring simulation using fog computing principles with iFogSim.


