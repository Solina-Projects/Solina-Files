package ir.solina.files.feature.explorer

import ir.solina.files.core.termux.TermuxSession
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.solina.files.R
import ir.solina.files.core.fs.Mounts
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.remote.RemoteProtocol
import ir.solina.files.core.remote.RemoteUri

/** Top app bar of the explorer (menu, filter field, search, sort, overflow menu), split out of [ExplorerScreen]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExplorerTopBar(
    ui: ExplorerUiState,
    vm: ExplorerViewModel,
    visibleItems: List<FileItem>,
    termux: TermuxSession,
    onOpenDrawer: () -> Unit,
    onOpenSearch: (String) -> Unit,
    onSearchLocalOnly: () -> Unit,
    onDialog: (Dialog) -> Unit,
) {
    var overflow by remember { mutableStateOf(false) }
    val sshHere = RemoteUri.parse(ui.activePane.path)?.let { (id, dir) ->
        ui.storages.firstOrNull { it.id == id && it.protocol == RemoteProtocol.SFTP }?.let { it to dir }
    }
    if (ui.searchActive) {
        FilterBar(query = ui.query, onQuery = vm::setQuery, onClose = { vm.setSearchActive(false) })
    } else {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Surface(
                        shape = RoundedCornerShape(13.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Icon(
                            Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.explorer_menu),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp).size(22.dp),
                        )
                    }
                }
            },
            title = { ExplorerTitle(ui.activePane, visibleItems) },
            actions = {
                IconButton(onClick = { vm.setSearchActive(true) }) { Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.explorer_filter)) }
                IconButton(onClick = {
                    if (RemoteUri.isRemote(ui.activePane.path)) {
                        onSearchLocalOnly()
                    } else {
                        onOpenSearch(ui.activePane.path)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ManageSearch, contentDescription = stringResource(R.string.explorer_advanced_search))
                }
                IconButton(onClick = { onDialog(Dialog.Sort) }) { Icon(Icons.Rounded.SortByAlpha, contentDescription = stringResource(R.string.common_sort)) }
                Box {
                    IconButton(onClick = { overflow = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.explorer_options)) }
                    DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.explorer_refresh_both)) }, onClick = { overflow = false; vm.refreshAll() })
                        DropdownMenuItem(text = { Text(stringResource(R.string.explorer_jump_to_path_more)) }, onClick = { overflow = false; onDialog(Dialog.JumpTo(ui.active)) })
                        DropdownMenuItem(
                            text = { Text(stringResource(if (ui.settings.showHidden) R.string.common_hide_hidden else R.string.common_show_hidden)) },
                            onClick = { overflow = false; vm.setShowHidden(!ui.settings.showHidden) },
                        )
                        val mount = ui.activePane.mount
                        if (ui.settings.rootAccess && mount != null && mount.point in Mounts.SYSTEM_POINTS) {
                            DropdownMenuItem(
                                text = { Text(stringResource(if (mount.readOnly) R.string.explorer_mount_rw else R.string.explorer_mount_ro, mount.point)) },
                                onClick = { overflow = false; vm.remount(ui.active, writable = mount.readOnly) },
                            )
                        }
                        sshHere?.let { (c, dir) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.explorer_ssh_here)) },
                                onClick = { overflow = false; termux.openSsh(c.host, c.port, c.username, dir) },
                            )
                        }
                    }
                }
            },
        )
    }
}
