package com.agmstech.princessgame.core

import android.content.Context
import android.content.SharedPreferences

/** [KeyValueStore] backed by SharedPreferences. */
class PrefsStore(context: Context) : KeyValueStore {
    private val prefs: SharedPreferences = context.getSharedPreferences("princess_game", Context.MODE_PRIVATE)

    override fun get(key: String): String? = prefs.getString(key, null)

    override fun put(values: Map<String, String?>, sync: Boolean) {
        val e = prefs.edit()
        for ((k, v) in values) if (v == null) e.remove(k) else e.putString(k, v)
        if (sync) e.commit() else e.apply()
    }
}
