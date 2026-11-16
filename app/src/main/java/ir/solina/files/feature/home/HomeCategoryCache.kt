package ir.solina.files.feature.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.coroutines.coroutineContext

// Perf item 3: the Home screen used to walk the whole storage tree on every
// visit (the composition is disposed when leaving Home, so the scan re-ran
// from scratch each time). Totals are now cached here for the app session:
// the first visit scans once, later visits reuse the result, and only the
// explicit refresh button re-scans. The Home route's own ViewModelStore is
// cleared when the route leaves the stack, so a route-scoped ViewModel
// could not hold this cache.
object HomeCategoryCache {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _bytes = MutableStateFlow<Map<String, Long>>(emptyMap())
    val bytes: StateFlow<Map<String, Long>> = _bytes.asStateFlow()
    private var job: Job? = null
    private var scannedRoot: String? = null

    fun ensureScanned(root: String) {
        if (scannedRoot == root && (_bytes.value.isNotEmpty() || job != null)) return
        refresh(root)
    }

    fun refresh(root: String) {
        job?.cancel()
        scannedRoot = root
        job = scope.launch {
            _bytes.value = scanCategoryBytes(File(root))
        }
    }
}

private val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "tif", "tiff")
private val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v", "ts")
private val documentExtensions = setOf("pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv", "epub")
private val musicExtensions = setOf("mp3", "flac", "wav", "ogg", "m4a", "aac", "opus", "wma")
private val appExtensions = setOf("apk", "apks", "xapk")

private suspend fun scanCategoryBytes(root: File): Map<String, Long> {
    val totals = mutableMapOf("images" to 0L, "videos" to 0L, "documents" to 0L, "music" to 0L, "apps" to 0L)
    if (!root.exists() || !root.canRead()) return totals
    var seen = 0
    runCatching {
        root.walkTopDown().onEnter { dir ->
            val path = dir.absolutePath.replace('\\', '/')
            !path.contains("/Android/data") && !path.contains("/Android/obb") && !path.contains("/.thumbnails")
        }.forEach { file ->
            // Cooperative cancellation: leaving Home (or a refresh) stops an
            // in-flight walk promptly instead of letting it run to the end.
            if (++seen % 512 == 0) coroutineContext.ensureActive()
            if (!file.isFile) return@forEach
            val ext = file.extension.lowercase(Locale.ROOT)
            val key = when {
                ext in imageExtensions -> "images"
                ext in videoExtensions -> "videos"
                ext in documentExtensions -> "documents"
                ext in musicExtensions -> "music"
                ext in appExtensions -> "apps"
                else -> null
            }
            if (key != null) totals[key] = (totals[key] ?: 0L) + file.length().coerceAtLeast(0L)
        }
    }
    return totals
}
