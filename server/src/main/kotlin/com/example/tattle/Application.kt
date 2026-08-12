package com.example.tattle

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class User(val email: String, val password: String, val phoneNumber: String? = null)

@Serializable
data class AuthResponse(val success: Boolean, val message: String)

// Simulated Database
val users = mutableListOf(
    User("developerteam@themavericksindia.com", "12345")
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Tattle Backend Live")
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
