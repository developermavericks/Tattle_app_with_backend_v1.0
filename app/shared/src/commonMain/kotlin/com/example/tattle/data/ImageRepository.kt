package com.example.tattle.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayHit>
)

@Serializable
data class PixabayHit(
    val id: Int,
    val webformatURL: String,
    val largeImageURL: String,
    val tags: String
)

class ImageRepository(private val client: HttpClient) {
    private val apiKey = "57288715-79ef2e54282e42425a088bb2e"
    private val baseUrl = "https://pixabay.com/api/"

    suspend fun searchImage(query: String): String? {
        if (query.isBlank()) return null
        return try {
            val cleanQuery = query.take(100) // Pixabay limit
            val response: PixabayResponse = client.get(baseUrl) {
                parameter("key", apiKey)
                parameter("q", cleanQuery)
                parameter("image_type", "photo")
                parameter("per_page", 3)
                parameter("safesearch", "true")
            }.body()
            
            response.hits.firstOrNull()?.webformatURL
        } catch (e: Exception) {
            println("Pixabay search failed for '$query': ${e.message}")
            null
        }
    }

    suspend fun searchImages(query: String, perPage: Int = 10): List<String> {
        return try {
            val response: PixabayResponse = client.get(baseUrl) {
                parameter("key", apiKey)
                parameter("q", query)
                parameter("image_type", "photo")
                parameter("per_page", perPage)
                parameter("safesearch", "true")
            }.body()
            
            response.hits.map { it.webformatURL }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
