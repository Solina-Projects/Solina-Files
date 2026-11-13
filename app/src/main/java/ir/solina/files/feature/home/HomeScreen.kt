package ir.solina.files.feature.home

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.solina.files.R
import ir.solina.files.core.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private data class HomeShortcut(val title: String, val path: String, val icon: ImageVector)
private data class StorageCategory(
    val title: String,
    val path: String,
    val icon: ImageVector,
    val color: Color,
    val bytes: Long,
)

@Composable
fun HomeScreen(
    dualPanePreferred: Boolean,
    onBrowse: (Boolean) -> Unit,
    onOpenFolder: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    val context = LocalContext.current
    val root = remember { Environment.getExternalStorageDirectory()?.absolutePath ?: "/storage/emulated/0" }
    var refreshKey by remember { mutableIntStateOf(0) }
    var categoryBytes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    val storage = remember(root, refreshKey) {
        runCatching {
            val stat = StatFs(root)
            val total = stat.totalBytes.coerceAtLeast(1L)
            val free = stat.availableBytes.coerceAtLeast(0L)
            Triple(total, free, (total - free).coerceAtLeast(0L))
        }.getOrElse { Triple(0L, 0L, 0L) }
    }

    LaunchedEffect(refreshKey) {
        categoryBytes = withContext(Dispatchers.IO) { queryMediaStoreCategoryBytes(context) }
    }

    val downloadsTitle = stringResource(R.string.home_cat_downloads)
    val imagesTitle = stringResource(R.string.home_cat_images)
    val documentsTitle = stringResource(R.string.home_cat_documents)
    val musicTitle = stringResource(R.string.home_cat_music)
    val videosTitle = stringResource(R.string.home_cat_videos)
    val appsTitle = stringResource(R.string.home_cat_apps)
    val internalStorageTitle = stringResource(R.string.home_internal_storage)

    val shortcuts = remember(root, downloadsTitle, imagesTitle, documentsTitle, musicTitle, videosTitle, internalStorageTitle) {
        listOf(
            HomeShortcut(downloadsTitle, "$root/Download", Icons.Rounded.Download),
            HomeShortcut(imagesTitle, "$root/DCIM", Icons.Rounded.Image),
            HomeShortcut(documentsTitle, "$root/Documents", Icons.Rounded.Description),
            HomeShortcut(musicTitle, "$root/Music", Icons.Rounded.MusicNote),
            HomeShortcut(videosTitle, "$root/Movies", Icons.Rounded.Movie),
            HomeShortcut(internalStorageTitle, root, Icons.Rounded.Storage),
        )
    }

    val categories = remember(root, categoryBytes, imagesTitle, videosTitle, documentsTitle, musicTitle, appsTitle) {
        listOf(
            StorageCategory(imagesTitle, "$root/DCIM", Icons.Rounded.Image, Color(0xFF0D9488), categoryBytes["images"] ?: 0L),
            StorageCategory(videosTitle, "$root/Movies", Icons.Rounded.Movie, Color(0xFFD97706), categoryBytes["videos"] ?: 0L),
            StorageCategory(documentsTitle, "$root/Documents", Icons.Rounded.Description, Color(0xFF0A6BCF), categoryBytes["documents"] ?: 0L),
            StorageCategory(musicTitle, "$root/Music", Icons.Rounded.MusicNote, Color(0xFFE11D48), categoryBytes["music"] ?: 0L),
            StorageCategory(appsTitle, "$root/Download", Icons.Rounded.Apps, Color(0xFF059669), categoryBytes["apps"] ?: 0L),
        )
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.home_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { refreshKey++ }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.home_refresh_cd))
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = stringResource(R.string.home_theme_cd),
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.home_settings_cd))
                    }
                }
            }

            item {
                Surface(
                    onClick = onSearch,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            stringResource(R.string.home_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    internalStorageTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    if (storage.first > 0L) {
                                        stringResource(
                                            R.string.home_storage_summary,
                                            Formatters.size(storage.third),
                                            Formatters.size(storage.second),
                                        )
                                    } else {
                                        stringResource(R.string.home_storage_unavailable)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (storage.first > 0L) {
                                val pct = (storage.third * 100 / storage.first).toInt()
                                Text(
                                    "${Formatters.number(pct.toLong())}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            val known = categories.sumOf { it.bytes }.coerceAtMost(storage.third)
                            val other = (storage.third - known).coerceAtLeast(0L)
                            val parts = categories.filter { it.bytes > 0L }.map { it.color to it.bytes } +
                                listOf(
                                    Color(0xFF64748B) to other,
                                    MaterialTheme.colorScheme.surfaceVariant to storage.second,
                                )
                            parts.filter { it.second > 0L }.forEach { (color, bytes) ->
                                Box(
                                    Modifier
                                        .weight(bytes.coerceAtLeast(1L).toFloat())
                                        .height(10.dp)
                                        .background(color),
                                )
                            }
                        }

                        Text(
                            stringResource(R.string.home_storage_categories),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        categories.forEach { category ->
                            val percent = if (storage.third > 0L) (category.bytes * 100 / storage.third).toInt() else 0
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        category.icon,
                                        contentDescription = null,
                                        tint = category.color,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        category.title,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Text(
                                        "${Formatters.size(category.bytes)} · ${Formatters.number(percent.toLong())}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    val fraction = (category.bytes.toDouble() / storage.third.coerceAtLeast(1L))
                                        .toFloat()
                                        .coerceIn(0f, 1f)
                                    Box(
                                        Modifier
                                            .fillMaxWidth(fraction)
                                            .height(4.dp)
                                            .background(category.color),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onOpenFolder(root) }) {
                                Text(stringResource(R.string.home_browse_storage))
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.home_choose_workspace),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { onBrowse(false) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (!dualPanePreferred) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(24.dp))
                                Text(
                                    stringResource(R.string.home_single_pane),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    stringResource(R.string.home_single_pane_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).clickable { onBrowse(true) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (dualPanePreferred) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Rounded.ViewWeek, contentDescription = null, modifier = Modifier.size(24.dp))
                                Text(
                                    stringResource(R.string.home_dual_pane),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    stringResource(R.string.home_dual_pane_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.home_quick_access),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { onOpenFolder(root) }) {
                            Text(stringResource(R.string.home_all_files))
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        shortcuts.forEach { shortcut ->
                            Card(
                                modifier = Modifier.width(120.dp).clickable { onOpenFolder(shortcut.path) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(
                                    Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        shortcut.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Text(
                                        shortcut.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.home_browse_categories),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    categories.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            pair.forEach { category ->
                                Card(
                                    modifier = Modifier.weight(1f).clickable { onOpenFolder(category.path) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(category.color.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                category.icon,
                                                contentDescription = null,
                                                tint = category.color,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                category.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                Formatters.size(category.bytes),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun queryMediaStoreCategoryBytes(context: Context): Map<String, Long> {
    val totals = mutableMapOf(
        "images" to 0L,
        "videos" to 0L,
        "documents" to 0L,
        "music" to 0L,
        "apps" to 0L,
    )
    val docExtensions = setOf("pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv", "epub")
    val apkExtensions = setOf("apk", "apks", "xapk")
    val projection = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
    )

    runCatching {
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val typeIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val size = cursor.getLong(sizeIdx).coerceAtLeast(0L)
                if (size == 0L) continue
                val mediaType = cursor.getInt(typeIdx)
                val name = cursor.getString(nameIdx).orEmpty()
                val ext = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
                val key = when {
                    mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> "images"
                    mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> "videos"
                    mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> "music"
                    ext in apkExtensions -> "apps"
                    ext in docExtensions -> "documents"
                    else -> null
                }
                if (key != null) {
                    totals[key] = (totals[key] ?: 0L) + size
                }
            }
        }
    }
    return totals
}
