package com.example.newattendancetracker.util

import android.content.Context
import android.os.Build
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceOptimizer @Inject constructor(
    private val context: Context
) {
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_SIZE = 100
    }
    
    data class CacheEntry(
        val data: Any,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS
    }
    
    /**
     * Cache data with automatic expiry
     */
    fun <T> cacheData(key: String, data: T) {
        if (cache.size >= MAX_CACHE_SIZE) {
            cleanExpiredCache()
        }
        cache[key] = CacheEntry(data as Any)
    }
    
    /**
     * Retrieve cached data if not expired
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedData(key: String): T? {
        val entry = cache[key]
        return if (entry != null && !entry.isExpired()) {
            entry.data as T
        } else {
            cache.remove(key)
            null
        }
    }
    
    /**
     * Clean expired cache entries
     */
    private fun cleanExpiredCache() {
        val expiredKeys = cache.entries
            .filter { it.value.isExpired() }
            .map { it.key }
        
        expiredKeys.forEach { cache.remove(it) }
    }
    
    /**
     * Clear all cache
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Debounce function calls to prevent excessive API calls
     */
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    fun <T> Flow<T>.debounceLatest(timeoutMillis: Long = 300L): Flow<T> {
        return this.debounce(timeoutMillis).distinctUntilChanged()
    }
    
    /**
     * Retry with exponential backoff
     */
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000L,
        maxDelayMs: Long = 10000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 2) throw e
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        return block() // Last attempt
    }
    
    /**
     * Execute task in background with timeout
     */
    suspend fun <T> executeWithTimeout(
        timeoutMs: Long = 30000L,
        block: suspend CoroutineScope.() -> T
    ): T {
        return withTimeout(timeoutMs) {
            withContext(Dispatchers.IO) {
                block()
            }
        }
    }
    
    /**
     * Batch operations to reduce database calls
     */
    suspend fun <T, R> batchProcess(
        items: List<T>,
        batchSize: Int = 50,
        processor: suspend (List<T>) -> R
    ): List<R> {
        return items.chunked(batchSize).map { batch ->
            processor(batch)
        }
    }
    
    /**
     * Memory optimization - check available memory
     */
    fun isLowMemory(): Boolean {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val availableMemory = maxMemory - usedMemory
        
        return availableMemory < (maxMemory * 0.1) // Less than 10% available
    }
    
    /**
     * Force garbage collection if low on memory
     */
    fun optimizeMemory() {
        if (isLowMemory()) {
            clearCache()
            System.gc()
        }
    }
    
    /**
     * Check if device has sufficient resources
     */
    fun hasGoodPerformance(): Boolean {
        val runtime = Runtime.getRuntime()
        val availableProcessors = runtime.availableProcessors()
        val maxMemory = runtime.maxMemory()
        
        return availableProcessors >= 4 && maxMemory >= 512 * 1024 * 1024 // 512MB
    }
    
    /**
     * Optimize WorkManager for background tasks
     */
    fun optimizeBackgroundTasks() {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel unnecessary work
        workManager.pruneWork()
        
        // Optimize based on device capabilities
        if (!hasGoodPerformance()) {
            // Reduce background task frequency on low-end devices
            workManager.cancelAllWorkByTag("non_critical")
        }
    }
    
    /**
     * Adaptive loading based on network and device performance
     */
    fun getOptimalBatchSize(): Int {
        return when {
            !hasGoodPerformance() -> 10
            isLowMemory() -> 25
            else -> 50
        }
    }
    
    /**
     * Preload critical data in background
     */
    fun preloadData(loader: suspend () -> Unit) {
        backgroundScope.launch {
            try {
                loader()
            } catch (e: Exception) {
                // Log error but don't crash
                ErrorHandler().logError("PerformanceOptimizer", "Preload failed", e)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        backgroundScope.cancel()
        clearCache()
    }
}