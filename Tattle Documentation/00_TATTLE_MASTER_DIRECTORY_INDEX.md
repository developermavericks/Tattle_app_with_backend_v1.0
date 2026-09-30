# 📞 TATTLE MASTER DIRECTORY & PROJECT COMPREHENSIVE INDEX

Welcome to the **Tattle Master Project Directory & Quick-Lookup Catalog**. This document functions as a master index and "telephone book" for the entire Tattle codebase, system architecture, API endpoints, UI screens, monetization models, and deployment configurations.

> [!TIP]
> **How to use this directory**: Search or scroll to any feature, topic, API endpoint, or cost question below to find its exact document link, code implementation path, and architectural specification.

---

## 📂 Master Module Index (Clickable Directory)

| Module # | File Link | Focus & Primary Contents | Key Topics Covered |
| :---: | :--- | :--- | :--- |
| **00** | **[Master Index](./00_TATTLE_MASTER_DIRECTORY_INDEX.md)** *(This File)* | Project Overview, Master Directory, Feature Map & Directory Index | All system docs, UI screenshots, codebase map |
| **01** | **[Collaborative Filtering Engine](./01_Collaborative_Filtering_Engine.md)** | Implicit Telemetry Tracking & Server Scoring Matrix | Dwell time, swipe velocity, tap depth, Ktor telemetry ingestion |
| **02** | **[On-Device NPU Local AI Summarization](./02_OnDevice_NPU_Local_AI_Summarization.md)** | ExecuTorch / ONNX Local NPU Inference Engine | Llama 3.2 1B / Qwen 2.5 1.5B, $0 AI cloud cost, `AndroidNPUEngine.kt` |
| **03** | **[Vector Database Qdrant Personalization](./03_Vector_Database_Qdrant_Personalization.md)** | Qdrant Vector Search & Content Embeddings | 128-dim vectors, Cosine similarity, Exponential Moving Average (EMA) |
| **04** | **[Interactive Micro-Polls & Fact-Checking](./04_Interactive_Micro_Polls_and_Fact_Checking.md)** | Micro-Polls & Real-Time Credibility Engine | 1-tap poll voting, atomic SQL aggregators, "Verified Facts 🟢" badge |
| **05** | **[Inshorts-Inspired System Design](./05_Inshorts_Inspired_HighScale_System_Design.md)** | 10M MAU Scaling, Zero Editorial Cost & Cost Models | Cloudflare + Redis edge caching, $0 editorial staff, $0-$23.4k/mo costing |
| **06** | **[UI Specs, 16 Sector Streams & Recap Telemetry](./06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md)** | Complete UI Layout Specs, 11 PNG Snapshots & Real-Time Recap | 11 screen PNGs, 16 real-time sector streams, Softmax feed randomizer |
| **07** | **[API Endpoints, Telemetry & Scaling Architecture](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md)** | Complete Inbound/Outbound API Reference & Scaling Blueprint | 20+ API JSON schemas, 1 to 10M users scaling phases, itemized costs |
| **08** | **[Dynamic Brand Monetization & Survey Engine](./08_Dynamic_Brand_Monetization_and_Survey_Engine.md)** | Zero-Code Brand Ad & Survey Ingestion API | `POST /api/monetization/campaigns`, `AdWatchDialog`, `SurveyCard` |
| **09** | **[Unified Module Convergence & Deployment Guide](./09_Unified_Module_Convergence_and_Deployment_Guide.md)** | Single-Infra Modular Convergence & Docker Compose Stack | $12-$25/mo unified container stack (`docker-compose.yml`), zero sprawl |
| **10** | **[Security, CI/CD & Disaster Recovery Operations](./10_Security_CI_CD_and_Disaster_Recovery.md)** | Mobile & Cloud Security, GitHub Actions CI/CD & DB Backups | ProGuard/R8 rules, GitHub Actions `.aab` pipeline, PostgreSQL WAL backups |
| **11** | **[Developer Admin Dashboard & Endpoint Wiring Map](./11_Developer_Admin_Dashboard_and_Endpoint_Wiring_Map.md)** | Built-in Developer Visual Web Dashboard & Wiring Matrix | Ktor Web Dashboard (`/admin`), live signal monitor, 3-column endpoint wiring map |
| **12** | **[Ultimate Developer Command Center & Enterprise Admin Portal](./12_Ultimate_Developer_Admin_Command_Center.md)** | Enterprise Command Center & Brand Monetization Hub | `/admin/dashboard`, live active users, GeoIP heatmap, manual trending pin, crash radar & brand pitch deck generator |
| **13** | **[17-Sector JSON Endpoints & Controlled AI Pipeline](./13_17Sector_JSON_Endpoints_and_Controlled_AI_Pipeline.md)** | 17 Independent Sector JSON Ingestion Endpoints & Controlled AI | 17 sector JSON contracts, Local Ollama ($0/mo) vs Groq API toggle switch |
| **14** | **[Age-Aware ML Content Serving Architecture](./14_Age_Aware_ML_Content_Serving_Architecture.md)** | API Serving Layer Age Mapping & KMP Integration | `user_age < 18` ➔ `kids` + `teens`, `user_age >= 18` ➔ `young_adult` + `general` + `mature`, zero DB migrations |
| **15** | **[ML Backend Containerization & Unified Docker Deployment](./15_ML_Backend_Containerization_and_Unified_Docker_Deployment.md)** | Python ML Backend Edits, Dockerfile & Unified Compose Stack | Python FastAPI edits, Dockerfile, unified 4-service `docker-compose.yml` packaging Ktor, ML Backend, Postgres & Redis |
| **16** | **[Daily 5:00 AM IST Ingestion Pipeline, Local Dry-Run & Distribution](./16_Daily_5AM_Automated_Pipeline_Local_DryRun_and_App_Distribution.md)** | Automated Daily 5:00 AM IST Pipeline, Local Dry-Run & Beta Distribution | Daily Cron poller, Redis pre-warming, local dry-run steps, APK / Play Store Internal Testing beta distribution |
| **17** | **[Auth, SMS, Email & Push Notification Verification Guide](./17_Auth_SMS_Email_and_Push_Notification_Verification_Guide.md)** | Google Auth, SMS OTP, Email Passcodes & FCM Push Setup | Google One-Tap + `GoogleIdTokenVerifier`, Twilio/Fast2SMS SMS API, SendGrid Email API, FCM Push Notifications |
| **18** | **[Google Cloud Master Deployment & Android APK Release Guide](./18_Google_Cloud_Master_Deployment_and_APK_Release_Guide.md)** | Master 5-Container Docker Stack, Signed APK/AAB & GCP Deployment | Master `docker-compose.yml`, `./gradlew :assembleRelease`, Google Compute Engine VM commands |
| **19** | **[Multi-Provider Free AI Cascade & Parallel Ingestion Pipeline](./19_Multi_Provider_Free_AI_Cascade_Strategy.md)** | 17 Concurrent Parallel Workers & Free AI Cascade ($0 Spend) | `asyncio.gather` parallel workers, Groq Llama 3 ➔ Gemini Flash ➔ DeepSeek ➔ Local Ollama cascade |
| **Master** | **[README.md](./README.md)** | Master Suite Index & Navigation Hub | Executive summary & documentation suite navigation | shelter |


---

## 🔍 Topic & Feature Quick-Lookup Index ("Telephone Book")

Looking for something specific? Use the table below to find the exact file and section:

### 🅰️ Analytics, Telemetry & Recommendations
- **Implicit Signal Tracking (Dwell Time, Swipe Velocity, Tap Depth)** ➔ [`01_Collaborative_Filtering_Engine.md`](./01_Collaborative_Filtering_Engine.md#2-client-side-implicit-signal-collector-kotlin-multiplatform)
- **Batch Signal Dispatcher (`SignalTracker.kt`)** ➔ [`01_Collaborative_Filtering_Engine.md`](./01_Collaborative_Filtering_Engine.md#22-client-side-telemetry-collector-signaltrackerkt)
- **Server Telemetry Ingestion Endpoint (`POST /api/telemetry/signals`)** ➔ [`07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md`](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md#21-post-apitelemetrysignals)
- **Softmax Temperature-Controlled Feed Randomization** ➔ [`06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md`](./06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md#21-main-feed-dynamic-weighted-randomization-algorithm)
- **Qdrant Vector Personalization (128-dim embeddings)** ➔ [`03_Vector_Database_Qdrant_Personalization.md`](./03_Vector_Database_Qdrant_Personalization.md)

---

### 🅱️ AI Summarization & Local NPU Execution
- **On-Device ExecuTorch NPU Inference (`AndroidNPUEngine.kt`)** ➔ [`02_OnDevice_NPU_Local_AI_Summarization.md`](./02_OnDevice_NPU_Local_AI_Summarization.md#32-android-npu-engine-implementation-androidnpuenginekt)
- **4-Bit Quantized Models (Llama 3.2 1B / Qwen 2.5 1.5B)** ➔ [`02_OnDevice_NPU_Local_AI_Summarization.md`](./02_OnDevice_NPU_Local_AI_Summarization.md#2-model-quantization-pipeline)
- **AI Brief Generation Fallback Pipeline** ➔ [`02_OnDevice_NPU_Local_AI_Summarization.md`](./02_OnDevice_NPU_Local_AI_Summarization.md#4-fallback-architecture)

---

### 🅒 Cloud Infrastructure, Costs & Scaling
- **Cost for 10 to 100 Beta Users ($0.00 / month)** ➔ [`07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md`](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md#4-itemized-cloud-infrastructure-costing-model)
- **Cost for 100,000 Users (Lakhs) (~$800 / month)** ➔ [`07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md`](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md#4-itemized-cloud-infrastructure-costing-model)
- **Cost for 10,000,000 Users (~$23,400 / month)** ➔ [`05_Inshorts_Inspired_HighScale_System_Design.md`](./05_Inshorts_Inspired_HighScale_System_Design.md#3-comprehensive-cloud-cost-model-10-to-10000000-users)
- **Unified Single-Infra Docker Compose Setup ($12 - $25/mo)** ➔ [`09_Unified_Module_Convergence_and_Deployment_Guide.md`](./09_Unified_Module_Convergence_and_Deployment_Guide.md#3-unified-production-docker-composeyml)
- **Inshorts vs Tattle Comparison (60% Lower Cost)** ➔ [`05_Inshorts_Inspired_HighScale_System_Design.md`](./05_Inshorts_Inspired_HighScale_System_Design.md#1-architectural-lessons--adaptations-from-inshorts)

---

### 🅹 Monetization, Ads & Surveys
- **Dynamic Brand Ad & Survey Ingestion API (`POST /api/monetization/campaigns`)** ➔ [`08_Dynamic_Brand_Monetization_and_Survey_Engine.md`](./08_Dynamic_Brand_Monetization_and_Survey_Engine.md#31-post-apimonetizationcampaigns-admin--brand-campaign-ingestion)
- **Reader Check-In Choice Dialog (`CheckInChoiceDialog.kt`)** ➔ [`06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md`](./06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md#11-screen-by-screen-snapshots--ui-specifications)
- **1-Hour Free Unlimited Reading Pass** ➔ [`08_Dynamic_Brand_Monetization_and_Survey_Engine.md`](./08_Dynamic_Brand_Monetization_and_Survey_Engine.md#33-post-apimonetizationadimpression-outbound-ad-telemetry)
- **Ad View & Survey Answer Telemetry** ➔ [`08_Dynamic_Brand_Monetization_and_Survey_Engine.md`](./08_Dynamic_Brand_Monetization_and_Survey_Engine.md#34-post-apimonetizationsurveysubmit-outbound-survey-answers)

---

### MM News Streams & Image Resolution
- **16 Independent Real-Time Sector Streams Architecture** ➔ [`06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md`](./06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md#2-architecture-for-16-independent-real-time-sector-data-streams)
- **Envato Discovery Search API Proxy (`GET /api/images/search`)** ➔ [`07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md`](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md#17-get-apiimagessearch)
- **Direct Compose Native Image Decoding (`decodeToImageBitmap()`)** ➔ [`07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md`](./07_Complete_API_Endpoints_Telemetry_and_Scaling_Design.md#53-softmax-feed-randomizer-articlerepositorykt)

---

### 🆂 UI Screens, Snapshots & User Flow
- **Complete Screen Flow Map (Mermaid)** ➔ [`06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md`](./06_App_UI_16Sector_Realtime_Streams_and_Recap_Telemetry.md#1-application-ui-structure-screenshots--screen-flow)
- 📱 **[Splash Screen Snapshot](./snapshots/splash_screen.png)**
- 📱 **[Onboarding Screen Snapshot](./snapshots/onboarding_screen.png)**
- 📱 **[Main Feed Screen Snapshot](./snapshots/feed_screen.png)**
- 📱 **[Executive Brief View Snapshot](./snapshots/brief_view.png)**
- 📱 **[Full Article View Snapshot](./snapshots/full_view.png)**
- 📱 **[Explore Screen Snapshot](./snapshots/explore_screen.png)**
- 📱 **[Trending Screen Snapshot](./snapshots/trending_screen.png)**
- 📱 **[Saved Screen Snapshot](./snapshots/saved_screen.png)**
- 📱 **[Daily Recap Screen Snapshot](./snapshots/recap_screen.png)**
- 📱 **[Settings Screen Snapshot](./snapshots/settings_screen.png)**
- 📱 **[Share Card Dialog Snapshot](./snapshots/share_dialog.png)**

---

## 🛠️ Codebase Source File Map

The list below maps key source files in the project to their functionality:

```text
F:\Tech-Folder\MyApplication\
├── app/shared/src/commonMain/kotlin/com/example/tattle/
│   ├── App.kt                          # Root KMP Navigation, CompositionLocal & Overlays
│   ├── PlatformConfig.kt               # Base URLs & BuildKonfig environment parameters
│   ├── data/
│   │   ├── ArticleRepository.kt        # Feed fetching, 1.5s fast timeouts, Softmax & caching
│   │   ├── ImageRepository.kt          # Envato API search, Unsplash fallbacks & ImageBitmap decode
│   │   ├── LoginRepository.kt          # Phone/Email OTP authentication & profile server sync
│   │   ├── PreferencesRepository.kt    # Preferences state flow & persistent storage
│   │   └── MockData.kt                 # 16-sector fallback news data
│   ├── di/
│   │   └── Koin.kt                     # Dependency Injection, Ktor HttpClient (HttpRedirect)
│   ├── models/
│   │   ├── Article.kt                  # ArticleDto, UserPreferences, NotificationsPrefs
│   │   └── TourState.kt                # Deprecated tour step definitions
│   └── ui/
│       ├── components/
│       │   ├── ArticleCard.kt          # 3D Deck Card UI, gesture drag/swipes, native image draw
│       │   ├── AdWatchDialog.kt        # Simulated 5s video ad player
│       │   ├── CheckInChoiceDialog.kt  # Check-in choice dialog (Watch Ad vs 4-MCQ Survey)
│       │   ├── SurveyCard.kt           # 4-MCQ Reader Survey card flow
│       │   ├── ShareCardDialog.kt      # Rich share dialog with WhatsApp/Telegram/X app chips
│       │   └── SkeletonCard.kt         # Shimmer loading card placeholder
│       ├── screens/
│       │   ├── SplashScreen.kt         # World map backdrop, Google/Phone/Email OTP overlays
│       │   ├── OnboardingScreen.kt     # Step 1 DOB (DDMMYYYY) + Step 2 Push Notifications
│       │   ├── FeedScreen.kt           # 3-Tier Card Stack Deck, 15+ card backup queue
│       │   ├── BriefView.kt            # 30s Executive Brief, 3 bullets, Why It Matters
│       │   ├── FullView.kt             # Full journalistic narrative text reader
│       │   ├── ExploreScreen.kt        # 16 Sector Grid cards
│       │   ├── TrendingScreen.kt       # Ranked 1-10 viral news list
│       │   ├── SavedScreen.kt          # Offline bookmarked stories
│       │   ├── RecapScreen.kt          # 16-Sector Thin Bar Chart, 4 Metric Tiles, AI Insights
│       │   └── SettingsScreen.kt       # Dark Mode, Profile Editor, Logout
│       └── viewmodels/
│           └── AppViewModel.kt         # Central UI View State Management
└── server/src/main/kotlin/com/example/tattle/
    └── Application.kt                  # Embedded Ktor Netty Server, SQLite/Postgres DB & Endpoints
```

---

## 📌 Summary
For any deep dive into system design, API schemas, AI execution, or costing models, click any module link in the **Master Module Index** above.
