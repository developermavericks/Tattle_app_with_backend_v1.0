package com.example.tattle.ui.theme

object LocalStrings {
    private val translations = mapOf(
        "English" to mapOf(
            "for_you" to "For You",
            "explore" to "Explore",
            "trending" to "Trending",
            "saved" to "Saved",
            "settings" to "Settings",
            "language" to "Language",
            "clear_history" to "Clear History",
            "next" to "Next",
            "finish" to "Finish",
            "back" to "Back"
        ),
        "Spanish" to mapOf(
            "for_you" to "Para ti",
            "explore" to "Explorar",
            "trending" to "Tendencias",
            "saved" to "Guardado",
            "settings" to "Ajustes",
            "language" to "Idioma",
            "clear_history" to "Borrar historial",
            "next" to "Siguiente",
            "finish" to "Finalizar",
            "back" to "Atrás"
        ),
        "French" to mapOf(
            "for_you" to "Pour vous",
            "explore" to "Explorer",
            "trending" to "Tendances",
            "saved" to "Enregistré",
            "settings" to "Paramètres",
            "language" to "Langue",
            "clear_history" to "Effacer l'historique",
            "next" to "Suivant",
            "finish" to "Terminer",
            "back" to "Retour"
        ),
        "German" to mapOf(
            "for_you" to "Für dich",
            "explore" to "Entdecken",
            "trending" to "Trends",
            "saved" to "Gespeichert",
            "settings" to "Einstellungen",
            "language" to "Sprache",
            "clear_history" to "Verlauf löschen",
            "next" to "Weiter",
            "finish" to "Fertig",
            "back" to "Zurück"
        )
    )

    fun get(key: String, language: String): String {
        return translations[language]?.get(key) ?: translations["English"]?.get(key) ?: key
    }
}
