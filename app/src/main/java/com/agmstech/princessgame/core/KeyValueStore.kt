package com.agmstech.princessgame.core

/** Minimal persistent string storage, so game logic can be unit-tested without Android. */
interface KeyValueStore {
    fun get(key: String): String?

    /** Writes all [values] (a null value removes the key). [sync] waits until it is on disk. */
    fun put(values: Map<String, String?>, sync: Boolean = false)

    fun put(key: String, value: String?, sync: Boolean = false) = put(mapOf(key to value), sync)
}

class MemoryStore : KeyValueStore {
    val data = HashMap<String, String>()

    override fun get(key: String): String? = data[key]

    override fun put(values: Map<String, String?>, sync: Boolean) {
        for ((k, v) in values) if (v == null) data.remove(k) else data[k] = v
    }
}
