package com.knoxprobe.util

import android.content.Context
import java.util.UUID

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("knox_probe", Context.MODE_PRIVATE)

    val sessionId: String by lazy {
        prefs.getString(KEY_SESSION_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_SESSION_ID, it).apply()
        }
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"
    }
}
