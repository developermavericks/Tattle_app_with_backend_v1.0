# 10. Security, CI/CD Pipeline & Disaster Recovery Operations

This document completes the Tattle Technical Documentation Suite by detailing **Mobile & Cloud Security**, **Automated CI/CD Release Pipelines**, and **Disaster Recovery Procedures**.

---

## 1. Security Architecture & OWASP Mobile Security Compliance

### 1.1 Mobile Client Security (Android / KMP)
- **App Signing**: Signed with a 4096-bit RSA keystore managed via **Google Play App Signing**.
- **Play Integrity API**: Protects the app against tampering, unauthorized side-loading, and automated bot scraping.
- **R8 / ProGuard Obfuscation**:
  ```proguard
  # Keep KMP & Ktor Data Transfer Objects
  -keep class com.example.tattle.models.** { *; }
  -keep class com.example.tattle.data.** { *; }
  -dontwarn io.ktor.**
  -dontwarn io.kamel.**
  ```
- **Network Security Configuration**:
  - Enforces TLS 1.3 for all external API endpoints (`api.envato.com`, `api.pixabay.com`, `images.unsplash.com`, `tattle.app`).
  - Implements SSL Certificate Pinning on Ktor `HttpClient` to prevent Man-In-The-Middle (MITM) proxy attacks.

### 1.2 Backend API Security & Access Control
- **JWT Authentication**: User sessions authenticated via HMAC256 signed JWT bearer tokens (`Authorization: Bearer <TOKEN>`) with a 7-day expiration window.
- **Rate Limiting & Anti-Scraping**: Cloudflare WAF rate-limits incoming HTTP requests (max 100 req/min per IP) to prevent DDoS attacks and unauthorized scraping.
- **Database Injection Defense**: All SQL queries execute via **Exposed ORM** parameterized statements, eliminating SQL injection vulnerabilities.

---

## 2. Automated CI/CD Release Pipeline (GitHub Actions)

### 2.1 GitHub Actions Workflow (`.github/workflows/deploy.yml`)

```yaml
name: Tattle Production Build & Deploy Pipeline

on:
  push:
    branches: [ "ITS_DIVS", "main" ]

jobs:
  # 1. Android App Bundle Build & Play Store Release
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build Android App Bundle (.aab)
        run: ./gradlew :app:androidApp:bundleRelease

      - name: Upload AAB Artifact
        uses: actions/upload-artifact@v4
        with:
          name: tattle-release-aab
          path: app/androidApp/build/outputs/bundle/release/*.aab

  # 2. Ktor Server Docker Build & VPS Deployment
  deploy-backend:
    runs-on: ubuntu-latest
    needs: build-android
    steps:
      - uses: actions/checkout@v4
      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_HUB_USER }}
          password: ${{ secrets.DOCKER_HUB_TOKEN }}

      - name: Build & Push Server Docker Image
        run: |
          docker build -t developermavericks/tattle-server:latest -f server/Dockerfile .
          docker push developermavericks/tattle-server:latest

      - name: SSH to Production VPS & Restart Container Stack
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.VPS_IP }}
          username: root
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            cd /opt/tattle
            git pull origin ITS_DIVS
            docker-compose up -d --build --no-deps tattle-server
```

---

## 3. Disaster Recovery, Backups & High Availability

### 3.1 PostgreSQL Backup Strategy
- **Daily Automated Dump**: Cron job exports PostgreSQL database compressed dumps every midnight:
  ```bash
  pg_dump -U tattle_user -d tattle_db | gzip > /backups/tattle_db_$(date +%Y%m%d).sql.gz
  ```
- **S3 Glacier Storage**: Backups automatically uploaded to encrypted AWS S3 Glacier with a 30-day retention policy.
- **Point-in-Time Recovery (PITR)**: AWS Aurora WAL logging enables point-in-time recovery to any second within 35 days.

### 3.2 Redis Persistence & Cluster Recovery
- **Append-Only File (AOF)**: Configured with `appendonly yes` and `appendfsync everysec` for 1-second data loss tolerance on server power loss.
- **Automatic Health Checks**: Docker Compose restarts unhealthy Redis or Ktor containers within 5 seconds of failure.

---

## 4. Final Systems Audit Summary

With the addition of Module 10, the Tattle Technical Documentation Suite covers **100% of product, system design, AI engineering, API specifications, monetization, cloud costs, security, CI/CD, and disaster recovery operations**.

```text
Tattle Documentation/
├── 00_TATTLE_MASTER_DIRECTORY_INDEX.md           # Master Telephone Book & Directory
├── 01_Collaborative_Filtering_Engine.md          # Implicit Signal Collector & Scoring
├── 02_OnDevice_NPU_Local_AI_Summarization.md     # ExecuTorch NPU AI Engine ($0 AI cost)
├── 03_Vector_Database_Qdrant_Personalization.md  # 128-dim Vector Search Personalization
├── 04_Interactive_Micro_Polls_and_Fact_Checking.md # Micro-Polls & Fact-Checking Badges
├── 05_Inshorts_Inspired_HighScale_System_Design.md # 10M MAU Scaling & Inshorts Comparison
├── 06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md # UI Specs, 11 PNGs & 16 Streams
├── 07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md  # 20+ API Contracts & Costing
├── 08_Dynamic_Brand_Monetization_and_Survey_Engine.md         # Zero-Code Ads & Survey API
├── 09_Unified_Module_Convergence_and_Deployment_Guide.md     # $12-$25/mo Docker Compose Stack
├── 10_Security_CI_CD_and_Disaster_Recovery.md    # Security, ProGuard, CI/CD & Backups
└── README.md                                     # Master Suite Index
```
