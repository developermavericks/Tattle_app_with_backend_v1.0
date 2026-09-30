# 15. ML Backend Containerization & Unified Docker Deployment

This document provides the complete, step-by-step guide for **editing, containerizing, and packaging the Python ML Backend** (`Tattle_Machine_learning_model_for_articles_v1.0`) together with the **Ktor Backend Server** into a single `docker-compose.yml` deployment stack.

---

## 1. Required Code Edits in ML Backend Repository

In the ML Backend repository (`Tattle_Machine_learning_model_for_articles_v1.0`), make the following 2 quick edits:

### Edit A: Update `app/api.py` (FastAPI Endpoint)
Add the `user_age` query parameter to filter articles based on permitted age brackets:

```python
from fastapi import FastAPI, Query
from typing import List, Optional

@app.get("/api/feed/{sector}")
async def get_age_appropriate_feed(
    sector: str,
    user_age: Optional[int] = Query(default=18, description="User age in years"),
    limit: int = 20
):
    # Map user_age to permitted ML database brackets
    if user_age < 18:
        permitted_brackets = ("kids", "teens")
    else:
        permitted_brackets = ("young_adult", "general", "mature")

    query = """
        SELECT id, headline, brief, bullets_json, why_it_matters, full_text, image_url, age_bracket
        FROM articles
        WHERE sector = :sector
          AND age_bracket IN :permitted_brackets
        ORDER BY quality_score DESC, published_at DESC
        LIMIT :limit
    """
    return await database.fetch_all(query=query, values={"sector": sector, "permitted_brackets": permitted_brackets, "limit": limit})
```

---

## 2. Dockerfile for ML Backend (`Dockerfile`)

In the root of the `Tattle_Machine_learning_model_for_articles_v1.0` repository, create a `Dockerfile`:

```dockerfile
# Dockerfile for Python ML Backend
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy dependencies & install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy ML application source code
COPY . .

# Expose FastAPI port
EXPOSE 8000

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8000/health || exit 1

# Start FastAPI server using Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

---

## 3. Unified Production `docker-compose.yml`

In the main Tattle repository (`Tattle_app_with_backend_v1.0`), use this unified `docker-compose.yml` to package **all 4 services** together:

```yaml
version: '3.8'

services:
  # 1. Ktor Core Backend Server
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

  # 3. Shared Production PostgreSQL Database
  postgres-db:
    image: postgres:16-alpine
    container_name: tattle-postgres
    restart: always
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

  # 4. Shared In-Memory Redis Cache
  redis-cache:
    image: redis:7-alpine
    container_name: tattle-redis
    restart: always
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
```

---

## 4. Single Command Deployment

To deploy the entire converged infrastructure on a $12/month Cloud VPS (Hetzner / DigitalOcean):

```bash
docker-compose up -d --build
```

### Deployed Endpoints:
- ⚡ **Ktor API Engine**: `http://<YOUR_VPS_IP>:8081`
- 🖥️ **Developer Command Center**: `http://<YOUR_VPS_IP>:8081/admin/dashboard`
- 🤖 **Python ML Service**: `http://<YOUR_VPS_IP>:8000`
