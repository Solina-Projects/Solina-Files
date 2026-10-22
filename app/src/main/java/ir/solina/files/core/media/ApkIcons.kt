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

data class ApkIcon(val path: String, val modified: Long)

object ApkIcons {

    const val SIZE = 192

    fun load(context: Context, path: String, size: Int = SIZE): Bitmap? {
        val pm = context.packageManager
        val info = runCatching { pm.getPackageArchiveInfo(path, 0) }.getOrNull() ?: return null
        val app = info.applicationInfo ?: return null
        app.sourceDir = path
        app.publicSourceDir = path
        val icon = runCatching { app.loadIcon(pm) }.getOrNull() ?: return null
        return runCatching { icon.toBitmap(size, size, Bitmap.Config.ARGB_8888) }.getOrNull()
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
