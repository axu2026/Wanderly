package com.example.wanderly.auth

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var currentUserId: Long?
        get() = prefs.getLong(KEY_USER_ID, NO_USER).takeIf { it != NO_USER }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_USER_ID) else putLong(KEY_USER_ID, value)
            }.apply()
        }

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
        set(value) { prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, value).apply() }

    private companion object {
        const val PREFS_NAME = "wanderly_session"
        const val KEY_USER_ID = "current_user_id"
        const val KEY_ONBOARDING_SEEN = "has_seen_onboarding"
        const val NO_USER = -1L
    }
}
