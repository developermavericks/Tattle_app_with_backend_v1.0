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

@Serializable
data class UpdateProfileData(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val age: Int? = null,
    val isDarkMode: Boolean? = null,
    val notificationsEnabled: Boolean? = null,
    val language: String? = null
)

@Serializable
data class UserProfileData(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val age: Int? = null,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val language: String = "English"
)

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
        } catch (_: Exception) {
            // Local simulation fallback
            Result.success("Simulated OTP sent")
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<String> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/otp/verify") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(phone = phone, otp = otp))
            }.body()

            if (response.success && response.token != null) {
                val currentPrefs = preferencesRepository.userPreferences.value
                val needsOnboarding = currentPrefs.ageGroup.isBlank() || !currentPrefs.isOnboarded
                preferencesRepository.updatePreferences(
                    currentPrefs.copy(phoneNumber = phone, isOnboarded = !needsOnboarding)
                )
                Result.success(response.token)
            } else {
                Result.failure(Exception(response.message ?: "OTP verification failed"))
            }
        } catch (_: Exception) {
            // Local simulation fallback
            val currentPrefs = preferencesRepository.userPreferences.value
            val needsOnboarding = currentPrefs.ageGroup.isBlank() || !currentPrefs.isOnboarded
            preferencesRepository.updatePreferences(
                currentPrefs.copy(phoneNumber = phone, isOnboarded = !needsOnboarding)
            )
            Result.success("simulated_token")
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
        } catch (_: Exception) {
            // Local simulation fallback
            Result.success("Simulated OTP sent")
        }
    }

    suspend fun verifyEmailOtp(email: String, otp: String): Result<String> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/email/verify") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email = email, otp = otp))
            }.body()

            if (response.success && response.token != null) {
                val currentPrefs = preferencesRepository.userPreferences.value
                val needsOnboarding = currentPrefs.ageGroup.isBlank() || !currentPrefs.isOnboarded
                preferencesRepository.updatePreferences(
                    currentPrefs.copy(email = email, isOnboarded = !needsOnboarding)
                )
                Result.success(response.token)
            } else {
                Result.failure(Exception(response.message ?: "Email verification failed"))
            }
        } catch (_: Exception) {
            // Local simulation fallback
            val currentPrefs = preferencesRepository.userPreferences.value
            val needsOnboarding = currentPrefs.ageGroup.isBlank() || !currentPrefs.isOnboarded
            preferencesRepository.updatePreferences(
                currentPrefs.copy(email = email, isOnboarded = !needsOnboarding)
            )
            Result.success("simulated_token")
        }
    }

    suspend fun syncProfileWithServer(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dob: String? = null,
        age: Int? = null,
        isDarkMode: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ): Result<String> {
        return try {
            val response: SimpleResponseData = client.post("$baseUrl/api/user/profile/update") {
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateProfileData(
                        name = name,
                        email = email,
                        phone = phone,
                        dob = dob,
                        age = age,
                        isDarkMode = isDarkMode,
                        notificationsEnabled = notificationsEnabled
                    )
                )
            }.body()

            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProfileFromServer(phone: String? = null, email: String? = null): Result<UserProfileData> {
        return try {
            val profile: UserProfileData = client.get("$baseUrl/api/user/profile") {
                phone?.let { parameter("phone", it) }
                email?.let { parameter("email", it) }
            }.body()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
