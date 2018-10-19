package com.mobile.featured

import com.facebook.FacebookSdk.getCacheDir
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import java.io.File


object MPSimpleCache {
    private var sDownloadCache: SimpleCache? = null

    val instance: SimpleCache
        get() {
            if (sDownloadCache == null) {
                val evict = LeastRecentlyUsedCacheEvictor((100 * 1024 * 1024).toLong())
                sDownloadCache = SimpleCache(File(getCacheDir(), "media"), evict)}
            return sDownloadCache as SimpleCache
        }
}