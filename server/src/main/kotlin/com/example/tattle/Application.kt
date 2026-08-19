package com.example.tattle

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Serializable
data class User(val email: String, val password: String, val phoneNumber: String? = null)

@Serializable
data class AuthResponse(val success: Boolean, val message: String)

@Serializable
data class OtpRequest(val phoneNumber: String)

@Serializable
data class OtpVerifyRequest(val phoneNumber: String, val otp: String)

@Serializable
data class GatewayRegisterRequest(val ip: String, val port: Int)

@Serializable
data class SmsRequest(val phone: String, val message: String)

// Global State
val users = mutableListOf(
    User("developerteam@themavericksindia.com", "12345")
)

val otpStore = ConcurrentHashMap<String, String>() // phoneNumber -> otp
var gatewayAddress: String? = null

val client = HttpClient(CIO) {
    install(ClientContentNegotiation) {
        json()
    }
}

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Tattle Backend Live. Gateway: $gatewayAddress")
        }

        // --- Gateway Discovery ---
        post("/api/gateway/register") {
            val req = call.receive<GatewayRegisterRequest>()
            gatewayAddress = "http://${req.ip}:${req.port}"
            println("Gateway registered at: $gatewayAddress")
            call.respond(mapOf("status" to "registered"))
        }

        // --- OTP Logic ---
        post("/api/otp/generate") {
            val req = call.receive<OtpRequest>()
            val generatedOtp = (100000..999999).random().toString()
            otpStore[req.phoneNumber] = generatedOtp
            
            println("Generated OTP for ${req.phoneNumber}: $generatedOtp")

            val currentGateway = gatewayAddress
            if (currentGateway == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "SMS Gateway not registered"))
                return@post
            }

            try {
                val response = client.post("$currentGateway/send-sms") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer TATTLE_CLEO_CM_MAVS")
                    setBody(SmsRequest(req.phoneNumber, "Your Tattle OTP is: $generatedOtp"))
                }
                
                if (response.status == HttpStatusCode.OK) {
                    call.respond(mapOf("status" to "sent"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Gateway returned ${response.status}"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to reach gateway: ${e.message}"))
            }
        }

        post("/api/otp/verify") {
            val req = call.receive<OtpVerifyRequest>()
            val storedOtp = otpStore[req.phoneNumber]
            
            if (storedOtp == req.otp) {
                otpStore.remove(req.phoneNumber)
                call.respond(AuthResponse(true, "OTP Verified"))
            } else {
                call.respond(AuthResponse(false, "Invalid OTP"))
            }
        }

        post("/login") {
            val credentials = call.receive<User>()
            val user = users.find { it.email == credentials.email && it.password == credentials.password }
            if (user != null) {
                call.respond(AuthResponse(true, "Login successful"))
            } else {
                call.respond(AuthResponse(false, "Invalid credentials"))
            }
        }

        post("/register") {
            val newUser = call.receive<User>()
            if (users.any { it.email == newUser.email }) {
                call.respond(AuthResponse(false, "User already exists"))
            } else {
                users.add(newUser)
                call.respond(AuthResponse(true, "Registration successful"))
            }
        }
    }
}
