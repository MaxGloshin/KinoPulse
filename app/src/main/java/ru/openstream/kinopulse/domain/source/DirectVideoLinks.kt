package ru.openstream.kinopulse.domain.source

import java.net.URI
import java.security.MessageDigest

/**
 * Only explicitly supplied HTTPS video URLs. A webpage link, DRM URL or a stream
 * requiring custom authorization headers is not automatically supported.
 */
object DirectVideoLinks {
    fun normalize(url: String): String? {
        val value = url.trim()
        if (value.length !in 1..4096) return null
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.host.isNullOrBlank() || uri.userInfo != null || uri.fragment != null) return null
        return value
    }

    fun stableId(url: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(url.toByteArray(Charsets.UTF_8))
        return "user_link_" + hash.take(12).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }
}
