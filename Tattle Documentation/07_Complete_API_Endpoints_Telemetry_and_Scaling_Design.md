# 07. Complete API Endpoint Specification, Telemetry Pipeline & Scaling Architecture

This document presents the complete API reference for all **Inbound (Data to App)** and **Outbound (Data from App)** endpoints, the **Recommendation Engine Data Pipeline**, the **Scaling Architecture** from 1 user to Lakhs to Millions of users, and the **Overhead Costing Model**.

---

## 1. Inbound Data Endpoints (Data Delivered to App)

Below are the complete API contracts for data requested by the mobile/desktop app.

### 1.1 `GET /api/sectors`
- **Purpose**: Fetches all available news sector categories for Explore Screen grid & navigation tabs.
- **Query Params**: None
- **Response `200 OK`**:
  ```json
  {
    "sectors": [
      "ai", "tech", "world news", "money and business", "science and space",
      "climate and environment", "geopolitics", "healthcare", "gaming",
      "sports", "lifestyle", "startups", "creator economy", "education",
      "pop culture", "media and entertainment"
    ]
  }
  ```

---

### 1.2 `GET /api/articles/for-you`
- **Purpose**: Returns the Main Feed curated using Temperature-Controlled Softmax Randomization based on user telemetry weights.
- **Headers**: `Authorization: Bearer <JWT_TOKEN>` (optional)
- **Response `200 OK`**:
  ```json
  [
    {
      "id": "art_tech_002",
      "category": "tech",
      "readTime": "30s read",
      "headline": "Quantum-Safe Encryption Standards Standardized Globally",
      "hook": "International tech consortiums officially approve post-quantum cryptographic algorithms.",
      "brief": "Governments and tech giants have initiated the largest cryptographic transition in digital history.",
      "bullets": [
        "NIST officially releases final post-quantum encryption standards.",
        "Financial networks mandated to transition by end of 2027.",
        "Lattice-based cryptography forms the core defense mechanism."
      ],
      "whyItMatters": "Protects critical global banking and communications infrastructure against quantum computer decryption capabilities.",
      "fullText": "The global technology sector reached a historic milestone today...",
      "publisher": "CyberWire",
      "publishedAt": "25m ago",
      "imageUrl": "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80",
      "likes": 890,
      "commentsCount": 42,
      "isBreaking": false,
      "views": 5600
    }
  ]
  ```

---

### 1.3 `GET /api/mobile/articles/{sector}`
- **Purpose**: Fetches news stories from 1 of the 16 independent sector data streams.
- **Path Param**: `sector` (e.g., `ai`, `tech`, `geopolitics`, `healthcare`, `startups`).
- **Response `200 OK`**: List of `ArticleDto` objects matching the specified sector stream.

---

### 1.4 `GET /api/articles/trending`
- **Purpose**: Fetches the top 10 viral headlines ranked by view count and view velocity $\frac{\Delta \text{Views}}{\Delta t}$.
- **Response `200 OK`**: List of 10 `ArticleDto` objects.

---

### 1.5 `GET /api/articles/{id}`
- **Purpose**: Fetches a single article full narrative by ID.
- **Path Param**: `id` (e.g., `art_ai_001`).
- **Response `200 OK`**: Single `ArticleDto` object.

---

### 1.6 `POST /api/articles/batch`
- **Purpose**: Batch fetches multiple articles by ID array (used for Saved/Bookmarked stories screen).
- **Request Body**:
  ```json
  {
    "ids": ["art_ai_001", "art_tech_002", "art_biz_003"]
  }
  ```
- **Response `200 OK`**: List of `ArticleDto` objects.

---

### 1.7 `GET /api/images/search`
- **Purpose**: Server-side proxy for Envato Discovery Search API.
- **Query Param**: `term` (e.g. `semiconductor microchip`).
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "imageUrl": "https://preview.photodune.net/files/12345/preview.jpg?w=800&q=80",
    "source": "envato"
  }
  ```

---

### 1.8 `GET /api/user/profile`
- **Purpose**: Fetches the user's profile and settings from Ktor backend server.
- **Query Param**: `phone` or `email` (e.g., `?phone=+919876543210`).
- **Response `200 OK`**:
  ```json
  {
    "id": 102,
    "name": "Alex Mercer",
    "email": "alex@tattle.app",
    "phone": "+919876543210",
    "dob": "15082002",
    "age": 24,
    "isDarkMode": true,
    "notificationsEnabled": true,
    "language": "English"
  }
  ```

---

### 1.9 `GET /api/user/bookmarks` & `GET /api/user/likes`
- **Purpose**: Fetches all bookmarked/liked article IDs for a specific user identifier.
- **Query Param**: `userIdentifier`
- **Response `200 OK`**: Array of string article IDs `["art_ai_001", "art_biz_003"]`.

---

### 1.10 `GET /api/polls/{articleId}`
- **Purpose**: Fetches question, options, and vote breakdown for an article micro-poll.
- **Path Param**: `articleId`
- **Response `200 OK`**:
  ```json
  {
    "articleId": "art_ai_001",
    "question": "Will AI replace 30% of software jobs by 2028?",
    "optionA": "Yes, likely",
    "optionB": "No, unlikely",
    "votesA": 1240,
    "votesB": 860,
    "percentageA": 59.0,
    "percentageB": 41.0
  }
  ```

---

## 2. Outbound Data Endpoints (Data Sent from App to Server)

### 2.1 `POST /api/telemetry/signals`
- **Purpose**: Transmits batch implicit user telemetry signals for real-time recommendation scoring.
- **Request Body**:
  ```json
  {
    "userIdentifier": "+919876543210",
    "events": [
      {
        "articleId": "art_ai_001",
        "category": "ai",
        "dwellTimeMs": 6400,
        "swipeVelocityPxMs": 1.45,
        "tapDepthLevel": 2,
        "interactionType": "BRIEF_OPEN",
        "timestamp": 1774360000000
      },
      {
        "articleId": "art_tech_002",
        "category": "tech",
        "dwellTimeMs": 12000,
        "swipeVelocityPxMs": 0.85,
        "tapDepthLevel": 3,
        "interactionType": "FULL_OPEN",
        "timestamp": 1774360012000
      }
    ]
  }
  ```
- **Response `200 OK`**: `{"success": true, "message": "Signals processed successfully"}`

---

### 2.2 `POST /api/user/bookmarks/toggle`
- **Purpose**: Toggles bookmark/save state for an article.
- **Request Body**:
  ```json
  {
    "userIdentifier": "+919876543210",
    "articleId": "art_ai_001"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "isBookmarked": true,
    "bookmarks": ["art_ai_001", "art_tech_002"]
  }
  ```

---

### 2.3 `POST /api/user/likes/toggle`
- **Purpose**: Toggles like state and returns updated total like count.
- **Request Body**: `{"userIdentifier": "+919876543210", "articleId": "art_ai_001"}`
- **Response `200 OK`**: `{"success": true, "isLiked": true, "likesCount": 1241}`

---

### 2.4 `POST /api/user/shares/record`
- **Purpose**: Records story share event for telemetry and analytics.
- **Request Body**: `{"userIdentifier": "+919876543210", "articleId": "art_ai_001", "platform": "whatsapp"}`
- **Response `200 OK`**: `{"success": true, "message": "Share recorded successfully"}`

---

### 2.5 `POST /api/polls/vote`
- **Purpose**: Submits an atomic vote for a micro-poll and returns live percentage results.
- **Request Body**: `{"articleId": "art_ai_001", "selectedOption": "A", "userIdentifier": "+919876543210"}`
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "votesA": 1241,
    "votesB": 860,
    "percentageA": 59.1,
    "percentageB": 40.9
  }
  ```

---

### 2.6 `POST /api/user/profile/update`
- **Purpose**: Updates user profile (Name, DOB, Age, Phone, Email, Dark Mode, Notifications).
- **Request Body**:
  ```json
  {
    "name": "Alex Mercer",
    "email": "alex@tattle.app",
    "phone": "+919876543210",
    "dob": "15082002",
    "age": 24,
    "isDarkMode": true,
    "notificationsEnabled": true
  }
  ```
- **Response `200 OK`**: `{"success": true, "message": "Profile updated successfully"}`

---

### 2.7 OTP Auth Endpoints (`/api/auth/otp/generate`, `/api/auth/otp/verify`)
- **Purpose**: Generates and verifies 6-digit OTP codes for Phone & Email login.
- **Request Body (`generate`)**: `{"phone": "+919876543210"}`
- **Request Body (`verify`)**: `{"phone": "+919876543210", "otp": "123456"}`
- **Response `200 OK`**: `{"success": true, "token": "JWT_BEARER_TOKEN_HERE"}`

---

## 3. Recommendation Engine Architecture: From 1 User to Lakhs to Millions

```mermaid
graph TD
    subgraph Phase 1: Dev (1 - 1,000 Users)
        KtorDev[Single Ktor Instance] --> SQLite[(SQLite DB tattle.db)]
    end

    subgraph Phase 2: Lakhs (100,000 Users)
        ALB2[Load Balancer] --> KtorCluster2[Ktor Server Cluster - 4 Instances]
        KtorCluster2 --> Redis2[(Redis Cluster - Feed & Signal Cache)]
        KtorCluster2 --> Aurora2[(AWS Aurora PostgreSQL Serverless)]
    end

    subgraph Phase 3: Millions (10,000,000 Users)
        Edge[Cloudflare Anycast WAF] --> ALB3[AWS Load Balancer]
        ALB3 --> KtorCluster3[Ktor Kubernetes Auto-Scaling - 30 Instances]
        KtorCluster3 --> Kafka[Kafka Telemetry Ingestion Queue]
        Kafka --> SignalWorker[Worker Threads - Exponential Moving Average Engine]
        SignalWorker --> Redis3[(Redis Pre-Computed Feeds)]
        KtorCluster3 --> Aurora3[(AWS Aurora Multi-AZ + 3 Read Replicas)]
    end
```

---

### 3.1 Data Flow at 100,000 Users (Lakhs)
1. **Batch Ingestion**: Client batches 10 telemetry events and sends 1 POST request to `/api/telemetry/signals`.
2. **Ktor Ingestion**: Ktor writes signals asynchronously to Redis in-memory lists without blocking HTTP response threads.
3. **Asynchronous Weight Update**: A background coroutine updates the user's category weight vector in PostgreSQL:
   $$U_{\text{new}} = 0.95 \cdot U_{\text{old}} + 0.05 \cdot W_{\text{signal}}$$
4. **Softmax Feed Generation**: When `/api/articles/for-you` is requested, Ktor fetches the top 50 candidate articles from the 16 sector streams, applies Softmax probability sampling using $U_{\text{new}}$, and returns the personalized 20-card list in **< 8ms**.

---

## 4. Itemized Cloud Infrastructure Costing Model

| Component | 1 User *(Local)* | 1,000 Users | 100,000 Users *(Lakhs)* | 1,000,000 Users | 10,000,000 Users |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Ktor App Servers** | $0.00 | $0.00 | $120.00 / mo | $800.00 / mo | $4,500.00 / mo |
| **PostgreSQL Database** | $0.00 | $0.00 | $140.00 / mo | $900.00 / mo | $5,200.00 / mo |
| **Redis Cache Cluster** | $0.00 | $0.00 | $60.00 / mo | $350.00 / mo | $1,800.00 / mo |
| **CDN & WAF (Cloudflare)** | $0.00 | $0.00 | $200.00 / mo | $1,200.00 / mo | $6,000.00 / mo |
| **AI Brief Generation** | $0.00 | $0.00 | $180.00 / mo | $800.00 / mo | $3,500.00 / mo |
| **Stock Photo API** | $0.00 | $0.00 | $60.00 / mo | $250.00 / mo | $1,200.00 / mo |
| **Firebase Push (FCM)** | **$0.00** | **$0.00** | **$0.00** | **$0.00** | **$0.00** |
| **Logs & Monitoring** | $0.00 | $0.00 | $40.00 / mo | $200.00 / mo | $1,000.00 / mo |
| **TOTAL / MONTH** | **$0.00** | **$0.00** | **~$800.00 / mo** | **~$4,500.00 / mo** | **~$23,400.00 / mo** |

---

## 5. Client & Server Code Implementation Architecture

### 5.1 KMP Client Telemetry Integration (`SignalTracker.kt`)
- Captures dwell time, swipe velocity, tap depth, bookmarks, likes, shares, reactions.
- Dispatches batched HTTP JSON requests to `/api/telemetry/signals`.

### 5.2 Server Ingestion Endpoint (`Application.kt`)
- Ingests batch telemetry without blocking HTTP response.
- Updates `UserCategoryWeightsTable` using atomic SQL expressions.

### 5.3 Softmax Feed Randomizer (`ArticleRepository.kt`)
- Applies Softmax probability sampling over the 16 sector streams to guarantee diversity while honoring user category weights.
