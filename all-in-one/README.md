# Hyades All-in-One Docker Stack

This project provides a solution to build and run the full **OWASP Dependency-Track (Hyades)** stack in a single 
Docker container. It combines **Frontend**, **API Server**, **Repository & Vulnerability Analyzers**, **PostgreSQL**, 
**Kafka**, and **Nginx** into one image managed by the **S6-Overlay** process manager.

## 🚀 Part 1: User Guide

### Requirements
Linux / Windows (WSL2) & Docker / Podman

### 1. Build
Use the `build-all-in-one.sh` script to create the image. It executes a single multi-stage Docker build process.

```bash
chmod +x build-all-in-one.sh
./build-all-in-one.sh
```

### 2. Run
Use the `run-all-in-one.sh` script to start the container.

```bash
chmod +x run-all-in-one.sh
./run-all-in-one.sh
```

**After starting:**
* The application is available at: http://localhost:8080
* API Version Check: http://localhost:8080/api/version

## 🛠 Part 2: Technical Solutions

The solution relies on **S6-Overlay** to manage multiple services within a single container. It acts as a lightweight 
init system, ensuring the correct startup order and stability.

### 1. Dockerfile Architecture
The project uses a single **Multi-Stage Dockerfile** (`Dockerfile.all-in-one`). The build process is divided into three stages:

* **Global ARGs**: Define versions for the API Server, Frontend, and Analyzers, allowing specific releases to be built via build arguments (default is `snapshot`).
* **Stage 1: Base System Setup (`base-system`)**:
  * Based on `eclipse-temurin:25-jre`
  * Installs system dependencies, **Kafka**, and **S6-Overlay** binaries.
  * Configures environment variables for PostgreSQL and Kafka.
* **Stage 2: Artifact Collection**:
  * Fetches compiled artifacts (JARs) and frontend static files from the official upstream Docker images.
* **Stage 3: Final Image Construction**:
  * Combines the **Base System** with the collected **Artifacts**.
  * Configures **Nginx** to listen on port **9090** (allowing non-root execution).
  * Copies and sanitizes the `s6-config/` service definitions (CRLF to LF).

### 2. Process Management (S6-Overlay)
Operating as the container's root process (PID 1), **S6-Overlay** actively supervises the entire service stack. 

It ensures:
* **Dependencies:** Strict startup order (Postgres/Kafka -> API -> Analyzers -> Nginx).
* **Lifecycle:** Automatic restart of failed services.
* **Shutdown:** Graceful termination of all processes.

### 3. Service Configuration (`s6-config/`)
Service orchestration is defined statically in the `s6-config/` directory. 
During the build, this folder is copied to `/etc/s6-overlay/s6-rc.d`. The Dockerfile automatically handles permissions (`chmod +x`) and converts Windows line endings (`CRLF -> LF`), ensuring the container runs correctly regardless of the host OS.

#### Structure
Each service (e.g., `kafka`, `apiserver`, `init-postgres`) has its own directory containing:
* **`type`**: Defines the service lifecycle (e.g., `longrun` for daemons, `oneshot` for initialization tasks).
* **`run`**: The S6 entry point script. In this project, it acts as a minimalist wrapper that executes the logic script.
* **`script.sh`**: Contains the actual service logic (environment variables, loops). This separation is necessary because the S6 `run` file does not handle complex Bash execution correctly.
* **`dependencies.d/`**: Defines the startup order (e.g., `apiserver` depends on `init-postgres` and `kafka`).