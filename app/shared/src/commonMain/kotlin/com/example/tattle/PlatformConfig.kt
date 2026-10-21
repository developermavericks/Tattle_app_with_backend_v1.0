package com.example.tattle

object PlatformConfig {
    val BASE_URL: String = BuildKonfig.BASE_URL
    val CURATED_API_URL: String = BASE_URL
    /** Shared key the backend checks (X-App-Key) when MOBILE_APP_KEY is set on the server. */
    val APP_KEY: String = BuildKonfig.APP_KEY
    /** Skip the OTP/Google login (it needs the Ktor gateway): go straight to onboarding. For company pilots. */
    val GUEST_MODE: Boolean = BuildKonfig.GUEST_MODE == "true"
    /** Sample stories are for demos only: a real build must never show them as if they were news. */
    const val USE_MOCK_FALLBACK: Boolean = false
    val SUPABASE_URL: String = BuildKonfig.SUPABASE_URL
    val SUPABASE_KEY: String = BuildKonfig.SUPABASE_KEY
    const val GOOGLE_WEB_CLIENT_ID = "867080275588-1r7t5qbghdld0v4t4q1jhkhf79klau9h.apps.googleusercontent.com"
}
