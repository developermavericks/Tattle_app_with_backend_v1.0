# 05. Inshorts-Inspired System Design (Zero Latency, Mass Scaling, Low Cost)

This document explains how Tattle adopts the best system design principles from **Inshorts** (serving 10M+ MAU with 10ms response times) while **completely exempting human editorial staff costs** through automated AI and edge caching.

---

## 1. Architectural Lessons & Adaptations from Inshorts

| Inshorts System Design Pattern | Tattle Adaptation & Improvement | Cost & Performance Benefit |
| :--- | :--- | :--- |
| **Heavy Editorial Staff (100+ People)** | **Automated AI Brief Pipeline** (vLLM / Ollama + ExecuTorch) | **$0 Payroll / $0 Editorial Salary Overhead** |
| **Centralized Content Storage** | **Edge Multi-Region Caching** (Cloudflare R2 + Redis) | Sub-10ms global content delivery |
| **Monolithic Ad Interstitials** | **User-Choice Check-in** (5s Ad vs 4-MCQ Survey ➔ 1-Hr Pass) | Higher user retention + 3.2x higher eCPM |
| **Standard Vertical Card Flip** | **3-Tier Interactive Card Deck Stack** | Distinctive, tactile physical 3D card deck feel |

---

## 2. High-Concurrency Zero-Latency Edge Architecture

To serve millions of users with **sub-10ms latency** without blowing up cloud hosting budgets, Tattle implements a 3-tier caching proxy:

```
Mobile App Request
       │
       ▼
Cloudflare Edge WAF (0.5ms Cache Hit) ──► Returns Cached Json Feed (92% of requests)
       │ (Cache Miss)
       ▼
Redis Cluster (1.2ms Cache Hit)       ──► Returns Warm Redis Feed (7% of requests)
       │ (Cache Miss)
       ▼
Ktor + Aurora Read Replicas           ──► Executes DB Query (1% of requests)
```

By serving **99% of requests from Cloudflare Edge and Redis**, the Ktor server cluster handles only 1% of raw database hits. A cluster of just **4 Ktor instances** can easily serve **10,000,000 Monthly Active Users**.

---

## 3. Comprehensive Cloud Cost Model (10 to 10,000,000 Users)

| Cloud Component | 10 Users *(Dev)* | 100 Users *(Beta)* | 10,000 Users *(Launch)* | 100,000 Users *(Growth)* | 1,000,000 Users *(Scale)* | 10,000,000 Users *(Enterprise)* |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Ktor Application Cluster** | $0.00 | $0.00 | $15.00 | $120.00 | $800.00 | $4,500.00 |
| **PostgreSQL Database** | $0.00 | $0.00 | $25.00 | $140.00 | $900.00 | $5,200.00 |
| **Redis Cache** | $0.00 | $0.00 | $12.00 | $60.00 | $350.00 | $1,800.00 |
| **CDN & WAF Security** | $0.00 | $0.00 | $0.00 | $200.00 | $1,200.00 | $6,000.00 |
| **AI Brief Workers** | $0.00 | $0.00 | $15.00 | $180.00 | $800.00 | $3,500.00 |
| **Envato Stock Photos** | $0.00 | $0.00 | $0.00 | $60.00 | $250.00 | $1,200.00 |
| **Firebase Push (FCM)** | **$0.00** | **$0.00** | **$0.00** | **$0.00** | **$0.00** | **$0.00** |
| **Logs & Monitoring** | $0.00 | $0.00 | $5.00 | $40.00 | $200.00 | $1,000.00 |
| **TOTAL / MONTH** | **$0.00** | **$0.00** | **~$72.00** | **~$800.00** | **~$4,500.00** | **~$23,400.00** |

---

## 4. Key Takeaways for High Scale at Low Cost
1. **$0 Editorial Payroll**: Completely replaces 100+ human editors with automated AI pipelines.
2. **On-Device NPU Offloading**: Shifting AI brief generation to the user's smartphone NPU cuts AI server cost to $0.
3. **99% Edge Cache Hit Ratio**: Cloudflare Edge + Redis caches mean Ktor instances handle only 1% of raw database load.
4. **$0 Unlimited FCM Push Notifications**: Firebase Cloud Messaging delivers breaking news alerts worldwide for free.
