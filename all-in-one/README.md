# Hyades All-in-One Docker Stack

This project provides a solution to build and run the full **OWASP Dependency-Track (Hyades)** stack in a single 
Docker container. It combines **Frontend**, **API Server**, **Repository & Vulnerability Analyzers**, **PostgreSQL**, 
**Kafka**, and **Nginx** into one image managed by the **S6-Overlay** process manager.

## 🚀 Part 1: User Guide

### Requirements
Linux / Windows (WSL2) & Docker / Podman

### 1. Build
Use the `build-all-in-one.sh` script to create the images. It sequentially builds the base layer (system dependencies) and the final layer (application).

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

The project uses a two-stage build architecture and a declarative **S6-Overlay** configuration to ensure reliability, clarity, and ease of maintenance.

### 1. Dockerfile Architecture
The build is split into two files to optimize caching and speed up CI/CD processes:

* **`Dockerfile.all-in-one-base` (System Dependencies)**
    * Contains "heavy" and rarely changing dependencies.
    * Installs S6-Overlay binaries.
    * Built rarely, only when infrastructure component versions are updated.
* **`Dockerfile.all-in-one` (Application)**
    * Inherits from `Dockerfile.all-in-one-base`.
    * Copies application artifacts (JARs, Frontend) from official upstream images.
    * Configures Nginx and Copies the static S6 service definitions from s6-config/. 
    * Sanitizes scripts by automatically converting Windows line endings 
      (CRLF) to UNIX format (LF) during the build.
    * Built frequently, on every code change.

### 2. Process Management (S6-Overlay)
Since the container runs multiple services simultaneously, **S6-Overlay** is used as the init system (PID 1).

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