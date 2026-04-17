package com.yourapp.spendwise.data

import java.util.concurrent.ConcurrentHashMap

/**
 * In-process cache of GPS coordinates snapshotted the moment a transaction
 * signal is detected (SMS received, email parsed, etc.).
 *
 * Key: a deterministic string derived from the raw SMS body + timestamp so the
 *      same pending item can be looked up later when the transaction is confirmed.
 *
 * Thread-safe via ConcurrentHashMap — safe to write from background receivers
 * and read from processing coroutines simultaneously.
 *
 * Entries are small (two Doubles) and are always evicted as soon as the
 * transaction is either confirmed (coords attached) or discarded — so the map
 * never grows large.
 */
object LocationCache {

    private val cache = ConcurrentHashMap<String, Pair<Double, Double>>()

    /**
     * Build a stable key from the raw SMS body and the receive timestamp.
     * Both values come from the same SMS message object so they uniquely
     * identify a pending entry without any database lookup.
     */
    fun key(body: String, timestamp: Long): String =
        "${timestamp}_${body.take(80).hashCode()}"

    /** Store the location for this (body, timestamp) pair. */
    fun put(body: String, timestamp: Long, location: Pair<Double, Double>) {
        cache[key(body, timestamp)] = location
    }

    /**
     * Retrieve and remove the cached location (one-time claim).
     * Returns null if location was never captured (permission denied, etc.).
     */
    fun pop(body: String, timestamp: Long): Pair<Double, Double>? =
        cache.remove(key(body, timestamp))

    /** Evict without retrieving — call when a transaction is discarded. */
    fun evict(body: String, timestamp: Long) {
        cache.remove(key(body, timestamp))
    }
}
