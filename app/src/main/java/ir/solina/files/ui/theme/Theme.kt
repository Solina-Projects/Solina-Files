package ir.solina.files.ui.theme

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ir.solina.files.core.model.AppearancePrefs
import ir.solina.files.core.model.ContrastLevel
import ir.solina.files.core.model.MotionStyle
import ir.solina.files.core.model.ThemeMode
import ir.solina.files.core.model.ThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalAppearance = staticCompositionLocalOf { AppearancePrefs() }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SolinaTheme(
    appearance: AppearancePrefs = AppearancePrefs(),
    content: @Composable () -> Unit,
) {
    val dark = when (appearance.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    // The scheme (materialkolor HCT generation + a WallpaperManager IPC) is
    // built off the main thread. Until the first build finishes, the seed
    // scheme renders the first frame, so there is no blank/flashing screen;
    // later appearance changes keep showing the last scheme until the new one
    // is ready.
    val appContext = remember(context) { context.applicationContext }
    val fallbackScheme = remember {
        SolinaColors.generate(appearance.seed, appearance.style, appearance.contrast, dark)
            .let { if (dark && appearance.pureBlack) it.pureBlack() else it }
    }
    var colorScheme by remember { mutableStateOf<ColorScheme?>(null) }
    LaunchedEffect(appearance.wallpaperColors, appearance.seed, appearance.style, appearance.contrast, appearance.pureBlack, dark) {
        colorScheme = withContext(Dispatchers.Default) {
            SolinaColors.scheme(appContext, appearance, dark)
        }
    }
    val scheme = colorScheme ?: fallbackScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    val density = LocalDensity.current
    val scaled = remember(density.density, density.fontScale, appearance.textScale) {
        Density(density.density, density.fontScale * appearance.textScale)
    }
    val haptics = LocalHapticFeedback.current
    val shapes = remember(appearance.corners) { solinaShapes(appearance.corners.scale) }

    MaterialExpressiveTheme(
        colorScheme = scheme,
        motionScheme = motionScheme(appearance.motion),
        shapes = shapes,
        typography = SolinaTypography,
    ) {
        CompositionLocalProvider(
            LocalDensity provides scaled,
            LocalHapticFeedback provides if (appearance.haptics) haptics else NoHaptics,
            LocalAppearance provides appearance,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun motionScheme(style: MotionStyle): MotionScheme = when (style) {
    MotionStyle.EXPRESSIVE -> MotionScheme.expressive()
    MotionStyle.STANDARD -> MotionScheme.standard()
    MotionStyle.REDUCED -> ReducedMotion
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private object ReducedMotion : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = snap()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = snap()
}

private object NoHaptics : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}

object SolinaColors {

    val wallpaperAvailable: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun scheme(context: Context, a: AppearancePrefs, dark: Boolean): ColorScheme {
        val wallpaper = if (a.wallpaperColors && wallpaperAvailable) {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            null
        }
        // The wallpaper palette is fetched at most once per scheme build and
        // its primary colour is reused as the seed below. dynamic*ColorScheme()
        // reads the system tonal palette from resources, so this is the only
        // WallpaperManager call on this path. It is only fetched when a style
        // other than TONAL_SPOT/STANDARD actually needs the seed.
        val wallpaperPrimary: Int? = if (wallpaper != null &&
            !(a.style == ThemeStyle.TONAL_SPOT && a.contrast == ContrastLevel.STANDARD)
        ) {
            runCatching {
                WallpaperManager.getInstance(context)
                    .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb()
            }.getOrNull()
        } else {
            null
        }
        val base = when {
            wallpaper != null && a.style == ThemeStyle.TONAL_SPOT && a.contrast == ContrastLevel.STANDARD -> wallpaper
            wallpaper != null -> generate(wallpaperPrimary ?: wallpaper.primary.toArgb(), a.style, a.contrast, dark)
            else -> generate(a.seed, a.style, a.contrast, dark)
        }
        return if (dark && a.pureBlack) base.pureBlack() else base
    }

    fun generate(seed: Int, style: ThemeStyle, contrast: ContrastLevel, dark: Boolean): ColorScheme =
        dynamicColorScheme(
            seedColor = Color(seed),
            isDark = dark,
            isAmoled = false,
            style = style.palette,
            contrastLevel = contrast.level,
        )

    private val ThemeStyle.palette: PaletteStyle
        get() = when (this) {
            ThemeStyle.TONAL_SPOT -> PaletteStyle.TonalSpot
            ThemeStyle.VIBRANT -> PaletteStyle.Vibrant
            ThemeStyle.EXPRESSIVE -> PaletteStyle.Expressive
            ThemeStyle.RAINBOW -> PaletteStyle.Rainbow
            ThemeStyle.FRUIT_SALAD -> PaletteStyle.FruitSalad
            ThemeStyle.CONTENT -> PaletteStyle.Content
            ThemeStyle.FIDELITY -> PaletteStyle.Fidelity
            ThemeStyle.NEUTRAL -> PaletteStyle.Neutral
            ThemeStyle.MONOCHROME -> PaletteStyle.Monochrome
        }
}

fun ColorScheme.pureBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = lerp(surfaceContainerLow, Color.Black, 0.6f),
    surfaceContainer = lerp(surfaceContainer, Color.Black, 0.5f),
    surfaceContainerHigh = lerp(surfaceContainerHigh, Color.Black, 0.4f),
    surfaceContainerHighest = lerp(surfaceContainerHighest, Color.Black, 0.3f),
    surfaceBright = lerp(surfaceBright, Color.Black, 0.3f),
    surfaceVariant = lerp(surfaceVariant, Color.Black, 0.3f),
)
