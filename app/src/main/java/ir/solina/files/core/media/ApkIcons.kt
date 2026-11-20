package ir.solina.files.core.media

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import kotlin.math.roundToInt

data class ApkIcon(val path: String, val modified: Long)

object ApkIcons {

    const val SIZE = 192

    // Audit item 21: an APK icon used to mean a full APK parse plus a
    // 192px bitmap, while the icon is displayed at ~54dp. Decode at the
    // display size instead (~54dp in pixels on this device).
    fun displaySizePx(context: Context): Int =
        (54f * context.resources.displayMetrics.density).roundToInt().coerceAtLeast(1)

    fun load(context: Context, path: String, size: Int = -1): Bitmap? {
        val pm = context.packageManager
        val info = runCatching { pm.getPackageArchiveInfo(path, 0) }.getOrNull() ?: return null
        val app = info.applicationInfo ?: return null
        app.sourceDir = path
        app.publicSourceDir = path
        val icon = runCatching { app.loadIcon(pm) }.getOrNull() ?: return null
        // size <= 0 (the default) means "display size"; an explicit size
        // still wins for callers that need a larger icon.
        val targetSize = if (size > 0) size else displaySizePx(context)
        return runCatching { icon.toBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888) }.getOrNull()
    }
}

class ApkIconFetcher(private val data: ApkIcon, private val options: Options) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val bitmap = ApkIcons.load(options.context, data.path) ?: return null
        return ImageFetchResult(image = bitmap.asImage(), isSampled = false, dataSource = DataSource.DISK)
    }

    class Factory : Fetcher.Factory<ApkIcon> {
        override fun create(data: ApkIcon, options: Options, imageLoader: ImageLoader): Fetcher = ApkIconFetcher(data, options)
    }
}

class ApkIconKeyer : Keyer<ApkIcon> {
    override fun key(data: ApkIcon, options: Options): String = "apk-icon:${data.path}:${data.modified}"
}
