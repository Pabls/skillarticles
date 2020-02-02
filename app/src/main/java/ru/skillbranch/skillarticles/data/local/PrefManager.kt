package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

class PrefManager(context: Context) {
    internal val preferences: SharedPreferences by lazy { PreferenceManager(context).sharedPreferences }

    fun clearAll() {
        preferences.edit().clear().apply()
    }
}
