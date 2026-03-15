# Quick Start

Get Dependency-Track running locally in minutes.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/)
- [Compose v2](https://docs.docker.com/compose/install/)

## Setup

Download the Compose file and start the stack:

=== ":fontawesome-brands-linux: Linux / :fontawesome-brands-apple: macOS"

    ```shell linenums="1"
    curl -O https://raw.githubusercontent.com/DependencyTrack/hyades/main/docs/getting-started/docker-compose.quickstart.yml
    docker compose -f docker-compose.quickstart.yml up
    ```

=== ":fontawesome-brands-windows: Windows (PowerShell)"

    ```powershell linenums="1"
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/DependencyTrack/hyades/main/docs/getting-started/docker-compose.quickstart.yml" -OutFile "docker-compose.quickstart.yml"
    docker compose -f docker-compose.quickstart.yml up
    ```

This will pull the required images, initialize the database, and start all services.

??? note "Compose file contents"
    ```yaml linenums="1"
    --8<-- "docs/getting-started/docker-compose.quickstart.yml"
    ```

## Access

Once the stack is up, open the frontend:

**<http://localhost:8081>**

Log in with the default credentials:

| Username | Password |
|----------|----------|
| `admin`  | `admin`  |

!!! note
    You'll be prompted to change your password upon first login.

## What's Next

- [Changes over v4](changes-over-v4.md): What's different in version 5
- TODO: Upload your first BOM.
- [Configuration](../operations/configuration/overview.md): Customize your deployment
- [Scaling](../operations/scaling.md): Scale for production workloads
