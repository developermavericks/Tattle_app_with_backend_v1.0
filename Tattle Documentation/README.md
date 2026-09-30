# Tattle Master Technical Documentation Suite

Welcome to the official technical documentation suite for **Tattle** (Kotlin Multiplatform News App + Ktor Netty Backend).

---

## 📚 Documentation Index

| Module # | Document Name | Focus & Contents |
| :---: | :--- | :--- |
| **00** | [📞 Tattle Master Directory Index](./00_TATTLE_MASTER_DIRECTORY_INDEX.md) | **Telephone Directory & Quick Lookup Index** for all features, topics, API endpoints, codebase source files, PNG snapshots, and cost models. |
| **01** | [Collaborative Filtering Engine](./01_Collaborative_Filtering_Engine.md) | Client-side implicit signal tracking (Dwell time, swipe velocity, tap depth) + Ktor KMP telemetry collector & recommendation matrix. |
| **02** | [On-Device NPU Local AI Summarization](./02_OnDevice_NPU_Local_AI_Summarization.md) | ExecuTorch / ONNX Runtime engine running 1B-3B quantized LLMs (Llama 3.2 1B / Qwen 2.5 1.5B) directly on Android NPUs ($0 AI cloud cost). |
| **03** | [Vector Database Qdrant Personalization](./03_Vector_Database_Qdrant_Personalization.md) | Qdrant vector database setup, 128-dimensional content embeddings, and real-time EMA user interest vector updates. |
| **04** | [Interactive Micro-Polls & Fact-Checking](./04_Interactive_Micro_Polls_and_Fact_Checking.md) | 1-tap card back micro-polls, atomic Ktor vote aggregators, and real-time news wire AI claim verification ("Verified Facts 🟢"). |
| **05** | [Inshorts-Inspired High-Scale System Design](./05_Inshorts_Inspired_HighScale_System_Design.md) | Edge caching (Cloudflare + Redis), 10M+ MAU sub-10ms latency architecture, $0 editorial payroll strategy, and full costing models ($0 to 10M users). |
| **06** | [App UI, 16-Sector Real-Time Data Streams & Recap Telemetry](./06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md) | Complete UI screen specs & PNG snapshots, 16 independent real-time data streams, dynamic weighted randomization algorithm, and real-time Daily Recap telemetry engine. |
| **07** | [Complete API Endpoints, Telemetry Pipeline & Scaling Architecture](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md) | Full API specification for all 20+ Inbound and Outbound data endpoints, recommendation engine scaling from 1 to Lakhs to Millions of users, and itemized cloud cost models. |
| **08** | [Dynamic Brand Monetization & Survey Engine](./08_Dynamic_Brand_Monetization_and_Survey_Engine.md) | Zero-code API-driven ad video & multi-MCQ brand survey campaign ingestion pipeline, database schemas, and client telemetry handlers. |
| **09** | [Unified Module Convergence & Single-Infra Deployment Guide](./09_Unified_Module_Convergence_and_Deployment_Guide.md) | Single-infrastructure modular convergence system design combining Ktor API, Redis, PostgreSQL, and Cloudflare WAF into a $12-$25/mo unified Docker Compose deployment stack. |
| **10** | [Security, CI/CD & Disaster Recovery Operations](./10_Security_CI_CD_and_Disaster_Recovery.md) | ProGuard/R8 obfuscation, Play Integrity API, GitHub Actions automated `.aab` & server container deployment pipelines, and PostgreSQL/Redis disaster recovery backups. |
| **11** | [Developer Admin Dashboard & Complete Endpoint Wiring Map](./11_Developer_Admin_Dashboard_and_Endpoint_Wiring_Map.md) | Built-in Developer Visual Web Dashboard (`/admin`), real-time telemetry signal inspector, zero-code ad form, and 3-column endpoint wiring matrix. |
| **12** | [Ultimate Developer Command Center & Enterprise Admin Portal](./12_Ultimate_Developer_Admin_Command_Center.md) | Enterprise Developer Command Center (`/admin/dashboard`), live user metrics, GeoIP heatmap, manual trending pin override, real-time crash radar, user CRM & brand pitch deck generator. |
| **13** | [17-Sector JSON Endpoints & Controlled AI Pipeline](./13_17Sector_JSON_Endpoints_and_Controlled_AI_Pipeline.md) | 17 independent JSON sector stream endpoints, standardized JSON payloads, local Ollama ($0/mo) vs Groq API toggle, and developer dashboard controls. |
| **14** | [Age-Aware ML Content Serving Architecture](./14_Age_Aware_ML_Content_Serving_Architecture.md) | API serving layer age mapping (`user_age < 18` ➔ `kids` + `teens`, `user_age >= 18` ➔ `young_adult` + `general` + `mature`), zero DB migrations, and KMP client request flow. |
| **15** | [ML Backend Containerization & Unified Docker Deployment](./15_ML_Backend_Containerization_and_Unified_Docker_Deployment.md) | Python ML backend FastAPI edits, Dockerfile, and unified 4-service `docker-compose.yml` packaging Ktor, ML Backend, PostgreSQL & Redis together. |
| **16** | [Daily 5:00 AM IST Ingestion Pipeline, Local Dry-Run & Distribution](./16_Daily_5AM_Automated_Pipeline_Local_DryRun_and_App_Distribution.md) | Automated daily 5:00 AM IST news batch pipeline, Redis pre-warming, local dry-run test procedure, and direct APK / Play Store Internal Testing beta distribution. |
| **17** | [Auth, SMS, Email & Push Notification Verification Guide](./17_Auth_SMS_Email_and_Push_Notification_Verification_Guide.md) | Google Sign-In verification with `GoogleIdTokenVerifier`, real SMS OTP via Twilio/Fast2SMS, email magic passcodes via SendGrid, and FCM push notifications. |
| **18** | [Google Cloud Master Deployment & Android APK Release Guide](./18_Google_Cloud_Master_Deployment_and_APK_Release_Guide.md) | Master 5-container `docker-compose.yml`, signed Android APK/AAB compilation commands, and step-by-step GCP Compute Engine VM deployment commands. |
| **19** | [Multi-Provider Free AI Cascade Strategy ($0 AI Spend)](./19_Multi_Provider_Free_AI_Cascade_Strategy.md) | Chaining Groq (Tier 1) ➔ Gemini 1.5 Flash (Tier 2) ➔ DeepSeek (Tier 3) ➔ Local Ollama (Tier 4) for 3,500+ daily articles processed at $0.00 total spend. |

---

## 🚀 Key Architectural Highlights
- **100% Shared Kotlin Multiplatform Codebase** (Android, iOS, Desktop, Web).
- **3-Tier Interactive Card Deck Stack** with 0ms pre-fetched images.
- **Zero Editorial Overhead**: 100% automated AI pipeline.
- **Sub-10ms Latency at 10M MAU** for **~$23,400/month** total cloud spend.
