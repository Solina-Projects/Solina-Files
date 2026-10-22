package ir.solina.files.feature.home

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale

private data class HomeShortcut(val title: String, val path: String, val icon: ImageVector)

@Composable
fun HomeScreen(
    dualPanePreferred: Boolean,
    onBrowse: (Boolean) -> Unit,
    onOpenFolder: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
) {
    val root = remember { Environment.getExternalStorageDirectory()?.absolutePath ?: "/storage/emulated/0" }
    val storage = remember(root) {
        runCatching {
            val stat = StatFs(root)
            val total = stat.totalBytes.coerceAtLeast(1L)
            val free = stat.availableBytes.coerceAtLeast(0L)
            Triple(total, free, total - free)
        }.getOrElse { Triple(0L, 0L, 0L) }
    }
    val shortcuts = remember(root) {
        listOf(
            HomeShortcut("Downloads", "$root/Download", Icons.Rounded.Download),
            HomeShortcut("Images", "$root/DCIM", Icons.Rounded.Image),
            HomeShortcut("Documents", "$root/Documents", Icons.Rounded.Description),
            HomeShortcut("Music", "$root/Music", Icons.Rounded.MusicNote),
            HomeShortcut("Apps", "$root/Android", Icons.Rounded.Apps),
            HomeShortcut("Internal storage", root, Icons.Rounded.Storage),
        )
    }
    var query by remember { mutableStateOf("") }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Strata", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("File manager", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                androidx.compose.material3.IconButton(onClick = onSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search files and folders") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    androidx.compose.material3.IconButton(onClick = onSearch) {
                        Icon(Icons.Rounded.Search, contentDescription = "Open search")
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(26.dp),
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Internal storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (storage.first > 0) "${formatBytes(storage.third)} used · ${formatBytes(storage.second)} free"
                                else "Storage details unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (storage.first > 0) Text("${(storage.third * 100 / storage.first).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp))) {
                        Box(
                            Modifier.fillMaxWidth(if (storage.first > 0) (storage.third.toFloat() / storage.first).coerceIn(0f, 1f) else 0f)
                                .height(10.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(99.dp))
                        )
                    }
                    TextButton(onClick = { onOpenFolder(root) }) { Text("Browse storage") }
                }
            }
            Text("Choose your workspace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f).clickable { onBrowse(false) },
                    colors = CardDefaults.cardColors(containerColor = if (!dualPanePreferred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(30.dp))
                        Text("Single pane", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Simple browsing on one panel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { onBrowse(true) },
                    colors = CardDefaults.cardColors(containerColor = if (dualPanePreferred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.ViewWeek, contentDescription = null, modifier = Modifier.size(30.dp))
                        Text("Dual pane", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Compare folders and transfer between panels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quick access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onOpenFolder(root) }) { Text("All files") }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shortcuts) { shortcut ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenFolder(shortcut.path) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(shortcut.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Text(shortcut.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

private fun formatBytes(value: Long): String {
    if (value < 1024) return "$value B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var index = -1
    do {
        size /= 1024.0
        index++
    } while (size >= 1024.0 && index < units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", size, units[index])
}
