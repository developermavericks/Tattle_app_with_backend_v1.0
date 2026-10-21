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
                AuthSession.save(response.token)
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
                Result.success(response.message ?: "OTP sent")       // on the LAN pilot the message carries the code: "Dev code: 123456"
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Cannot reach the Tattle server. Check that your phone is on the same Wi-Fi as the PC."))
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<Boolean> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/otp/verify") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(phone = phone, otp = otp))
            }.body()

            if (response.success && response.token != null) {
                AuthSession.save(response.token)                      // from here every request is this user's
                val serverProfile = fetchProfileFromServer(phone = phone).getOrNull()
                val currentPrefs = preferencesRepository.userPreferences.value

                val hasServerDob = !serverProfile?.dob.isNullOrBlank() || serverProfile?.age != null
                val userAgeGroup = serverProfile?.age?.toString() ?: currentPrefs.ageGroup
                val userDob = serverProfile?.dob ?: currentPrefs.dob
                val needsOnboarding = !hasServerDob && userDob.isBlank()

                val finalPrefs = if (!needsOnboarding) {
                    currentPrefs.copy(
                        phoneNumber = phone,
                        dob = userDob,
                        ageGroup = userAgeGroup,
                        isOnboarded = true
                    )
                } else {
                    currentPrefs.copy(
                        phoneNumber = phone,
                        dob = "",
                        ageGroup = "",
                        isOnboarded = false
                    )
                }

                preferencesRepository.updatePreferences(finalPrefs)
                Result.success(needsOnboarding)
            } else {
                Result.failure(Exception(response.message ?: "OTP verification failed"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Cannot reach the Tattle server. Check your Wi-Fi and try again."))
        }
    }

    suspend fun generateEmailOtp(email: String): Result<String> {
        return try {
            val response: SimpleResponseData = client.post("$baseUrl/api/auth/email/generate") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email = email))
            }.body()
            
            if (response.success) {
                Result.success(response.message ?: "OTP sent")
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Cannot reach the Tattle server. Check that your phone is on the same Wi-Fi as the PC."))
        }
    }

    suspend fun verifyEmailOtp(email: String, otp: String): Result<Boolean> {
        return try {
            val response: AuthResponseData = client.post("$baseUrl/api/auth/email/verify") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(email = email, otp = otp))
            }.body()

            if (response.success && response.token != null) {
                AuthSession.save(response.token)
                val serverProfile = fetchProfileFromServer(email = email).getOrNull()
                val currentPrefs = preferencesRepository.userPreferences.value

                val hasServerDob = !serverProfile?.dob.isNullOrBlank() || serverProfile?.age != null
                val userAgeGroup = serverProfile?.age?.toString() ?: currentPrefs.ageGroup
                val userDob = serverProfile?.dob ?: currentPrefs.dob
                val needsOnboarding = !hasServerDob && userDob.isBlank()

                val finalPrefs = if (!needsOnboarding) {
                    currentPrefs.copy(
                        email = email,
                        dob = userDob,
                        ageGroup = userAgeGroup,
                        isOnboarded = true
                    )
                } else {
                    currentPrefs.copy(
                        email = email,
                        dob = "",
                        ageGroup = "",
                        isOnboarded = false
                    )
                }

                preferencesRepository.updatePreferences(finalPrefs)
                Result.success(needsOnboarding)
            } else {
                Result.failure(Exception(response.message ?: "Email verification failed"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Cannot reach the Tattle server. Check your Wi-Fi and try again."))
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
