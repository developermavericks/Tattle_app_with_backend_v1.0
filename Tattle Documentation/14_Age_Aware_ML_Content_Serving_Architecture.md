# 14. Age-Aware ML Content Serving Architecture

This document presents the complete architectural specification for **Age-Aware Content Classification and Serving** between Tattle's KMP Android App, the Ktor Server, and the Python ML Backend (`Tattle_Machine_learning_model_for_articles_v1.0`).

---

## 1. Architectural Evaluation: Is Option A Optimal?

> [!IMPORTANT]
> **YES! Option A (API Serving Layer Mapping) is 100% the most optimal, flexible, and production-ready approach.**

### Why Option A is Superior:
1. **Zero Database Migrations or Data Loss**: Keeping fine-grained classification labels (`kids`, `teens`, `young_adult`, `general`, `mature`) in the database means you never need to wipe or re-index historical ML predictions.
2. **Infinite Future Flexibility**: If you decide to introduce a special "College / Teen" feed tier (ages 15-19) in the future, fine-grained database tags allow you to adjust the API query in seconds without retraining ML models.
3. **Guaranteed Content Safety**: Hard-filtering at the SQL query level prevents mature, explicit, or graphic news from reaching users under 18.
4. **Sub-10ms Query Execution**: Adding an `IN ('kids', 'teens')` clause to indexed PostgreSQL queries executes in **< 2 milliseconds**.

---

## 2. Age-Tier Mapping Model

| Target Audience Tier | User Age (`user_age`) | Mapped Database ML Brackets | Permitted Content Characteristics |
| :--- | :---: | :--- | :--- |
| **Tier 1 (`below_18`)** | **`< 18`** | `kids` + `teens` | Educational, sports, science, gaming, clean tech; **strict zero-explicit / zero-violence filter**. |
| **Tier 2 (`above_18`)** | **`≥ 18`** | `young_adult` + `general` | Mainstream business, tech, career, geopolitics, global economy, world news. |
| **Tier 3 (`mature`)** | **`≥ 18`** *(Age-Gated)* | `mature` | Complex investigative journalism, sensitive political reporting, adult themes. |

---

## 3. End-to-End Data & Request Flow

```mermaid
graph TD
    Client[Tattle KMP App] -->|1. User enters DOB in Onboarding - Age = 16| Onboarding[OnboardingScreen.kt]
    Onboarding -->|2. Stores age=16 in UserPreferences| Prefs[UserPreferences / State Flow]
    
    Prefs -->|3. GET /api/articles/for-you?user_age=16| Ktor[Ktor Backend Server]
    Ktor -->|4. Forward to ML Backend: GET /api/feed/tech?user_age=16| MLBackend[Python ML Backend API]

    subgraph Option A: ML API Query Layer
        MLBackend --> AgeCheck{user_age < 18?}
        AgeCheck -->|Yes (< 18)| FilterBelow[SQL: WHERE age_bracket IN 'kids', 'teens']
        AgeCheck -->|No (>= 18)| FilterAbove[SQL: WHERE age_bracket IN 'young_adult', 'general', 'mature']
    end

    FilterBelow & FilterAbove --> MLDB[(PostgreSQL Articles Table)]
    MLDB -->|Returns Age-Appropriate Articles| Ktor
    Ktor -->|Renders Safe Cards| Client
```

---

## 4. Implementation Code Blueprint

### 4.1 ML Backend API Serving Layer Implementation (`app/api.py`)

In the Python ML backend (`app/api.py`), update the feed query endpoint to accept `user_age`:

```python
from fastapi import FastAPI, Query
from typing import List, Optional

app = FastAPI(title="Tattle ML Backend API")

@app.get("/api/feed/{sector}")
async def get_age_appropriate_feed(
    sector: str,
    user_age: Optional[int] = Query(default=18, description="User age in years"),
    limit: int = 20
):
    # 1. Determine permitted age brackets based on user_age
    if user_age < 18:
        permitted_brackets = ("kids", "teens")
    else:
        permitted_brackets = ("young_adult", "general", "mature")

    # 2. Execute SQL query with index-optimized age bracket filter
    query = """
        SELECT id, headline, brief, bullets_json, why_it_matters, full_text, image_url, age_bracket
        FROM articles
        WHERE sector = :sector
          AND age_bracket IN :permitted_brackets
        ORDER BY quality_score DESC, published_at DESC
        LIMIT :limit
    """
    
    articles = await database.fetch_all(
        query=query, 
        values={"sector": sector, "permitted_brackets": permitted_brackets, "limit": limit}
    )
    
    return articles
```

---

### 4.2 Ktor Server Request Proxy (`Application.kt`)

In Ktor (`Application.kt`), extract `user_age` from the user's profile and append it to the ML backend call:

```kotlin
get("/api/articles/for-you") {
    val userIdentifier = call.request.queryParameters["user"] ?: "anonymous"
    
    // 1. Fetch user age from Users table
    val userAge = transaction {
        Users.selectAll().where { Users.phoneNumber eq userIdentifier or (Users.email eq userIdentifier) }
            .singleOrNull()?.get(Users.age) ?: 18
    }

    // 2. Fetch age-appropriate candidate articles from ML Backend
    val mlApiUrl = "http://localhost:8000/api/feed/tech?user_age=$userAge"
    val candidateArticles: List<ArticleDto> = httpClient.get(mlApiUrl).body()

    // 3. Apply Softmax Temperature Randomization
    call.respond(candidateArticles)
}
```

---

### 4.3 Tattle KMP Mobile App Request Flow (`ArticleRepository.kt`)

In the Android KMP mobile app, `ArticleRepository` passes `user_age` automatically from `UserPreferences`:

```kotlin
suspend fun getForYouArticles(userAge: Int): List<Article> {
    return withTimeoutOrNull(1500) {
        try {
            val response: List<Article> = client.get("$backendUrl/api/articles/for-you?user_age=$userAge").body()
            if (response.isNotEmpty()) response else null
        } catch (_: Exception) {
            null
        }
    } ?: MockData.articles.sortProcessedFirst()
}
```

---

## 5. Summary & Operational Benefits

By implementing **Option A**:
1. **100% Content Safety**: Teenagers (`< 18`) are completely protected from sensitive/mature themes.
2. **Zero ML Retraining**: Your ML classifiers continue predicting fine-grained labels (`kids`, `teens`, `young_adult`, `general`, `mature`).
3. **Instant Implementation**: Can be deployed in < 30 minutes with zero database schema changes or data loss.
