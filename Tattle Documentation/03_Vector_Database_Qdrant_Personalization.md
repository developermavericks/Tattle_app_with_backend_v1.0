# 03. Vector Database (Qdrant) Personalization Architecture

This document describes how Tattle uses **Qdrant Vector Database** and **128-dimensional content embeddings** to deliver hyper-personalized story feeds.

---

## 1. Overview & Mechanics

Traditional category filters match broad tags (e.g. `tech` or `ai`). Vector Search converts every news story into a 128-dimensional mathematical vector reflecting semantic meaning, sentiment, and topic nuances.

When a user reads, likes, or saves stories, their **User Interest Vector** updates dynamically in real-time. Qdrant calculates **Cosine Similarity** between the user's vector and incoming news stories to deliver a hyper-personalized feed.

```
Article Embeddings (128-dim)  ──┐
                                ├─►  Qdrant Cosine Distance  ──►  Top Personalized Feed
User Interest Vector (128-dim) ──┘
```

---

## 2. Qdrant Collection Setup (Ktor Backend)

```kotlin
package com.example.tattle.vector

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class QdrantVectorPoint(
    val id: String,
    val vector: List<Float>,
    val payload: Map<String, String>
)

@Serializable
data class QdrantSearchRequest(
    val vector: List<Float>,
    val limit: Int = 20,
    val with_payload: Boolean = true
)

class QdrantVectorService(
    private val client: HttpClient,
    private val qdrantUrl: String = "http://localhost:6333"
) {
    suspend fun upsertArticleVector(articleId: String, vector: List<Float>, category: String, headline: String) {
        val payload = QdrantVectorPoint(
            id = articleId,
            vector = vector,
            payload = mapOf("category" to category, "headline" to headline)
        )

        client.put("$qdrantUrl/collections/tattle_articles/points") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("points" to listOf(payload)))
        }
    }

    suspend fun searchPersonalizedFeed(userVector: List<Float>, limit: Int = 20): List<String> {
        val request = QdrantSearchRequest(vector = userVector, limit = limit)
        val response = client.post("$qdrantUrl/collections/tattle_articles/points/search") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        // Extract matching article IDs ordered by cosine similarity
        return emptyList()
    }
}
```

---

## 3. Real-Time User Vector Updating Algorithm

When a user interacts with an article $A$ (with vector $V_A$), the user's vector $U_{t}$ updates via an **Exponential Moving Average (EMA)** with learning rate $\alpha$:

$$U_{t+1} = (1 - \alpha \cdot W) \cdot U_t + (\alpha \cdot W) \cdot V_A$$

Where:
- $\alpha = 0.05$ (Learning rate)
- $W$ = Interaction Weight from Telemetry (Dwell time + Interaction bonus)

This allows the user's feed to adapt dynamically within 3 swipes to emerging interests while maintaining long-term preference memory.
