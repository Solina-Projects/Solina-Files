package ir.solina.files.feature.explorer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.solina.files.R
import ir.solina.files.core.bookmarks.Bookmark
import ir.solina.files.core.bookmarks.Bookmarks
import ir.solina.files.ui.asPath
import ir.solina.files.ui.expressive.ArtTone
import ir.solina.files.ui.expressive.ExpressiveEmptyState

class BookmarkActions(
    val onOpen: (Bookmark) -> Unit,
    val onAddCurrent: () -> Unit,
    val onUpdate: (path: String, edited: Bookmark) -> Unit,
    val onMove: (path: String, offset: Int) -> Unit,
    val onDelete: (path: String) -> Unit,
    val onRenameGroup: (from: String, to: String) -> Unit,
    val onDeleteGroup: (String) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(bookmarks: List<Bookmark>, actions: BookmarkActions, onDismiss: () -> Unit) {
    var editing by remember { mutableStateOf<Bookmark?>(null) }
    var deleting by remember { mutableStateOf<Bookmark?>(null) }
    var renamingGroup by remember { mutableStateOf<String?>(null) }
    var deletingGroup by remember { mutableStateOf<String?>(null) }
    val groups = Bookmarks.groups(bookmarks).filter { g -> g == Bookmarks.DEFAULT_GROUP || bookmarks.any { it.group == g } }
    val showHeaders = groups.size > 1

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.common_bookmarks), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = actions.onAddCurrent) {
                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.explorer_add_current_folder))
            }
        }
        if (bookmarks.isEmpty()) {
            ExpressiveEmptyState(
                stringResource(R.string.explorer_no_bookmarks),
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                icon = Icons.Rounded.Bookmarks,
                tone = ArtTone.TERTIARY,
                body = stringResource(R.string.explorer_no_bookmarks_body),
                maxHero = 104.dp,
            )
        }
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 560.dp).padding(bottom = 16.dp)) {
            for (group in groups) {
                val members = Bookmarks.inGroup(bookmarks, group)
                if (showHeaders) {
                    item(key = "group:$group") {
                        GroupHeader(
                            name = group,
                            onRename = { renamingGroup = group },
                            onDelete = { deletingGroup = group },
                        )
                    }
                }
                items(members, key = { "b:${it.path}" }) { b ->
                    BookmarkRow(
                        bookmark = b,
                        isFirst = b == members.first(),
                        isLast = b == members.last(),
                        onOpen = { actions.onOpen(b) },
                        onEdit = { editing = b },
                        onMove = { offset -> actions.onMove(b.path, offset) },
                        onDelete = { deleting = b },
                    )
                }
            }
        }
    }

    editing?.let { b ->
        BookmarkEditDialog(
            bookmark = b,
            groups = groups,
            onSave = { edited -> actions.onUpdate(b.path, edited); editing = null },
            onDismiss = { editing = null },
        )
    }
    deleting?.let { b ->
        ConfirmDialog(
            title = stringResource(R.string.explorer_delete_bookmark),
            message = stringResource(R.string.explorer_delete_bookmark_confirm, b.name),
            confirmText = stringResource(R.string.common_delete),
            onConfirm = { actions.onDelete(b.path); deleting = null },
            onDismiss = { deleting = null },
        )
    }
    renamingGroup?.let { g ->
        InputDialog(
            title = stringResource(R.string.explorer_rename_group),
            label = stringResource(R.string.explorer_group_name),
            initial = g,
            select = InputSelection.ALL,
            onConfirm = { actions.onRenameGroup(g, it); renamingGroup = null },
            onDismiss = { renamingGroup = null },
        )
    }
    deletingGroup?.let { g ->
        ConfirmDialog(
            title = stringResource(R.string.explorer_delete_group),
            message = stringResource(R.string.explorer_delete_group_confirm, g),
            confirmText = stringResource(R.string.common_delete),
            onConfirm = { actions.onDeleteGroup(g); deletingGroup = null },
            onDismiss = { deletingGroup = null },
        )
    }
}

@Composable
private fun GroupHeader(name: String, onRename: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            name.ifEmpty { stringResource(R.string.explorer_default_group) },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (name != Bookmarks.DEFAULT_GROUP) {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.explorer_group_options)) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.explorer_rename_group)) }, onClick = { menu = false; onRename() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.explorer_delete_group)) }, onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun BookmarkRow(
    bookmark: Bookmark,
    isFirst: Boolean,
    isLast: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val description = stringResource(R.string.explorer_bookmark_cd, bookmark.name)
    ListItem(
        content = { Text(bookmark.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(bookmark.path, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall.asPath())
        },
        leadingContent = { Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.explorer_options_for, bookmark.name)) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.common_edit)) }, onClick = { menu = false; onEdit() })
                    if (!isFirst) DropdownMenuItem(text = { Text(stringResource(R.string.explorer_move_up)) }, onClick = { menu = false; onMove(-1) })
                    if (!isLast) DropdownMenuItem(text = { Text(stringResource(R.string.explorer_move_down)) }, onClick = { menu = false; onMove(+1) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.common_delete)) }, onClick = { menu = false; onDelete() })
                }
            }
        },
        modifier = Modifier
            .semantics { contentDescription = description }
            .clickable(onClick = onOpen),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookmarkEditDialog(bookmark: Bookmark, groups: List<String>, onSave: (Bookmark) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(bookmark.name) }
    var path by remember { mutableStateOf(bookmark.path) }
    var group by remember { mutableStateOf(bookmark.group) }
    val valid = name.isNotBlank() && path.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.explorer_edit_bookmark)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.common_name)) }, singleLine = true)
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text(stringResource(R.string.common_path)) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                )
                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text(stringResource(R.string.explorer_bookmark_group)) },
                    placeholder = { Text(stringResource(R.string.explorer_default_group)) },
                    singleLine = true,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf(Bookmarks.DEFAULT_GROUP) + groups).distinct().forEach { g ->
                        FilterChip(selected = group.trim() == g, onClick = { group = g }, label = { Text(g.ifEmpty { stringResource(R.string.explorer_default_group) }) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(Bookmark(name.trim(), path.trim(), group.trim())) }, enabled = valid) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}
