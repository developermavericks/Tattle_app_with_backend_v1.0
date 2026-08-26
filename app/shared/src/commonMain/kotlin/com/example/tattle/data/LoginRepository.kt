package com.example.tattle.data

import com.example.tattle.PlatformConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val token: String? = null, val phone: String? = null, val email: String? = null, val otp: String? = null)

@Serializable
data class AuthResponseData(val success: Boolean, val token: String? = null, val message: String? = null)

@Serializable
data class SimpleResponseData(val success: Boolean, val message: String)

class LoginRepository(
    private val client: HttpClient,
    private val preferencesRepository: PreferencesRepository
) {
    private val baseUrl = PlatformConfig.BASE_URL

    suspend fun googleLogin(idToken: String): Result<String> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(token = idToken))
            }.body()

            if (response.success && response.token != null) {
                Result.success(response.token)
            } else {
                Result.failure(Exception(response.message ?: "Google login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateOtp(phone: String): Result<String> {
        return try {
            val response: SimpleResponseData = client.post("$baseUrl/api/auth/otp/generate") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(phone = phone))
            }.body()
            
            if (response.success) {
                Result.success("OTP sent")
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<String> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/otp/verify") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(phone = phone, otp = otp))
            }.body()

            if (response.success && response.token != null) {
                Result.success(response.token)
            } else {
                Result.failure(Exception(response.message ?: "OTP verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateEmailOtp(email: String): Result<String> {
        return try {
            val response: SimpleResponseData = client.post("$baseUrl/api/auth/email/generate") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email = email))
            }.body()
            
            if (response.success) {
                Result.success("OTP sent")
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyEmailOtp(email: String, otp: String): Result<String> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/email/verify") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email = email, otp = otp))
            }.body()

            if (response.success && response.token != null) {
                Result.success(response.token)
            } else {
                Result.failure(Exception(response.message ?: "Email verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
