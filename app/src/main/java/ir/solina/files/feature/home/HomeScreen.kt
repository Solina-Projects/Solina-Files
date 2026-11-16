package ir.solina.files.feature.home

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.solina.files.core.util.FileKind
import ir.solina.files.ui.theme.KindPalette
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

private data class HomeShortcut(val title: String, val path: String, val icon: ImageVector, val kind: FileKind)
private data class StorageCategory(val title: String, val path: String, val icon: ImageVector, val kind: FileKind, val bytes: Long, val sizeText: String)

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
    // Perf item 3: category totals are cached in HomeCategoryCache for the
    // session; this screen only triggers the first scan and refreshes.
    val categoryBytes by HomeCategoryCache.bytes.collectAsStateWithLifecycle()
    LaunchedEffect(root) { HomeCategoryCache.ensureScanned(root) }
    val storage = remember(root) {
        runCatching {
            val stat = StatFs(root)
            val total = stat.totalBytes.coerceAtLeast(1L)
            val free = stat.availableBytes.coerceAtLeast(0L)
            Triple(total, free, (total - free).coerceAtLeast(0L))
        }.getOrElse { Triple(0L, 0L, 0L) }
    }
    val shortcuts = remember(root) {
        listOf(
            HomeShortcut("دانلودها", "$root/Download", Icons.Rounded.Download, FileKind.FOLDER),
            HomeShortcut("تصاویر", "$root/DCIM", Icons.Rounded.Image, FileKind.IMAGE),
            HomeShortcut("اسناد", "$root/Documents", Icons.Rounded.Description, FileKind.DOCUMENT),
            HomeShortcut("موزیک", "$root/Music", Icons.Rounded.MusicNote, FileKind.AUDIO),
            HomeShortcut("فیلم‌ها", "$root/Movies", Icons.Rounded.Movie, FileKind.VIDEO),
            HomeShortcut("حافظه داخلی", root, Icons.Rounded.Storage, FileKind.FOLDER),
        )
    }
    // Redesign v2: category colours come from KindPalette (one hue per file
    // kind) instead of hardcoded random colours. Colours and formatted sizes
    // are computed once per categoryBytes change, not per recomposition.
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val kindColors = remember(scheme, dark) {
        FileKind.entries.associateWith { kind ->
            KindPalette.colors(kind, colorful = true, scheme = scheme, dark = dark).content
        }
    }
    fun kindColor(kind: FileKind): Color = kindColors.getValue(kind)
    val categories = remember(categoryBytes) {
        fun category(title: String, path: String, icon: ImageVector, kind: FileKind, bytes: Long) =
            StorageCategory(title, path, icon, kind, bytes, fa(formatBytes(bytes)))
        listOf(
            category("تصاویر", "$root/DCIM", Icons.Rounded.Image, FileKind.IMAGE, categoryBytes["images"] ?: 0L),
            category("فیلم‌ها", "$root/Movies", Icons.Rounded.Movie, FileKind.VIDEO, categoryBytes["videos"] ?: 0L),
            category("اسناد", "$root/Documents", Icons.Rounded.Description, FileKind.DOCUMENT, categoryBytes["documents"] ?: 0L),
            category("موزیک", "$root/Music", Icons.Rounded.MusicNote, FileKind.AUDIO, categoryBytes["music"] ?: 0L),
            category("برنامه‌ها / APK", "$root/Android", Icons.Rounded.Apps, FileKind.APK, categoryBytes["apps"] ?: 0L),
        )
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("سولینا فایلز", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("مدیریت فایل‌ها", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { HomeCategoryCache.refresh(root) }) { Icon(Icons.Rounded.Refresh, contentDescription = "تازه‌سازی حافظه") }
                IconButton(onClick = onToggleTheme) {
                    Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, contentDescription = "تغییر تم")
                }
                IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, contentDescription = "تنظیمات") }
            }
            // Perf item 8: the search field owns its own query state, so a
            // keystroke recomposes only the field, not the whole Home screen.
            HomeSearchField(onOpenSearch = onSearch)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("حافظه داخلی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (storage.first > 0) "${fa(formatBytes(storage.third))} استفاده‌شده · ${fa(formatBytes(storage.second))} آزاد"
                                else "جزئیات حافظه در دسترس نیست",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (storage.first > 0) Text("${fa((storage.third * 100 / storage.first).toInt().toString())}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier.fillMaxWidth().height(14.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(99.dp)),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        val known = categories.sumOf { it.bytes }.coerceAtMost(storage.third)
                        val other = (storage.third - known).coerceAtLeast(0L)
                        val parts = categories.filter { it.bytes > 0L }.map { kindColor(it.kind) to it.bytes } +
                            listOf(MaterialTheme.colorScheme.outline to other, MaterialTheme.colorScheme.surfaceVariant to storage.second)
                        parts.filter { it.second > 0L }.forEach { (color, bytes) ->
                            Box(Modifier.weight(bytes.coerceAtLeast(1L).toFloat()).height(14.dp).background(color))
                        }
                    }
                    Text("دسته‌بندی حافظه", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    categories.forEach { category ->
                        val percent = if (storage.third > 0L) (category.bytes * 100 / storage.third).toInt() else 0
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(category.icon, contentDescription = null, tint = kindColor(category.kind), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(category.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text("${category.sizeText} · ${fa(percent.toString())}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(Modifier.fillMaxWidth().height(5.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp))) {
                                Box(Modifier.fillMaxWidth((category.bytes.toDouble() / storage.third.coerceAtLeast(1L)).toFloat().coerceIn(0f, 1f)).height(5.dp).background(kindColor(category.kind), RoundedCornerShape(99.dp)))
                            }
                        }
                    }
                    TextButton(onClick = { onOpenFolder(root) }) { Text("مرور حافظه") }
                }
            }
            Text("نمای کار", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f).clickable { onBrowse(false) },
                    colors = CardDefaults.cardColors(containerColor = if (!dualPanePreferred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(28.dp))
                        Text("تک‌پنجره‌ای", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("یک پنل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { onBrowse(true) },
                    colors = CardDefaults.cardColors(containerColor = if (dualPanePreferred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.ViewWeek, contentDescription = null, modifier = Modifier.size(28.dp))
                        Text("دوپنجره‌ای", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("کپی و جابه‌جایی بین دو پنل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("دسترسی سریع", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onOpenFolder(root) }) { Text("همه فایل‌ها") }
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
                            Icon(shortcut.icon, contentDescription = null, tint = kindColor(shortcut.kind), modifier = Modifier.size(24.dp))
                            Text(shortcut.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Text("مرور بر اساس دسته", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories, key = { it.path }) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenFolder(category.path) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(category.icon, contentDescription = null, tint = kindColor(category.kind), modifier = Modifier.size(23.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(category.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(category.sizeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSearchField(onOpenSearch: () -> Unit) {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("جست‌وجوی فایل‌ها و پوشه‌ها") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = { IconButton(onClick = onOpenSearch) { Icon(Icons.Rounded.Search, contentDescription = "باز کردن جستجو") } },
        shape = RoundedCornerShape(18.dp),
    )
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
