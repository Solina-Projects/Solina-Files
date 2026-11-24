package ir.solina.files.feature.explorer.components

import ir.solina.files.ui.isolated
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.decode.BitmapFactoryDecoder
import coil3.request.ImageRequest
import ir.solina.files.R
import ir.solina.files.core.media.ApkIcon
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.model.UiDensity
import ir.solina.files.core.model.ListPrefs
import ir.solina.files.core.util.FileKind
import ir.solina.files.core.util.FileKinds
import ir.solina.files.core.util.Formatters
import ir.solina.files.feature.explorer.dnd.DropAnimation
import ir.solina.files.feature.explorer.dnd.DropHalo
import ir.solina.files.feature.explorer.dnd.DropLook
import ir.solina.files.feature.explorer.dnd.dropHaloBehind
import ir.solina.files.feature.explorer.dnd.fileGestures
import ir.solina.files.feature.explorer.dnd.rememberDropAnimation
import ir.solina.files.ui.LocalAppearancePrefs
import ir.solina.files.ui.LocalBehaviorPrefs
import ir.solina.files.ui.LocalListPrefs
import ir.solina.files.ui.LocalUiDensity
import ir.solina.files.ui.expressive.rememberCheckPopEnter
import ir.solina.files.ui.expressive.rememberCheckPopExit
import ir.solina.files.ui.theme.KindPalette
import ir.solina.files.ui.theme.toShape
import okio.Path.Companion.toPath

/**
 * Shared resting [DropAnimation] for rows with no drag active (PERF_AUDIT_2
 * item 3). The values are the rest values [rememberDropAnimation] produces
 * for [DropLook.NONE] (hover 0, ring 0, gulp 1, spin 0). The states are
 * never mutated, so sharing one instance across all rows is safe — and it
 * lets every idle row skip its own animateFloatAsState, two Animatables
 * and two LaunchedEffects entirely.
 */
private val IdleDropAnimation = DropAnimation(
    hover = mutableFloatStateOf(0f),
    ring = mutableFloatStateOf(0f),
    gulp = mutableFloatStateOf(1f),
    spin = mutableFloatStateOf(0f),
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileRow(
    item: FileItem,
    selected: Boolean,
    flashKey: Int,
    showThumbnail: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    flashPulses: Int = FLASH_PULSES,
    drop: DropLook = DropLook.NONE,
    onDragStart: ((Offset, PointerId) -> Boolean)? = null,
    singlePane: Boolean = false,
    showSelectionCheckbox: Boolean = false,
    folderStats: Pair<Long, Int>? = null,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val density = LocalUiDensity.current
    // PERF_AUDIT_2 item 15: read only the narrow list slice, not the whole
    // SolinaSettings — an appearance/behavior change no longer recomposes
    // every visible row.
    val list = LocalListPrefs.current
    val iconTapSelects = LocalBehaviorPrefs.current.iconTapSelects
    // Scroll-lag fix: folder size/count is NOT computed here anymore.
    // A recursive walk per visible row, re-run every time the row scrolled
    // back into view, is what made single-pane scrolling stutter. The
    // ViewModel computes it once per directory visit and passes it in.
    val layoutScale = if (!singlePane) 1f else when (density) { UiDensity.COMPACT -> 1.5f; UiDensity.COMFORTABLE -> 2.5f; UiDensity.LARGE -> 3.5f }
    val listIconScale = if (!singlePane) 1f else when (density) { UiDensity.COMPACT -> 1.1f; UiDensity.COMFORTABLE, UiDensity.LARGE -> 1f }
    val listSpacingScale = if (!singlePane) 1f else when (density) { UiDensity.COMPACT -> 1f; UiDensity.COMFORTABLE, UiDensity.LARGE -> 0.5f }
    val fontScale = if (singlePane) when (density) { UiDensity.COMPACT -> 1f; UiDensity.COMFORTABLE -> 1.05f; UiDensity.LARGE -> 1.1f } else 1f
    // Redesign v2: in dual-pane the font is fixed at the normal size too
    // (COMFORTABLE 14/11), independent of the single-pane density setting.
    val nameSp = if (!singlePane) 14 else density.nameSp
    val metaSp = if (!singlePane) 11 else density.metaSp
    // Redesign v2: in dual-pane the icons are a fixed 1.5x the normal size
    // (COMFORTABLE 36dp -> 54dp), independent of density and of single-pane
    // settings.
    val iconSize = if (!singlePane) 54.dp else density.iconDp.dp * layoutScale * listIconScale
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, motion.fastSpatialSpec(), label = "press")
    // PERF_AUDIT_2 item 3: the drag state already lives at list level
    // (DragLinks returns the DropLook.NONE singleton when no drag is
    // active), so only a row with a live DropLook pays for drop-animation
    // state. Idle rows share IdleDropAnimation instead.
    val dragActive = drop !== DropLook.NONE
    val look = if (dragActive) rememberDropAnimation(drop) else IdleDropAnimation
    // PERF_AUDIT_2 item 7: the check pop transitions are built once per
    // motion scheme, not fresh on every composition.
    val checkPopEnter = rememberCheckPopEnter(motion)
    val checkPopExit = rememberCheckPopExit(motion)
    val background by animateColorAsState(
        when {
            drop.hovered -> colors.primaryContainer.copy(alpha = 0.6f)
            selected -> colors.secondaryContainer
            else -> Color.Transparent
        },
        motion.defaultEffectsSpec(),
        label = "rowBg",
    )
    val ghostAlpha by animateFloatAsState(if (drop.ghosted) 0.36f else 1f, motion.defaultEffectsSpec(), label = "rowGhost")
    val flash = rememberFlash(flashKey, flashPulses)
    val flashColor = colors.primary.copy(alpha = 0.32f)
    // Resolve the extension-based kind once per file identity. Leading and
    // its nested icon/thumbnail branches reuse this value rather than each
    // lowercasing the extension and walking the kind classifier again.
    val kind = remember(item.path, item.name, item.isDirectory) { FileKinds.of(item) }

    Box(
        modifier
            .then(drop.spot)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = ghostAlpha
            }
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .drawBehind {
                val a = flash.value
                if (a > 0f) drawRect(flashColor, alpha = a)
            }
            .fileGestures(interaction, onClick, onLongClick, onDragStart)
            .padding(horizontal = 6.dp, vertical = density.rowPadDp.dp * layoutScale),
    ) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // No drag active: the halo draws nothing (hover == 0), so skip the
        // MorphPainter / polygon-morph setup entirely. drop.anchor is kept
        // so a later drag still finds this row as a target.
        if (dragActive) {
            DropHalo(look, drop.anchor) {
                Leading(item, kind, selected, showThumbnail, iconSize, if (iconTapSelects) onIconClick else null)
            }
        } else {
            Box(drop.anchor, contentAlignment = Alignment.Center) {
                Leading(item, selected, showThumbnail, iconSize, if (iconTapSelects) onIconClick else null)
            }
        }
        Spacer(Modifier.width((if (density.iconDp <= 30) 8.dp else 10.dp) * listSpacingScale))
        Column(Modifier.weight(1f)) {
            Text(
                isolated(Formatters.displayName(item.name, item.isDirectory, list.showExtensions)),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = (nameSp * fontScale).sp,
                maxLines = if (list.wrapNames) 3 else density.nameMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            val date = if (list.showDate) Formatters.listDate(item.lastModified, list.dateStyle, list.use24Hour) else null
            val info = trailingInfo(item, list, singlePane, folderStats)
            if (date != null || info != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        date.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = (metaSp * fontScale).sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (info != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp)) {
                            Icon(Icons.Rounded.DataUsage, contentDescription = "حجم", modifier = Modifier.size(12.dp), tint = colors.onSurfaceVariant)
                            Text(info.first, style = MaterialTheme.typography.labelSmall, fontSize = (metaSp * fontScale).sp, color = colors.onSurfaceVariant, maxLines = 1)
                            info.second?.let { count ->
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Rounded.FolderOpen, contentDescription = "تعداد فایل‌های پوشه", modifier = Modifier.size(12.dp), tint = colors.onSurfaceVariant)
                                Text(count, style = MaterialTheme.typography.labelSmall, fontSize = (metaSp * fontScale).sp, color = colors.onSurfaceVariant, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showSelectionCheckbox) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp, bottom = 28.dp)
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (selected) colors.primary else colors.surface.copy(alpha = 0.92f))
                .border(1.5.dp, if (selected) colors.primary else colors.outline, RoundedCornerShape(4.dp))
                .clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() },
            contentAlignment = Alignment.Center,
        ) {
            // Boolean flip only: the check pops in/out on the shared
            // motion-scheme spec, so REDUCED motion snaps it.
            AnimatedVisibility(
                visible = selected,
                enter = checkPopEnter,
                exit = checkPopExit,
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.explorer_selected),
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileTile(
    item: FileItem,
    selected: Boolean,
    selecting: Boolean,
    flashKey: Int,
    showThumbnail: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    flashPulses: Int = FLASH_PULSES,
    drop: DropLook = DropLook.NONE,
    onDragStart: ((Offset, PointerId) -> Boolean)? = null,
    singlePane: Boolean = false,
    horizontalPane: Boolean = false,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    // PERF_AUDIT_2 item 15: narrow list slice only (see FileRow).
    val list = LocalListPrefs.current
    val density = LocalUiDensity.current
    val fontScale = if (singlePane) when (density) { UiDensity.COMPACT -> 1f; UiDensity.COMFORTABLE -> 1.05f; UiDensity.LARGE -> 1.1f } else 1f
    // Redesign v2: fixed normal font size in dual-pane, like the list rows.
    val nameSp = if (!singlePane) 14 else density.nameSp
    val tileDensityScale = if (!singlePane) 1f else when (density) { UiDensity.COMPACT -> 1f; UiDensity.COMFORTABLE -> 1.2f; UiDensity.LARGE -> 1.4f }
    val tileIconSize = density.iconDp.dp * (if (horizontalPane) 1.125f else 1.75f) * tileDensityScale
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, motion.fastSpatialSpec(), label = "press")
    // PERF_AUDIT_2 item 3: same gating as FileRow — no drag, no
    // per-tile drop-animation state (see IdleDropAnimation).
    val dragActive = drop !== DropLook.NONE
    val look = if (dragActive) rememberDropAnimation(drop) else IdleDropAnimation
    val background by animateColorAsState(
        when {
            drop.hovered -> colors.primaryContainer
            selected -> colors.secondaryContainer
            else -> colors.surfaceContainerLow
        },
        motion.defaultEffectsSpec(),
        label = "tileBg",
    )
    val ghostAlpha by animateFloatAsState(if (drop.ghosted) 0.36f else 1f, motion.defaultEffectsSpec(), label = "tileGhost")
    val flash = rememberFlash(flashKey, flashPulses)
    val flashColor = colors.primary.copy(alpha = 0.32f)
    val shape = LocalAppearancePrefs.current.iconShape.toShape()
    val kind = remember(item.path, item.name, item.isDirectory) { FileKinds.of(item) }
    Column(
        modifier
            .then(drop.spot)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = ghostAlpha
            }
            .clip(MaterialTheme.shapes.large)
            .background(background)
            .drawBehind {
                val a = flash.value
                if (a > 0f) drawRect(flashColor, alpha = a)
            }
            .fileGestures(interaction, onClick, onLongClick, onDragStart)
            .padding(6.dp)
            .testTag("file-tile"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // PERF_AUDIT_2 item 3: skip dropHaloBehind entirely when no drag
        // is active — the halo draws nothing at hover 0.
        Box(Modifier.fillMaxWidth().aspectRatio(1f).then(drop.anchor).then(if (dragActive) Modifier.dropHaloBehind(look, reach = 0.72f) else Modifier), contentAlignment = Alignment.Center) {
            val picture = showThumbnail && list.thumbnails && (kind == FileKind.IMAGE || kind == FileKind.VIDEO)
            val apk = showThumbnail && list.apkIcons && kind == FileKind.APK
            when {
                picture -> {
                    KindIcon(item, kind, 48.dp, shape)
                    AsyncImage(
                        model = thumbnailModel(item),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                    )
                }
                apk -> ApkIconOrKind(item, kind, tileIconSize, shape)
                else -> KindIcon(item, kind, tileIconSize, shape, contentPaddingFraction = if (!singlePane) 0.22f else when (density) { UiDensity.COMPACT -> 0.07f; UiDensity.COMFORTABLE -> 0.07f; UiDensity.LARGE -> 0.02f })
            }
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                TileCheckOverlay(selected = selected, selecting = selecting)
            }
        }
        Text(
            isolated(Formatters.displayName(item.name, item.isDirectory, list.showExtensions)),
            style = MaterialTheme.typography.labelMedium,
            fontSize = ((if (horizontalPane) nameSp * 0.85f else nameSp * fontScale)).sp,
            maxLines = if (list.wrapNames) 3 else 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (singlePane) 3.dp else 6.dp),
        )
    }
}

/**
 * Tile check overlay, extracted from FileTile so AnimatedVisibility resolves
 * to the top-level function: inside FileTile's Column the ColumnScope member
 * overload is picked instead, and it cannot be called from the nested Box.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TileCheckOverlay(selected: Boolean, selecting: Boolean) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    // Boolean flip only, same shared pop as the row checkbox.
    // PERF_AUDIT_2 item 7: transitions cached per motion scheme.
    val checkPopEnter = rememberCheckPopEnter(motion)
    val checkPopExit = rememberCheckPopExit(motion)
    AnimatedVisibility(
        visible = selected,
        enter = checkPopEnter,
        exit = checkPopExit,
    ) {
        Box(Modifier.fillMaxSize().clip(CircleShape).background(colors.primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.explorer_selected), tint = colors.onPrimary, modifier = Modifier.size(14.dp))
        }
    }
    if (!selected && selecting) {
        Box(Modifier.fillMaxSize().clip(CircleShape).border(2.dp, colors.outline, CircleShape).background(colors.surface.copy(alpha = 0.6f)))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun rememberFlash(flashKey: Int, pulses: Int): State<Float> {
    // PERF_AUDIT_2 item 3: a flash is requested for at most one row at a
    // time, and only briefly — every other row used to pay for an
    // Animatable plus a LaunchedEffect that did nothing but snap to 0.
    // With flashKey == 0 there is no animation state at all; when a flash
    // is requested the state is created and the effect runs as before.
    if (flashKey == 0) return remember { mutableFloatStateOf(0f) }
    // The flash now rides the motion scheme's effects spec instead of
    // hardcoded tweens, so REDUCED motion snaps it; the FLASH_*_MS
    // constants stay because flashDurationMs() still times the reveal
    // delay in PaneColumn.
    val motion = MaterialTheme.motionScheme
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashKey) {
        repeat(pulses) {
            flash.animateTo(1f, motion.defaultEffectsSpec())
            flash.animateTo(0.25f, motion.defaultEffectsSpec())
        }
        flash.animateTo(0f, motion.defaultEffectsSpec())
    }
    return flash.asState()
}

const val FLASH_PULSES = 2
private const val FLASH_IN_MS = 200
private const val FLASH_OUT_MS = 280
private const val FLASH_FADE_MS = 520

fun flashDurationMs(pulses: Int): Long = (pulses * (FLASH_IN_MS + FLASH_OUT_MS) + FLASH_FADE_MS).toLong()

@Composable
private fun trailingInfo(item: FileItem, list: ListPrefs, singlePane: Boolean, folderStats: Pair<Long, Int>?): Pair<String, String?>? = when {
    item.isSymlink -> stringResource(R.string.explorer_link) to null
    singlePane && item.isDirectory -> folderStats?.let { Formatters.size(it.first, list.sizeUnits) to Formatters.number(it.second.toLong()) }
    singlePane -> Formatters.size(item.size, list.sizeUnits) to null
    item.isDirectory -> null
    !list.showSize -> null
    else -> Formatters.size(item.size, list.sizeUnits) to null
}

@Composable
private fun thumbnailModel(item: FileItem): Any {
    val context = LocalContext.current
    return remember(item.path) {
        if (item.extension == "gif" || item.extension == "webp") {
            ImageRequest.Builder(context).data(item.path.toPath()).decoderFactory(BitmapFactoryDecoder.Factory()).build()
        } else {
            item.path.toPath()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Leading(item: FileItem, kind: FileKind, selected: Boolean, showThumbnail: Boolean, iconSize: Dp, onIconClick: (() -> Unit)?) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    // PERF_AUDIT_2 item 15: narrow slices only (see FileRow).
    val shape = LocalAppearancePrefs.current.iconShape.toShape()
    val list = LocalListPrefs.current
    Box(
        Modifier
            .size(iconSize)
            .clip(shape)
            .then(if (onIconClick != null) Modifier.clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                (scaleIn(motion.fastSpatialSpec(), initialScale = 0.6f) + fadeIn(motion.fastEffectsSpec()))
                    .togetherWith(scaleOut(motion.fastSpatialSpec(), targetScale = 0.6f) + fadeOut(motion.fastEffectsSpec()))
            },
            label = "selectCheck",
        ) { isSelected ->
            when {
                isSelected -> Box(
                    Modifier.size(iconSize).clip(shape).background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.explorer_selected), tint = colors.onPrimary)
                }
                showThumbnail && list.thumbnails && (kind == FileKind.IMAGE || kind == FileKind.VIDEO) -> Box(
                    Modifier.size(iconSize),
                    contentAlignment = Alignment.Center,
                ) {
                    KindIcon(item, kind, iconSize, shape)
                    AsyncImage(
                        model = thumbnailModel(item),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(iconSize).clip(shape),
                    )
                }
                showThumbnail && list.apkIcons && kind == FileKind.APK -> ApkIconOrKind(item, kind, iconSize, shape)
                else -> KindIcon(item, iconSize, shape)
            }
        }
    }
}

@Composable
private fun ApkIconOrKind(item: FileItem, kind: FileKind, size: Dp, shape: Shape) {
    var loaded by remember(item.path) { mutableStateOf(false) }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (!loaded) KindIcon(item, kind, size, shape)
        AsyncImage(
            model = remember(item.path, item.lastModified) { ApkIcon(item.path, item.lastModified) },
            contentDescription = stringResource(R.string.explorer_app_icon),
            onSuccess = { loaded = true },
            modifier = Modifier.size(size).testTag("apk-icon"),
        )
    }
}

@Composable
private fun KindIcon(item: FileItem, kind: FileKind, iconSize: Dp, shape: Shape, contentPaddingFraction: Float = 0.22f) {
    val colors = MaterialTheme.colorScheme
    // PERF_AUDIT_2 item 15: narrow appearance slice only (see FileRow).
    val tile = KindPalette.colors(kind, LocalAppearancePrefs.current.colorfulIcons, colors, colors.background.luminance() < 0.5f)
    // Show the file-type symbol by itself: no colored tile/background or inset.
    Box(
        Modifier.size(iconSize),
        contentAlignment = Alignment.Center,
    ) {
        if (item.isDirectory) {
            // Redesign v2: folders take their own KindPalette colour (purple
            // hue 265), like every other file kind — tinting with the theme
            // primary turned them all the same washed-out brown.
            Icon(
                Icons.Rounded.Folder,
                contentDescription = stringResource(kind.label),
                tint = tile.content,
                modifier = Modifier.fillMaxSize().padding(iconSize * 0.08f),
            )
        } else {
            Icon(
                kind.icon,
                contentDescription = stringResource(kind.label),
                tint = tile.content,
                modifier = Modifier.padding(iconSize * contentPaddingFraction),
            )
        }
    }
}
