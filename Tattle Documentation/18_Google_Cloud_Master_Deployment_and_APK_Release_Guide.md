# 18. Google Cloud Master Deployment & Android APK Release Guide

This document presents the complete step-by-step master guide for **Compiling & Containerizing the Entire Backend Stack**, **Building the Signed Android Release APK/AAB**, and **Deploying the Entire Infrastructure on Google Cloud Platform (GCP)**.

---

## 1. Complete Unified Master Backend Stack (`docker-compose.yml`)

The entire backend infrastructure—**Ktor Core API Server**, **Python ML Scraper & Filter**, **Local Ollama AI Summarizer**, **Envato Stock Photo Matcher**, **PostgreSQL 16 Database**, and **Redis 7 Cache**—is packaged into a single, unified `docker-compose.yml` file.

```mermaid
graph TD
    UserApp[Android App - Release APK] --> Cloudflare[Cloudflare Edge WAF]
    Cloudflare --> GCP[Google Cloud Compute Engine VM]

    subgraph Unified Converged Container Stack
        GCP --> KtorBackend[1. tattle-backend - Ktor Netty Server Port 8081]
        GCP --> MLBackend[2. tattle-ml-backend - Python ML Scraper Port 8000]
        GCP --> OllamaAI[3. ollama-ai - Local Ollama LLM Container Port 11434]
        GCP --> PostgresDB[(4. postgres-db - PostgreSQL 16 Port 5432)]
        GCP --> RedisCache[(5. redis-cache - Redis 7 In-Memory Port 6379)]

        KtorBackend --> MLBackend
        KtorBackend --> OllamaAI
        KtorBackend --> PostgresDB
        KtorBackend --> RedisCache
    end
```

---

### Master Production `docker-compose.yml`

In your root project directory (`Tattle_app_with_backend_v1.0`), use this master `docker-compose.yml`:

```yaml
version: '3.8'

services:
  # 1. Ktor Core Backend Server Engine
  tattle-server:
    build:
      context: .
      dockerfile: server/Dockerfile
    container_name: tattle-backend
    restart: always
    ports:
      - "8081:8081"
    environment:
      - PORT=8081
      - DATABASE_URL=jdbc:postgresql://postgres-db:5432/tattle_db
      - DATABASE_USER=tattle_user
      - DATABASE_PASSWORD=tattle_secure_password
      - ML_BACKEND_URL=http://tattle-ml-backend:8000
      - OLLAMA_URL=http://ollama-ai:11434
      - REDIS_HOST=redis-cache
      - REDIS_PORT=6379
      - ENVATO_API_TOKEN=OP6NdHQ7vl9vb4TNaOqloZdaf0c9A2UF
    depends_on:
      postgres-db:
        condition: service_healthy
      redis-cache:
        condition: service_healthy
      tattle-ml-backend:
        condition: service_healthy

  # 2. Python ML Scraper & Age-Aware Filter Service
  tattle-ml-backend:
    build:
      context: ../Tattle_Machine_learning_model_for_articles_v1.0
      dockerfile: Dockerfile
    container_name: tattle-ml-backend
    restart: always
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql://tattle_user:tattle_secure_password@postgres-db:5432/tattle_db
      - REDIS_HOST=redis-cache
    depends_on:
      postgres-db:
        condition: service_healthy

  # 3. Local Ollama AI Engine ($0 Cloud AI Spend)
  ollama-ai:
    image: ollama/ollama:latest
    container_name: ollama-ai
    restart: always
    ports:
      - "11434:11434"
    volumes:
      - ollama_storage:/root/.ollama

  # 4. Production PostgreSQL 16 Database
  postgres-db:
    image: postgres:16-alpine
    container_name: tattle-postgres
    restart: always
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: tattle_db
      POSTGRES_USER: tattle_user
      POSTGRES_PASSWORD: tattle_secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tattle_user -d tattle_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  # 5. Production Redis 7 In-Memory Cache
  redis-cache:
    image: redis:7-alpine
    container_name: tattle-redis
    restart: always
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --maxmemory 512mb --maxmemory-policy allkeys-lru
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
  redis_data:
  ollama_storage:
```

---

## 2. STEP 2: Building the Signed Android Release APK & AAB

### 2.1 Build the Signed Release APK
To compile the Android app into an installable `.apk` file:

```bash
./gradlew :app:androidApp:assembleRelease
```

- **APK Output Location**:
  `app/androidApp/build/outputs/apk/release/app-release.apk`
- You can distribute `app-release.apk` directly to beta users via Google Drive, WhatsApp, or Firebase App Distribution!

---

### 2.2 Build the Android App Bundle (AAB for Google Play Store)
To build the `.aab` file for publishing on the Google Play Store:

```bash
./gradlew :app:androidApp:bundleRelease
```

- **AAB Output Location**:
  `app/androidApp/build/outputs/bundle/release/app-release.aab`
- Upload `app-release.aab` directly to **Google Play Console** ➔ **Production** or **Internal Testing**.

---

## 3. STEP 3: Deploying the Entire Backend Stack on Google Cloud Platform (GCP)

Follow these exact steps to deploy your entire unified backend container stack on **Google Cloud Compute Engine (GCP)**:

### Step 3.1: Create a Google Cloud Project & VM Instance
1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Click **Create Project** ➔ Name it `Tattle-Production`.
3. In the GCP Navigation Menu, go to **Compute Engine ➔ VM instances**.
4. Click **Create Instance**:
   - **Name**: `tattle-backend-vm`
   - **Region**: `asia-south1 (Mumbai)` or `us-central1`
   - **Machine Type**: `e2-standard-2` (2 vCPU, 8 GB RAM) or `e2-medium`
   - **Boot Disk**: `Ubuntu 22.04 LTS` (30 GB Disk)
   - **Firewall**: Check **Allow HTTP traffic** and **Allow HTTPS traffic**.
5. Click **Create**.

---

### Step 3.2: SSH into GCP VM & Install Docker
1. On the GCP VM Instances page, click the **SSH** button next to `tattle-backend-vm`.
2. Run these commands inside the SSH terminal to install Docker & Docker Compose:

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install -y docker-compose-plugin docker-compose

# Verify Docker installation
docker --version
docker-compose --version
```

---

### Step 3.3: Clone Repository & Launch Docker Stack
In the SSH terminal:

```bash
# Clone the repository
git clone https://github.com/developermavericks/Tattle_app_with_backend_v1.0.git
cd Tattle_app_with_backend_v1.0

# Clone the Python ML Backend repository into adjacent directory
cd ..
git clone https://github.com/developermavericks/Tattle_Machine_learning_model_for_articles_v1.0.git
cd Tattle_app_with_backend_v1.0

# Pull local Ollama model weights
docker run -d --name ollama-pull ollama/ollama:latest
docker exec -it ollama-pull ollama pull llama3.2:1b
docker stop ollama-pull && docker rm ollama-pull

# Launch the entire 5-container backend stack!
docker-compose up -d --build
```

---

### Step 3.4: Open Firewall Ports on Google Cloud
In Google Cloud Console:
1. Go to **VPC network ➔ Firewall**.
2. Click **Create Firewall Rule**:
   - **Name**: `allow-tattle-ports`
   - **Target tags**: `all-instances-in-network`
   - **Source IPv4 ranges**: `0.0.0.0/0`
   - **Protocols and ports**: Specified protocols and ports ➔ `tcp: 8081, 8000`
3. Click **Create**.

---

### Step 3.5: Verify Your Live Deployed Endpoints
Your entire backend infrastructure is now live on Google Cloud!

- ⚡ **Ktor API Engine**: `http://<YOUR_GCP_VM_IP>:8081`
- 🖥️ **Developer Command Center**: `http://<YOUR_GCP_VM_IP>:8081/admin/dashboard`
- 🤖 **Python ML Service**: `http://<YOUR_GCP_VM_IP>:8000`
- 🧠 **Local Ollama AI Engine**: `http://<YOUR_GCP_VM_IP>:11434`
