package com.example.tattle

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// --- Database Schema ---
object Users : IntIdTable() {
    val email = varchar("email", 255).uniqueIndex().nullable()
    val phoneNumber = varchar("phone_number", 20).uniqueIndex().nullable()
    val googleId = varchar("google_id", 255).uniqueIndex().nullable()
    val name = varchar("name", 255).nullable()
}

@Serializable
data class AuthRequest(val token: String? = null, val email: String? = null, val phone: String? = null, val otp: String? = null)

@Serializable
data class AuthResponse(val success: Boolean, val token: String? = null, val message: String? = null)

@Serializable
data class SimpleResponse(val success: Boolean, val message: String)

// --- Configuration ---
object AuthConfig {
    const val SECRET = "TATTLE_LOCAL_SECRET_KEY_2026"
    const val ISSUER = "com.example.tattle"
    const val AUDIENCE = "tattle-users"
    const val GOOGLE_WEB_CLIENT_ID = "610417006948-m97qce1p5ot524tr542m1ijf19n93uou.apps.googleusercontent.com"
}

val otpStore = ConcurrentHashMap<String, String>()

fun main() {
    // Initialize Local Database
    Database.connect("jdbc:sqlite:./tattle.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Users)
    }

    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Tattle Server"
            verifier(
                JWT.require(Algorithm.HMAC256(AuthConfig.SECRET))
                    .withAudience(AuthConfig.AUDIENCE)
                    .withIssuer(AuthConfig.ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Tattle Local Backend is running.")
        }

        // --- Google Sign In ---
        post("/api/auth/google") {
            val req = call.receive<AuthRequest>()
            val idTokenString = req.token ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing token")
            
            val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
                .setAudience(listOf(AuthConfig.GOOGLE_WEB_CLIENT_ID))
                .build()

            val idToken = try {
                verifier.verify(idTokenString)
            } catch (e: Exception) {
                null
            }

            if (idToken != null) {
                val payload = idToken.payload
                val userId = payload.subject
                val email = payload.email
                val name = payload.get("name") as String?

                val dbId = transaction {
                    val existing = Users.selectAll().where { Users.googleId eq userId }.singleOrNull()
                    if (existing != null) {
                        existing[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[googleId] = userId
                            it[Users.email] = email
                            it[Users.name] = name
                        }.value
                    }
                }
                
                val token = generateToken(dbId)
                call.respond(AuthResponse(true, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, message = "Invalid Google Token"))
            }
        }

        // --- Phone Auth (Simulation) ---
        post("/api/auth/otp/generate") {
            val req = call.receive<AuthRequest>()
            val phone = req.phone ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing phone")
            val otp = (100000..999999).random().toString()
            otpStore[phone] = otp
            
            println("------------------------------------")
            println("SIMULATED SMS to $phone: Your Tattle OTP is $otp")
            println("------------------------------------")
            
            call.respond(SimpleResponse(true, "OTP generated (Check server console)"))
        }

        post("/api/auth/otp/verify") {
            val req = call.receive<AuthRequest>()
            val phone = req.phone ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing phone")
            val otp = req.otp ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing otp")
            
            if (otpStore[phone] == otp) {
                otpStore.remove(phone)
                val dbId = transaction {
                    val existing = Users.selectAll().where { Users.phoneNumber eq phone }.singleOrNull()
                    if (existing != null) {
                        existing[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[phoneNumber] = phone
                        }.value
                    }
                }
                val token = generateToken(dbId)
                call.respond(AuthResponse(true, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, message = "Invalid OTP"))
            }
        }

        // --- Email Auth (Simulation) ---
        post("/api/auth/email/generate") {
            val req = call.receive<AuthRequest>()
            val email = req.email ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
            val otp = (100000..999999).random().toString()
            otpStore[email] = otp
            
            println("------------------------------------")
            println("SIMULATED EMAIL to $email: Your Tattle Login Code is $otp")
            println("------------------------------------")
            
            call.respond(SimpleResponse(true, "OTP generated (Check server console)"))
        }

        post("/api/auth/email/verify") {
            val req = call.receive<AuthRequest>()
            val email = req.email ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
            val otp = req.otp ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing otp")
            
            if (otpStore[email] == otp) {
                otpStore.remove(email)
                val dbId = transaction {
                    val existing = Users.selectAll().where { Users.email eq email }.singleOrNull()
                    if (existing != null) {
                        existing[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[Users.email] = email
                        }.value
                    }
                }
                val token = generateToken(dbId)
                call.respond(AuthResponse(true, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, message = "Invalid OTP"))
            }
        }

        authenticate("auth-jwt") {
            get("/api/user/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()
                
                val userProfile = transaction {
                    Users.selectAll().where { Users.id eq userId }.map {
                        mapOf(
                            "email" to it[Users.email],
                            "phone" to it[Users.phoneNumber],
                            "name" to it[Users.name]
                        )
                    }.singleOrNull()
                }
                
                if (userProfile != null) {
                    call.respond(userProfile)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

fun generateToken(userId: Int): String {
    return JWT.create()
        .withAudience(AuthConfig.AUDIENCE)
        .withIssuer(AuthConfig.ISSUER)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + 604_800_000)) // 7 days
        .sign(Algorithm.HMAC256(AuthConfig.SECRET))
}
