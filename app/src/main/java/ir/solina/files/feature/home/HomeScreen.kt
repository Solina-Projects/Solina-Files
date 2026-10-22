package ir.solina.files.feature.home

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Movie
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private data class HomeShortcut(val title: String, val path: String, val icon: ImageVector)
private data class StorageCategory(val title: String, val path: String, val icon: ImageVector, val color: Color, val bytes: Long)

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
    LaunchedEffect(root, refreshKey) {
        categoryBytes = withContext(Dispatchers.IO) { scanCategoryBytes(File(root)) }
    }
    val shortcuts = remember(root) {
        listOf(
            HomeShortcut("Downloads", "$root/Download", Icons.Rounded.Download),
            HomeShortcut("Images", "$root/DCIM", Icons.Rounded.Image),
            HomeShortcut("Documents", "$root/Documents", Icons.Rounded.Description),
            HomeShortcut("Music", "$root/Music", Icons.Rounded.MusicNote),
            HomeShortcut("Videos", "$root/Movies", Icons.Rounded.Movie),
            HomeShortcut("Internal storage", root, Icons.Rounded.Storage),
        )
    }
    val categories = listOf(
        StorageCategory("Images", "$root/DCIM", Icons.Rounded.Image, Color(0xFF4CAF50), categoryBytes["images"] ?: 0L),
        StorageCategory("Videos", "$root/Movies", Icons.Rounded.Movie, Color(0xFFFF9800), categoryBytes["videos"] ?: 0L),
        StorageCategory("Documents", "$root/Documents", Icons.Rounded.Description, Color(0xFF2196F3), categoryBytes["documents"] ?: 0L),
        StorageCategory("Music", "$root/Music", Icons.Rounded.MusicNote, Color(0xFF9C27B0), categoryBytes["music"] ?: 0L),
        StorageCategory("Apps / APK", "$root/Android", Icons.Rounded.Apps, Color(0xFFE91E63), categoryBytes["apps"] ?: 0L),
    )
    var query by remember { mutableStateOf("") }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Solina Files", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("File manager", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { refreshKey++ }) { Icon(Icons.Rounded.Refresh, contentDescription = "Refresh storage") }
                IconButton(onClick = onToggleTheme) {
                    Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, contentDescription = "Toggle theme")
                }
                IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Settings") }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search files and folders") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { IconButton(onClick = onSearch) { Icon(Icons.Rounded.Search, contentDescription = "Open search") } },
                shape = RoundedCornerShape(18.dp),
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Internal storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (storage.first > 0) "${fa(formatBytes(storage.third))} used · ${fa(formatBytes(storage.second))} free"
                                else "Storage details unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (storage.first > 0) Text("${fa((storage.third * 100 / storage.first).toInt().toString())}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier.fillMaxWidth().height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp)),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        val known = categories.sumOf { it.bytes }.coerceAtMost(storage.third)
                        val other = (storage.third - known).coerceAtLeast(0L)
                        val parts = categories.filter { it.bytes > 0L }.map { it.color to it.bytes } +
                            listOf(Color(0xFF78909C) to other, MaterialTheme.colorScheme.surfaceVariant to storage.second)
                        parts.filter { it.second > 0L }.forEach { (color, bytes) ->
                            Box(Modifier.weight(bytes.coerceAtLeast(1L).toFloat()).height(12.dp).background(color))
                        }
                    }
                    Text("Storage categories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    categories.forEach { category ->
                        val percent = if (storage.third > 0L) (category.bytes * 100 / storage.third).toInt() else 0
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(category.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text("${fa(formatBytes(category.bytes))} · ${fa(percent.toString())}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(Modifier.fillMaxWidth().height(5.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp))) {
                                Box(Modifier.fillMaxWidth((category.bytes.toDouble() / storage.third.coerceAtLeast(1L)).toFloat().coerceIn(0f, 1f)).height(5.dp).background(category.color, RoundedCornerShape(99.dp)))
                            }
                        }
                    }
                    TextButton(onClick = { onOpenFolder(root) }) { Text("Browse storage") }
                }
            }
            Text("Choose your workspace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f).clickable { onBrowse(false) },
                    colors = CardDefaults.cardColors(containerColor = if (!dualPanePreferred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(28.dp))
                        Text("Single pane", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("One panel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { onBrowse(true) },
                    colors = CardDefaults.cardColors(containerColor = if (dualPanePreferred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.ViewWeek, contentDescription = null, modifier = Modifier.size(28.dp))
                        Text("Dual pane", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Copy and move between panels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quick access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onOpenFolder(root) }) { Text("All files") }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                shortcuts.forEach { shortcut ->
                    Card(
                        modifier = Modifier.width(118.dp).clickable { onOpenFolder(shortcut.path) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(shortcut.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Text(shortcut.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Text("Browse by category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenFolder(category.path) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(23.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(category.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${fa(formatBytes(category.bytes))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun scanCategoryBytes(root: File): Map<String, Long> {
    val totals = mutableMapOf("images" to 0L, "videos" to 0L, "documents" to 0L, "music" to 0L, "apps" to 0L)
    if (!root.exists() || !root.canRead()) return totals
    val image = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "tif", "tiff")
    val video = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v", "ts")
    val docs = setOf("pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv", "epub")
    val music = setOf("mp3", "flac", "wav", "ogg", "m4a", "aac", "opus", "wma")
    runCatching {
        root.walkTopDown().onEnter { dir ->
            val path = dir.absolutePath.replace('\\', '/')
            !path.contains("/Android/data") && !path.contains("/Android/obb") && !path.contains("/.thumbnails")
        }.forEach { file ->
            if (!file.isFile) return@forEach
            val ext = file.extension.lowercase(Locale.ROOT)
            val key = when {
                ext in image -> "images"
                ext in video -> "videos"
                ext in docs -> "documents"
                ext in music -> "music"
                ext in setOf("apk", "apks", "xapk") -> "apps"
                else -> null
            }
            if (key != null) totals[key] = (totals[key] ?: 0L) + file.length().coerceAtLeast(0L)
        }
    }
    return totals
}

private fun fa(value: String): String = value.map { if (it in '0'..'9') ('۰'.code + (it.code - '0'.code)).toChar() else it }.joinToString("")

private fun formatBytes(value: Long): String {
    if (value < 1024) return "${value} B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var index = -1
    do {
        size /= 1024.0
        index++
    } while (size >= 1024.0 && index < units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", size, units[index])
}
