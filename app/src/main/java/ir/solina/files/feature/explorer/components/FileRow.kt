package ir.solina.files.feature.explorer.components

import android.util.LruCache
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import ir.solina.files.core.model.ListPrefs
import ir.solina.files.core.model.UiDensity
import ir.solina.files.core.util.FileKind
import ir.solina.files.core.util.FileKinds
import ir.solina.files.core.util.Formatters
import ir.solina.files.feature.explorer.dnd.DropHalo
import ir.solina.files.feature.explorer.dnd.DropLook
import ir.solina.files.feature.explorer.dnd.dropHaloBehind
import ir.solina.files.feature.explorer.dnd.fileGestures
import ir.solina.files.feature.explorer.dnd.rememberDropAnimation
import ir.solina.files.ui.LocalSettings
import ir.solina.files.ui.LocalUiDensity
import ir.solina.files.ui.isolated
import ir.solina.files.ui.theme.KindPalette
import ir.solina.files.ui.theme.toShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import java.io.File

private val folderCountCache = LruCache<String, Int>(256)

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
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val density = LocalUiDensity.current
    val settings = LocalSettings.current
    val list = settings.list

    val folderItemCount by produceState<Int?>(
        initialValue = folderCountCache.get(item.path),
        item.path,
        item.isDirectory,
        singlePane,
    ) {
        if (!singlePane || !item.isDirectory || "://" in item.path || !item.canRead) {
            value = null
        } else {
            value = folderCountCache.get(item.path) ?: readShallowFolderCount(item.path)?.also {
                folderCountCache.put(item.path, it)
            }
        }
    }

    val iconSize = if (!singlePane) {
        density.iconDp.dp
    } else {
        when (density) {
            UiDensity.COMPACT -> 36.dp
            UiDensity.COMFORTABLE -> 42.dp
            UiDensity.LARGE -> 48.dp
        }
    }
    val rowVerticalPad = if (!singlePane) {
        density.rowPadDp.dp
    } else {
        when (density) {
            UiDensity.COMPACT -> 6.dp
            UiDensity.COMFORTABLE -> 8.dp
            UiDensity.LARGE -> 10.dp
        }
    }
    val iconTextGap = if (singlePane) 12.dp else 10.dp
    val fontScale = if (singlePane) {
        when (density) {
            UiDensity.COMPACT -> 1f
            UiDensity.COMFORTABLE -> 1.05f
            UiDensity.LARGE -> 1.1f
        }
    } else {
        1f
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, motion.fastSpatialSpec(), label = "press")
    val look = rememberDropAnimation(drop)
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
            .padding(horizontal = if (singlePane) 12.dp else 8.dp, vertical = rowVerticalPad),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DropHalo(look, drop.anchor) {
                Leading(
                    item = item,
                    selected = selected,
                    showThumbnail = showThumbnail,
                    iconSize = iconSize,
                    onIconClick = if (settings.behavior.iconTapSelects) onIconClick else null,
                )
            }
            Spacer(Modifier.width(iconTextGap))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    isolated(Formatters.displayName(item.name, item.isDirectory, list.showExtensions)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = (density.nameSp * fontScale).sp,
                    maxLines = if (list.wrapNames) 3 else density.nameMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                val date = if (list.showDate) Formatters.listDate(item.lastModified, list.dateStyle, list.use24Hour) else null
                val info = trailingInfo(item, list, singlePane, folderItemCount)
                if (date != null || info != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            date.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = (density.metaSp * fontScale).sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (info != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 8.dp),
                            ) {
                                if (info.first.isNotEmpty()) {
                                    Text(
                                        info.first,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = (density.metaSp * fontScale).sp,
                                        color = colors.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                info.second?.let { count ->
                                    Icon(
                                        Icons.Rounded.FolderOpen,
                                        contentDescription = stringResource(R.string.explorer_folder_items_cd),
                                        modifier = Modifier.size(13.dp),
                                        tint = colors.onSurfaceVariant,
                                    )
                                    Text(
                                        count,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = (density.metaSp * fontScale).sp,
                                        color = colors.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showSelectionCheckbox) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) colors.primary else colors.surface.copy(alpha = 0.92f))
                        .border(1.5.dp, if (selected) colors.primary else colors.outline, RoundedCornerShape(6.dp))
                        .clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.explorer_selected),
                            tint = colors.onPrimary,
                            modifier = Modifier.size(15.dp),
                        )
                    }
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
    val settings = LocalSettings.current
    val list = settings.list
    val density = LocalUiDensity.current
    val fontScale = if (singlePane) {
        when (density) {
            UiDensity.COMPACT -> 1f
            UiDensity.COMFORTABLE -> 1.05f
            UiDensity.LARGE -> 1.1f
        }
    } else {
        1f
    }
    val tileDensityScale = if (!singlePane) {
        1f
    } else {
        when (density) {
            UiDensity.COMPACT -> 1f
            UiDensity.COMFORTABLE -> 1.1f
            UiDensity.LARGE -> 1.2f
        }
    }
    val tileIconSize = density.iconDp.dp * (if (horizontalPane) 1.1f else 1.5f) * tileDensityScale
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, motion.fastSpatialSpec(), label = "press")
    val look = rememberDropAnimation(drop)
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
    val shape = settings.appearance.iconShape.toShape()
    val kind = FileKinds.of(item)

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
            .padding(8.dp)
            .testTag("file-tile"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(drop.anchor)
                .dropHaloBehind(look, reach = 0.72f),
            contentAlignment = Alignment.Center,
        ) {
            val picture = showThumbnail && list.thumbnails && (kind == FileKind.IMAGE || kind == FileKind.VIDEO)
            val apk = showThumbnail && list.apkIcons && kind == FileKind.APK
            when {
                picture -> {
                    KindIcon(item, 48.dp, shape)
                    AsyncImage(
                        model = thumbnailModel(item),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                    )
                }
                apk -> ApkIconOrKind(item, tileIconSize, shape)
                else -> KindIcon(item, tileIconSize, shape, contentPaddingFraction = 0.16f)
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
                if (selected) {
                    Box(
                        Modifier.fillMaxSize().clip(CircleShape).background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.explorer_selected),
                            tint = colors.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                } else if (selecting) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, colors.outline, CircleShape)
                            .background(colors.surface.copy(alpha = 0.6f)),
                    )
                }
            }
        }
        Text(
            isolated(Formatters.displayName(item.name, item.isDirectory, list.showExtensions)),
            style = MaterialTheme.typography.labelMedium,
            fontSize = ((if (horizontalPane) density.nameSp * 0.9f else density.nameSp * fontScale)).sp,
            maxLines = if (list.wrapNames) 3 else 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun rememberFlash(flashKey: Int, pulses: Int): Animatable<Float, *> {
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashKey) {
        if (flashKey == 0) {
            flash.snapTo(0f)
            return@LaunchedEffect
        }
        repeat(pulses) {
            flash.animateTo(1f, tween(FLASH_IN_MS, easing = FastOutSlowInEasing))
            flash.animateTo(0.25f, tween(FLASH_OUT_MS, easing = FastOutSlowInEasing))
        }
        flash.animateTo(0f, tween(FLASH_FADE_MS))
    }
    return flash
}

const val FLASH_PULSES = 2
private const val FLASH_IN_MS = 200
private const val FLASH_OUT_MS = 280
private const val FLASH_FADE_MS = 520

fun flashDurationMs(pulses: Int): Long = (pulses * (FLASH_IN_MS + FLASH_OUT_MS) + FLASH_FADE_MS).toLong()

private suspend fun readShallowFolderCount(path: String): Int? = withContext(Dispatchers.IO) {
    runCatching { File(path).list()?.size }.getOrNull()
}

@Composable
private fun trailingInfo(
    item: FileItem,
    list: ListPrefs,
    singlePane: Boolean,
    folderItemCount: Int?,
): Pair<String, String?>? = when {
    item.isSymlink -> stringResource(R.string.explorer_link) to null
    singlePane && item.isDirectory -> folderItemCount?.let { "" to Formatters.number(it.toLong()) }
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
            ImageRequest.Builder(context)
                .data(item.path.toPath())
                .decoderFactory(BitmapFactoryDecoder.Factory())
                .build()
        } else {
            item.path.toPath()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Leading(
    item: FileItem,
    selected: Boolean,
    showThumbnail: Boolean,
    iconSize: Dp,
    onIconClick: (() -> Unit)?,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val settings = LocalSettings.current
    val shape = settings.appearance.iconShape.toShape()
    val kind = FileKinds.of(item)
    Box(
        Modifier
            .size(iconSize)
            .clip(shape)
            .then(
                if (onIconClick != null) {
                    Modifier.clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() }
                } else {
                    Modifier
                },
            ),
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
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.explorer_selected),
                        tint = colors.onPrimary,
                    )
                }
                showThumbnail && settings.list.thumbnails && (kind == FileKind.IMAGE || kind == FileKind.VIDEO) -> Box(
                    Modifier.size(iconSize),
                    contentAlignment = Alignment.Center,
                ) {
                    KindIcon(item, iconSize, shape)
                    AsyncImage(
                        model = thumbnailModel(item),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(iconSize).clip(shape),
                    )
                }
                showThumbnail && settings.list.apkIcons && kind == FileKind.APK -> ApkIconOrKind(item, iconSize, shape)
                else -> KindIcon(item, iconSize, shape)
            }
        }
    }
}

@Composable
private fun ApkIconOrKind(item: FileItem, size: Dp, shape: Shape) {
    var loaded by remember(item.path) { mutableStateOf(false) }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (!loaded) KindIcon(item, size, shape)
        AsyncImage(
            model = remember(item.path, item.lastModified) { ApkIcon(item.path, item.lastModified) },
            contentDescription = stringResource(R.string.explorer_app_icon),
            onSuccess = { loaded = true },
            modifier = Modifier.size(size).testTag("apk-icon"),
        )
    }
}

@Composable
private fun KindIcon(
    item: FileItem,
    iconSize: Dp,
    shape: Shape,
    contentPaddingFraction: Float = 0.18f,
) {
    val colors = MaterialTheme.colorScheme
    val settings = LocalSettings.current
    val kind = FileKinds.of(item)
    val tile = KindPalette.colors(kind, settings.appearance.colorfulIcons, colors, colors.background.luminance() < 0.5f)
    Box(
        Modifier
            .size(iconSize)
            .clip(shape)
            .background(if (item.isDirectory) colors.primaryContainer.copy(alpha = 0.45f) else tile.container),
        contentAlignment = Alignment.Center,
    ) {
        if (item.isDirectory) {
            Icon(
                Icons.Rounded.Folder,
                contentDescription = stringResource(kind.label),
                tint = colors.primary,
                modifier = Modifier.fillMaxSize().padding(iconSize * 0.14f),
            )
        } else {
            Icon(
                kind.icon,
                contentDescription = stringResource(kind.label),
                tint = tile.content,
                modifier = Modifier.fillMaxSize().padding(iconSize * contentPaddingFraction),
            )
        }
    }
}
