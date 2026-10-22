package ir.solina.files

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import coil3.video.VideoFrameDecoder
import ir.solina.files.core.media.ApkIconFetcher
import ir.solina.files.core.media.ApkIconKeyer
import ir.solina.files.core.remote.SafFileSystem
import ir.solina.files.core.root.RootManager
import ir.solina.files.core.shizuku.ShizukuManager
import ir.solina.files.core.sound.SoundPoolDevice
import ir.solina.files.core.sound.UiSounds
import ir.solina.files.core.util.Formatters
import ir.solina.files.core.vault.Vault
import ir.solina.files.ui.i18n.AppLocales
import ir.solina.files.ui.security.AppLock

class SolinaFilesApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        AppLocales.install(this)
        Formatters.locale = resources.configuration.locales[0]
        Vault.init(filesDir, cacheDir)
        AppLock.track(this)
        RootManager.init(this)
        SafFileSystem.appContext = this
        runCatching { ShizukuManager.init(this) }
        runCatching { UiSounds.device = SoundPoolDevice(this).also { it.trackActivities(this) } }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
                if (Build.VERSION.SDK_INT >= 28) add(AnimatedImageDecoder.Factory()) else add(GifDecoder.Factory())
                add(SvgDecoder.Factory())
                add(ApkIconFetcher.Factory())
                add(ApkIconKeyer())
            }
            .crossfade(true)
            .build()
}
