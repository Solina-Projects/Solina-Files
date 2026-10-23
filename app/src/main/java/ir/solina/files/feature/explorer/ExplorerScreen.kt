package ir.solina.files.feature.explorer

import ir.solina.files.core.i18n.separator
import ir.solina.files.feature.vault.VaultSetupDialog
import ir.solina.files.feature.vault.VaultUnlockDialog
import android.app.Activity
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.solina.files.R
import ir.solina.files.core.i18n.tr
import ir.solina.files.core.fs.Mounts
import ir.solina.files.core.data.sortFor
import ir.solina.files.core.model.FolderSorts
import ir.solina.files.core.archive.ArchiveFormat
import ir.solina.files.core.fs.Conflict
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.model.FloatingAction
import ir.solina.files.core.open.Opener
import ir.solina.files.core.remote.RemoteProtocol
import ir.solina.files.core.remote.RemoteUri
import ir.solina.files.core.termux.Project
import ir.solina.files.core.termux.TermuxProjects
import ir.solina.files.core.util.FileKind
import ir.solina.files.core.util.FileKinds
import ir.solina.files.core.util.IntentUtils
import ir.solina.files.feature.explorer.dnd.DragLayer
import ir.solina.files.feature.explorer.dnd.FileDragState
import ir.solina.files.feature.explorer.dnd.LocalFileDrag
import ir.solina.files.feature.explorer.dnd.dragHost
import ir.solina.files.feature.apk.SignApkDialog
import ir.solina.files.feature.explorer.components.BatchRenameDialog
import ir.solina.files.feature.explorer.components.BookmarkActions
import ir.solina.files.feature.explorer.components.BookmarksSheet
import ir.solina.files.feature.explorer.components.BottomBarActions
import ir.solina.files.feature.explorer.components.ChecksumDialog
import ir.solina.files.feature.explorer.components.CreateArchiveDialog
import ir.solina.files.feature.explorer.components.CreateLinkDialog
import ir.solina.files.feature.explorer.components.DeleteDialog
import ir.solina.files.feature.explorer.components.EncryptDialog
import ir.solina.files.feature.explorer.components.ExplorerBottomBar
import ir.solina.files.feature.explorer.components.InputDialog
import ir.solina.files.feature.explorer.components.InputSelection
import ir.solina.files.feature.explorer.components.ItemActionSheet
import ir.solina.files.feature.explorer.components.ItemActions
import ir.solina.files.feature.explorer.components.MergeDialog
import ir.solina.files.feature.explorer.components.PaneColumn
import ir.solina.files.feature.explorer.components.PasswordDialog
import ir.solina.files.feature.explorer.components.SortSheet
import ir.solina.files.feature.explorer.components.SplitDialog
import ir.solina.files.feature.explorer.components.TaskOverlay
import ir.solina.files.feature.explorer.components.TransferDialog
import ir.solina.files.feature.props.PropertiesSheet
import ir.solina.files.feature.remote.HostKeyChangedDialog
import ir.solina.files.feature.remote.StoragePasswordDialog
import ir.solina.files.feature.settings.ToolbarTab
import ir.solina.files.feature.termux.NewProjectSheet
import ir.solina.files.feature.termux.ProjectRunSheet
import ir.solina.files.feature.termux.TerminalGate
import ir.solina.files.feature.termux.TermuxViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Dialog {
    data object None : Dialog
    data class NewFolder(val pane: Pane) : Dialog
    data class NewFile(val pane: Pane, val initial: String = "") : Dialog
    data object Delete : Dialog
    data object Sort : Dialog
    data object Bookmarks : Dialog
    data class CreateArchive(val pane: Pane, val items: List<FileItem>) : Dialog
    data class BatchRename(val pane: Pane, val items: List<FileItem>) : Dialog
    data class CreateLink(val pane: Pane, val items: List<FileItem>) : Dialog
    data class Split(val pane: Pane, val item: FileItem) : Dialog
    data class Merge(val pane: Pane, val items: List<FileItem>) : Dialog
    data class Encrypt(val pane: Pane, val item: FileItem) : Dialog
    data class Decrypt(val pane: Pane, val item: FileItem) : Dialog
    data class SignApk(val item: FileItem) : Dialog
    data class Transfer(val move: Boolean) : Dialog
    data class JumpTo(val pane: Pane) : Dialog
    data class Rename(val pane: Pane, val item: FileItem) : Dialog
    data class ItemSheet(val pane: Pane, val item: FileItem) : Dialog
    data class Checksum(val item: FileItem) : Dialog
    data class Properties(val items: List<FileItem>) : Dialog
    data class NewProject(val dir: String) : Dialog
    data class RunProject(val project: Project) : Dialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    vm: ExplorerViewModel,
    onOpenFile: (FileItem) -> Unit,
    onOpenWithOpener: (FileItem, Opener) -> Unit,
    onOpenWith: (FileItem) -> Unit,
    onCompare: (FileItem, FileItem) -> Unit,
    onOpenSearch: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onCustomize: (ToolbarTab) -> Unit = {},
    drawerOpen: Boolean = false,
    appSnackbar: SnackbarHostState? = null,
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<Dialog>(Dialog.None) }
    var overflow by remember { mutableStateOf(false) }
    var lastBackPress by remember { mutableLongStateOf(0L) }
    val rootPath = vm.rootPath
    val dual = ui.settings.dualPanePreferred
    var horizontalSplit by rememberSaveable { mutableStateOf(false) }
    var quickCreateExpanded by rememberSaveable { mutableStateOf(false) }
    var floatingMoreExpanded by rememberSaveable { mutableStateOf(false) }
    var layoutMenuExpanded by remember { mutableStateOf(false) }
    val createIconRotation by animateFloatAsState(if (quickCreateExpanded) 45f else 0f, label = "createFabRotation")
    val layoutIconScale by animateFloatAsState(if (layoutMenuExpanded) 0.88f else 1f, label = "layoutFabScale")
    val paneIconRotation by animateFloatAsState(if (dual) 180f else 0f, label = "paneFabRotation")
    val termux = viewModel<TermuxViewModel>().session
    val sshHere = RemoteUri.parse(ui.activePane.path)?.let { (id, dir) ->
        ui.storages.firstOrNull { it.id == id && it.protocol == RemoteProtocol.SFTP }?.let { it to dir }
    }
    val activePath = ui.activePane.path
    val project by produceState<Project?>(null, activePath, ui.activePane.items) {
        value = if (RemoteUri.isRemote(activePath)) null else withContext(Dispatchers.IO) { TermuxProjects.detect(java.io.File(activePath)) }
    }

    LaunchedEffect(ui.message) {
        ui.message?.let {
            vm.consumeMessage()
            (appSnackbar ?: snackbar).showSnackbar(it)
        }
    }

    BackHandler(enabled = !drawerOpen) {
        val pane = ui.activePane
        when {
            pane.hasSelection -> vm.clearSelection(ui.active)
            ui.searchActive -> vm.setSearchActive(false)
            pane.path != rootPath && pane.path != "/" -> vm.navigateUp(ui.active)
            !ui.settings.behavior.doubleBackToExit -> (context as? Activity)?.finish()
            else -> {
                val now = SystemClock.uptimeMillis()
                if (now - lastBackPress < 2000) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPress = now
                    scope.launch { snackbar.showSnackbar(tr(R.string.explorer_press_back_again)) }
                }
            }
        }
    }

    fun withLocal(item: FileItem, use: (FileItem) -> Unit) {
        if (RemoteUri.isRemote(item.path)) vm.openRemote(item, use) else use(item)
    }

    fun localOnly(item: FileItem, block: () -> Unit) {
        if (RemoteUri.isRemote(item.path)) {
            dialog = Dialog.None
            scope.launch { (appSnackbar ?: snackbar).showSnackbar(tr(R.string.explorer_not_on_storage)) }
        } else {
            block()
        }
    }

    fun handleOpen(pane: Pane, item: FileItem) {
        when {
            item.isDirectory -> vm.open(pane, item)
            FileKinds.of(item) == FileKind.ENCRYPTED -> localOnly(item) { dialog = Dialog.Decrypt(pane, item) }
            else -> withLocal(item) { onOpenFile(it) }
        }
    }

    fun visibleOfActive() = visibleItems(ui.activePane, ui.settings, ui.query).map { it.path }

    val selection = ui.activePane.items.filter { it.path in ui.activePane.selected }
    val single = selection.singleOrNull()
    val smaliFolder by produceState(false, single?.path, ui.active) {
        value = single != null && single.isDirectory && vm.hasSmali(ui.active, single)
    }
    val jarFile = single != null && !single.isDirectory && single.extension.equals("jar", ignoreCase = true)

    fun selectionInOrder() = visibleItems(ui.activePane, ui.settings, ui.query).filter { it.path in ui.activePane.selected }

    fun archiveName(items: List<FileItem>, folder: String): String =
        items.singleOrNull()?.let { ArchiveFormat.ZIP.defaultName(it).removeSuffix(ArchiveFormat.ZIP.extension) }
            ?: java.io.File(folder).name.ifEmpty { "archive" }

    fun linkParent(pane: Pane): String =
        if (dual && ui.pane(pane.other).path != ui.pane(pane).path) ui.pane(pane.other).path else ui.pane(pane).path

    val drag = remember { FileDragState() }
    drag.haptics = LocalHapticFeedback.current
    drag.onDrop = { session, spot -> vm.dropInto(session.pane, session.items, spot.dir) }
    drag.onSpring = { spot -> Pane.entries.getOrNull(spot.pane)?.let { p -> vm.setActive(p); vm.openPath(p, spot.dir) } }
    val host = remember { arrayOfNulls<LayoutCoordinates>(1) }

    LaunchedEffect(ui.undo?.seq) {
        val u = ui.undo ?: return@LaunchedEffect
        val result = (appSnackbar ?: snackbar).showSnackbar(u.message, actionLabel = tr(R.string.explorer_undo), duration = SnackbarDuration.Long)
        if (result == SnackbarResult.ActionPerformed) vm.undoMove(u) else vm.consumeUndo(u.seq)
    }

    LaunchedEffect(ui.incoming?.seq) {
        val c = ui.incoming ?: return@LaunchedEffect
        val result = (appSnackbar ?: snackbar).showSnackbar(
            tr(R.string.explorer_incoming_hidden, c.name),
            actionLabel = tr(R.string.explorer_incoming_save),
            withDismissAction = true,
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) vm.saveIncomingCopy(c) else vm.consumeIncoming(c.seq)
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { host[0] = it }
            .dragHost(drag) { host[0] },
    ) {
    CompositionLocalProvider(LocalFileDrag provides drag) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
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
                    title = { ExplorerTitle(ui.activePane, visibleItems(ui.activePane, ui.settings, ui.query)) },
                    actions = {
                        IconButton(onClick = { vm.setSearchActive(true) }) { Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.explorer_filter)) }
                        IconButton(onClick = {
                            if (RemoteUri.isRemote(ui.activePane.path)) {
                                scope.launch { snackbar.showSnackbar(tr(R.string.explorer_search_local_only)) }
                            } else {
                                onOpenSearch(ui.activePane.path)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ManageSearch, contentDescription = stringResource(R.string.explorer_advanced_search))
                        }
                        IconButton(onClick = { dialog = Dialog.Sort }) { Icon(Icons.Rounded.SortByAlpha, contentDescription = stringResource(R.string.common_sort)) }
                        Box {
                            IconButton(onClick = { overflow = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.explorer_options)) }
                            DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.explorer_refresh_both)) }, onClick = { overflow = false; vm.refreshAll() })
                                DropdownMenuItem(text = { Text(stringResource(R.string.explorer_jump_to_path_more)) }, onClick = { overflow = false; dialog = Dialog.JumpTo(ui.active) })
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
        },
        bottomBar = {
            ExplorerBottomBar(
                selectionCount = ui.activePane.selected.size,
                canBack = ui.activePane.history.canBack,
                canForward = ui.activePane.history.canForward,
                activeIsLeft = ui.active == Pane.LEFT,
                dual = dual,
                clipboardCount = ui.clipboard?.items?.size ?: 0,
                clipboardIsMove = ui.clipboard?.move == true,
                selectedFiles = selection.count { !it.isDirectory },
                remote = ui.isRemote(ui.active),
                projectLabel = project?.kind?.label,
                smaliFolder = smaliFolder,
                jarFile = jarFile,
                vault = ui.isVault(ui.active),
                browse = ui.settings.actions.browse,
                selection = ui.settings.actions.selection,
                showHidden = ui.settings.showHidden,
                horizontalSplit = horizontalSplit,
                onToggleOrientation = { horizontalSplit = !horizontalSplit },
                actions = BottomBarActions(
                    onBack = { vm.goBack(ui.active) },
                    onForward = { vm.goForward(ui.active) },
                    onUp = { vm.navigateUp(ui.active) },
                    onHome = { vm.goHome(ui.active) },
                    onSwap = vm::swapPanes,
                    onNewFolder = { dialog = Dialog.NewFolder(ui.active) },
                    onNewFile = { dialog = Dialog.NewFile(ui.active) },
                    onRefresh = { vm.refresh(ui.active) },
                    onPaste = { vm.paste(ui.active) },
                    onCancelClipboard = vm::clearClipboard,
                    onSelectAll = { vm.selectAll(ui.active, visibleOfActive()) },
                    onClearSelection = { vm.clearSelection(ui.active) },
                    onCopyToOther = { vm.beginTransfer(move = false) },
                    onMoveToOther = { vm.beginTransfer(move = true) },
                    onCopyToClipboard = { vm.copySelectionToClipboard(ui.active, move = false) },
                    onCutToClipboard = { vm.copySelectionToClipboard(ui.active, move = true) },
                    onDelete = { dialog = Dialog.Delete },
                    onRename = { selection.firstOrNull()?.let { dialog = Dialog.Rename(ui.active, it) } },
                    onCompress = { dialog = Dialog.CreateArchive(ui.active, selectionInOrder()) },
                    onShare = { IntentUtils.share(context, selection) },
                    onProperties = { dialog = Dialog.Properties(selection) },
                    onInvert = { vm.invertSelection(ui.active, visibleOfActive()) },
                    onBookmarks = { dialog = Dialog.Bookmarks },
                    onBookmarkFolder = { vm.addBookmarks(listOf(ui.activePane.path)) },
                    onBookmarkSelection = { vm.addSelectionToBookmarks(ui.active) },
                    onBatchRename = { dialog = Dialog.BatchRename(ui.active, selectionInOrder()) },
                    onCreateLinks = { dialog = Dialog.CreateLink(ui.active, selectionInOrder()) },
                    onMerge = { dialog = Dialog.Merge(ui.active, selectionInOrder().filterNot { it.isDirectory }) },
                    onCompare = {
                        val two = selectionInOrder().filterNot { it.isDirectory }
                        if (two.size == 2) onCompare(two[0], two[1])
                    },
                    onNewProject = { dialog = Dialog.NewProject(ui.activePane.path) },
                    onOpenTerminal = { termux.openTerminal(ui.activePane.path) },
                    onRunProject = { project?.let { dialog = Dialog.RunProject(it) } },
                    onSmaliToDex = { single?.let { vm.smaliToDex(ui.active, it) } },
                    onJarToDex = { single?.let { vm.jarToDex(ui.active, it) } },
                    onFilter = { vm.setSearchActive(true) },
                    onSearch = {
                        if (RemoteUri.isRemote(ui.activePane.path)) {
                            scope.launch { snackbar.showSnackbar(tr(R.string.explorer_search_local_only)) }
                        } else {
                            onOpenSearch(ui.activePane.path)
                        }
                    },
                    onSort = { dialog = Dialog.Sort },
                    onJump = { dialog = Dialog.JumpTo(ui.active) },
                    onToggleHidden = { vm.setShowHidden(!ui.settings.showHidden) },
                    onCustomize = onCustomize,
                    onMoveToVault = { vm.moveSelectionToVault(ui.active) },
                    onRestore = { vm.restoreFromVault(ui.active) },
                ),
            )
        },
    ) { padding ->
        val panes = if (dual) listOf(Pane.LEFT, Pane.RIGHT) else listOf(ui.active)
        @Composable
        fun PaneContent(pane: Pane, modifier: Modifier) {
            val state = ui.pane(pane)
            val visible = remember(state.items, ui.settings, ui.query) { visibleItems(state, ui.settings, ui.query) }
            PaneColumn(
                state = state,
                visible = visible,
                isActive = ui.active == pane,
                showActive = dual,
                rootPath = rootPath,
                onActivate = { vm.setActive(pane) },
                onUp = { vm.setActive(pane); vm.navigateUp(pane) },
                onOpen = { handleOpen(pane, it) },
                onToggleSelect = { vm.toggleSelect(pane, it) },
                onItemMenu = { dialog = Dialog.ItemSheet(pane, it) },
                onJumpTo = { dialog = Dialog.JumpTo(pane) },
                onRefresh = { vm.setActive(pane); vm.refresh(pane) },
                modifier = modifier,
                displayPath = if (RemoteUri.isRemote(state.path)) ui.displayPath(state.path) else null,
                pane = pane,
                horizontalItems = dual && horizontalSplit,
                singlePane = !dual,
            )
        }
        AnimatedContent(
            targetState = dual to horizontalSplit,
            transitionSpec = {
                (fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.92f))
                    .togetherWith(fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.96f))
            },
            label = "singleDualPaneTransition",
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { (showDual, splitHorizontal) ->
            if (showDual && splitHorizontal) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    PaneContent(Pane.LEFT, Modifier.weight(1f).fillMaxWidth())
                    PaneContent(Pane.RIGHT, Modifier.weight(1f).fillMaxWidth())
                }
            } else if (!showDual) {
                PaneContent(ui.active, Modifier.fillMaxSize().padding(horizontal = 4.dp))
            } else {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(Pane.LEFT, Pane.RIGHT).forEach { pane ->
                        PaneContent(pane, Modifier.weight(1f).fillMaxSize())
                    }
                }
            }
        }
    }
    }
    if (!ui.searchActive) {
        Column(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(end = 16.dp, bottom = 84.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val floatingZones = ui.settings.actions.floating.zones
            if (floatingZones.getOrNull(0).orEmpty().contains(FloatingAction.CREATE)) {
            AnimatedVisibility(
                visible = quickCreateExpanded,
                enter = fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.7f),
                exit = fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.7f),
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExtendedFloatingActionButton(
                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFolder(ui.active) },
                        icon = { Icon(Icons.Rounded.CreateNewFolder, contentDescription = null) },
                        text = { Text("پوشه جدید") },
                    )
                    ExtendedFloatingActionButton(
                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFile(ui.active, "untitled.txt") },
                        icon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                        text = { Text("فایل متنی") },
                    )
                    ExtendedFloatingActionButton(
                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFile(ui.active, "index.html") },
                        icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                        text = { Text("فایل HTML") },
                    )
                    if (!ui.isRemote(ui.active)) {
                        ExtendedFloatingActionButton(
                            onClick = { quickCreateExpanded = false; dialog = Dialog.NewProject(ui.activePane.path) },
                            icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                            text = { Text("پروژه جدید") },
                        )
                    }
                }
            }
            if (floatingZones.getOrNull(0).orEmpty().contains(FloatingAction.CREATE)) {
                FloatingActionButton(
                    onClick = { quickCreateExpanded = !quickCreateExpanded; layoutMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = if (quickCreateExpanded) "بستن ایجاد سریع" else "ایجاد فایل یا پوشه",
                        modifier = Modifier.graphicsLayer { rotationZ = createIconRotation },
                    )
                }
            }
            if (floatingZones.getOrNull(0).orEmpty().contains(FloatingAction.ORIENTATION) && dual) {
                FloatingActionButton(
                    onClick = { horizontalSplit = !horizontalSplit },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(
                        imageVector = if (horizontalSplit) Icons.Rounded.SwapVert else Icons.Rounded.SwapHoriz,
                        contentDescription = if (horizontalSplit) "چیدن پنل‌ها روی هم" else "چیدن پنل‌ها کنار هم",
                        modifier = Modifier.graphicsLayer { rotationZ = if (horizontalSplit) 90f else 0f },
                    )
                }
            } else if (floatingZones.getOrNull(0).orEmpty().contains(FloatingAction.VIEW_LAYOUT) && !dual) {
                Box {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedVisibility(
                            visible = layoutMenuExpanded,
                            enter = fadeIn() + scaleIn(initialScale = 0.82f),
                            exit = fadeOut() + scaleOut(targetScale = 0.82f),
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 6.dp,
                                shadowElevation = 8.dp,
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        val isList = ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.LIST
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = if (isList) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                        ) {
                                            IconButton(
                                                onClick = { if (!isList) vm.toggleViewMode() },
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Icon(
                                                    Icons.Rounded.ViewList,
                                                    contentDescription = "چیدمان فهرستی",
                                                    tint = if (isList) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                        val isGrid = ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = if (isGrid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                        ) {
                                            IconButton(
                                                onClick = { if (!isGrid) vm.toggleViewMode() },
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Icon(
                                                    Icons.Rounded.GridView,
                                                    contentDescription = "چیدمان شبکه‌ای",
                                                    tint = if (isGrid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                    }
                                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.Bottom,
                                    ) {
                                        listOf(
                                            ir.solina.files.core.model.UiDensity.COMPACT to "فشرده",
                                            ir.solina.files.core.model.UiDensity.COMFORTABLE to "معمولی",
                                            ir.solina.files.core.model.UiDensity.LARGE to "بزرگ",
                                        ).forEach { (densityOption, label) ->
                                            val selectedDensity = ui.settings.uiDensity == densityOption
                                            Column(
                                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                            ) {
                                                Text(
                                                    label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (selectedDensity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Box(
                                                    Modifier
                                                        .size(if (selectedDensity) 28.dp else 24.dp)
                                                        .clip(CircleShape)
                                                        .background(if (selectedDensity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                                        .clickable { vm.setItemDensity(densityOption) },
                                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                                ) {
                                                    if (selectedDensity) {
                                                        Icon(Icons.Rounded.Check, contentDescription = "انتخاب شده: $label", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        FloatingActionButton(
                            onClick = { layoutMenuExpanded = !layoutMenuExpanded; quickCreateExpanded = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ) {
                            Icon(
                                imageVector = if (ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) Icons.Rounded.GridView else Icons.Rounded.ViewList,
                                contentDescription = "تنظیم چیدمان و اندازه",
                                modifier = Modifier.graphicsLayer { scaleX = layoutIconScale; scaleY = layoutIconScale },
                            )
                        }
                    }
                }
            }
            if (floatingZones.getOrNull(0).orEmpty().contains(FloatingAction.PANE_MODE)) {
                FloatingActionButton(
                    onClick = { vm.setDualPanePreferred(!dual); quickCreateExpanded = false; layoutMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = if (dual) Icons.Rounded.ViewAgenda else Icons.Rounded.ViewColumn,
                        contentDescription = if (dual) "Switch to single pane" else "Switch to dual pane",
                        modifier = Modifier.graphicsLayer { rotationZ = paneIconRotation },
                    )
                }
            }
            val floatingMenuActions = floatingZones.getOrNull(1).orEmpty()
            if (floatingMenuActions.isNotEmpty()) {
                Box {
                    FloatingActionButton(onClick = { floatingMoreExpanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "گزینه‌های نوار شناور")
                    }
                    DropdownMenu(expanded = floatingMoreExpanded, onDismissRequest = { floatingMoreExpanded = false }) {
                        floatingMenuActions.forEach { action ->
                            val title = when (action) {
                                FloatingAction.CREATE -> "ایجاد فایل یا پوشه"
                                FloatingAction.PANE_MODE -> if (dual) "تک‌پنجره" else "دوپنجره"
                                FloatingAction.ORIENTATION -> if (horizontalSplit) "پنل‌ها کنار هم" else "پنل‌ها روی هم"
                                FloatingAction.VIEW_LAYOUT -> "تنظیم چیدمان و اندازه"
                            }
                            val actionIcon = when (action) {
                                FloatingAction.CREATE -> Icons.Rounded.Add
                                FloatingAction.PANE_MODE -> if (dual) Icons.Rounded.ViewAgenda else Icons.Rounded.ViewColumn
                                FloatingAction.ORIENTATION -> if (horizontalSplit) Icons.Rounded.SwapVert else Icons.Rounded.SwapHoriz
                                FloatingAction.VIEW_LAYOUT -> if (ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) Icons.Rounded.GridView else Icons.Rounded.ViewList
                            }
                            DropdownMenuItem(
                                text = { Text(title) },
                                leadingIcon = { Icon(actionIcon, contentDescription = null) },
                                onClick = {
                                    floatingMoreExpanded = false
                                    when (action) {
                                        FloatingAction.CREATE -> quickCreateExpanded = !quickCreateExpanded
                                        FloatingAction.PANE_MODE -> { vm.setDualPanePreferred(!dual); quickCreateExpanded = false; layoutMenuExpanded = false }
                                        FloatingAction.ORIENTATION -> if (dual) horizontalSplit = !horizontalSplit else layoutMenuExpanded = true
                                        FloatingAction.VIEW_LAYOUT -> layoutMenuExpanded = true
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    DragLayer(drag) { host[0]?.positionInRoot() ?: Offset.Zero }
    }
    }

    when (val d = dialog) {
        Dialog.None -> Unit
        is Dialog.NewFolder -> InputDialog(
            title = stringResource(R.string.common_new_folder), label = stringResource(R.string.explorer_folder_name),
            onConfirm = { vm.newFolder(d.pane, it); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.NewFile -> InputDialog(
            title = stringResource(R.string.common_new_file), label = stringResource(R.string.explorer_file_name), initial = d.initial,
            onConfirm = { vm.newFile(d.pane, it); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        Dialog.Bookmarks -> BookmarksSheet(
            bookmarks = ui.bookmarks,
            actions = BookmarkActions(
                onOpen = { vm.openBookmark(ui.active, it); dialog = Dialog.None },
                onAddCurrent = { vm.addBookmarks(listOf(ui.activePane.path)) },
                onUpdate = { path, edited -> vm.updateBookmark(path, edited) },
                onMove = { path, offset -> vm.moveBookmark(path, offset) },
                onDelete = { vm.removeBookmark(it) },
                onRenameGroup = { from, to -> vm.renameBookmarkGroup(from, to) },
                onDeleteGroup = { vm.deleteBookmarkGroup(it) },
            ),
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.CreateArchive -> CreateArchiveDialog(
            items = d.items,
            defaultName = archiveName(d.items, ui.pane(d.pane).path),
            onConfirm = { name, options, independent ->
                vm.createArchive(d.pane, d.items, name, options, independent)
                dialog = Dialog.None
            },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.BatchRename -> BatchRenameDialog(
            items = d.items,
            history = ui.renameHistory,
            preview = { rule, order, descending -> vm.previewRename(d.pane, d.items, rule, order, descending) },
            onApply = { plan, rule -> vm.applyRename(d.pane, plan, rule); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.CreateLink -> CreateLinkDialog(
            items = d.items,
            defaultParent = linkParent(d.pane),
            onConfirm = { parent, requests -> vm.createLinks(d.pane, parent, requests); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.Split -> SplitDialog(
            item = d.item,
            onConfirm = { method, pattern -> vm.splitFile(d.pane, d.item, method, pattern); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.Merge -> MergeDialog(
            parts = d.items,
            existingNames = ui.pane(d.pane).items.mapTo(HashSet()) { it.name },
            onConfirm = { ordered, output, pdf -> vm.mergeFiles(d.pane, ordered, output, pdf); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.Encrypt -> EncryptDialog(
            item = d.item,
            onConfirm = { pw -> vm.encryptFile(d.pane, d.item, pw); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.Decrypt -> PasswordDialog(
            title = stringResource(R.string.explorer_decrypt_file),
            fileName = d.item.name,
            wrong = false,
            onConfirm = { pw -> vm.decryptFile(d.pane, d.item, pw); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.SignApk -> SignApkDialog(
            item = d.item,
            onConfirm = { request -> vm.signApk(d.item, request); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.JumpTo -> InputDialog(
            title = stringResource(R.string.explorer_jump_to_path), label = stringResource(R.string.common_path), initial = ui.pane(d.pane).path,
            confirmText = stringResource(R.string.explorer_go), select = InputSelection.ALL, ltr = true,
            onConfirm = { vm.openPath(d.pane, it.trim()); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.Rename -> InputDialog(
            title = stringResource(R.string.common_rename), label = stringResource(R.string.explorer_new_name), initial = d.item.name,
            select = if (d.item.isDirectory) InputSelection.ALL else InputSelection.BASE_NAME,
            onConfirm = { vm.rename(d.pane, d.item, it); vm.clearSelection(d.pane); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.Transfer -> Unit
        Dialog.Delete -> DeleteDialog(
            count = ui.activePane.selected.size,
            recycleByDefault = ui.settings.trashByDefault,
            allowRecycle = remember(ui.active, ui.activePane.path, ui.activePane.viaRoot) { vm.canRecycle(ui.active) },
            onConfirm = { toBin -> vm.deleteSelection(ui.active, toBin); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        Dialog.Sort -> {
            val folder = ui.activePane.path
            var own by remember(folder) { mutableStateOf(ui.settings.folderSorts.containsKey(FolderSorts.key(folder))) }
            var order by remember(folder) { mutableStateOf(ui.settings.sortFor(folder)) }
            SortSheet(
                current = order,
                showHidden = ui.settings.showHidden,
                onChange = { o ->
                    order = o
                    if (own) vm.setFolderSort(folder, o) else vm.setSort(o)
                },
                onToggleHidden = vm::setShowHidden,
                onDismiss = { dialog = Dialog.None },
                thisFolderOnly = own,
                folderName = ui.displayPath(folder).substringAfterLast('/').ifBlank { ui.displayPath(folder) },
                onThisFolderOnly = { on ->
                    own = on
                    vm.setFolderSort(folder, if (on) order else null)
                    if (!on) order = ui.settings.sortOrder
                },
            )
        }
        is Dialog.ItemSheet -> ItemActionSheet(
            item = d.item,
            smaliFolder = produceState(false, d.item.path) { value = d.item.isDirectory && vm.hasSmali(d.pane, d.item) }.value,
            actions = ItemActions(
                onSelect = { vm.setActive(d.pane); vm.toggleSelect(d.pane, d.item); dialog = Dialog.None },
                onOpen = {
                    dialog = Dialog.None
                    handleOpen(d.pane, d.item)
                },
                onOpenWith = { dialog = Dialog.None; withLocal(d.item) { onOpenWith(it) } },
                onOpenAsText = { dialog = Dialog.None; withLocal(d.item) { onOpenWithOpener(it, Opener.TEXT_EDITOR) } },
                onHex = { dialog = Dialog.None; withLocal(d.item) { onOpenWithOpener(it, Opener.HEX_EDITOR) } },
                onRename = { dialog = Dialog.Rename(d.pane, d.item) },
                onShare = { dialog = Dialog.None; withLocal(d.item) { IntentUtils.share(context, listOf(it)) } },
                onChecksum = { dialog = Dialog.None; withLocal(d.item) { dialog = Dialog.Checksum(it) } },
                onExtract = { localOnly(d.item) { vm.extractArchive(d.pane, d.item); dialog = Dialog.None } },
                onApkInfo = { dialog = Dialog.None; withLocal(d.item) { onOpenWithOpener(it, Opener.APK_VIEWER) } },
                onView = { dialog = Dialog.None; withLocal(d.item) { onOpenWithOpener(it, Opener.ARCHIVE_VIEWER) } },
                onSignApk = { localOnly(d.item) { dialog = Dialog.SignApk(d.item) } },
                onCompress = { localOnly(d.item) { dialog = Dialog.CreateArchive(d.pane, listOf(d.item)) } },
                onAddBookmark = { vm.addBookmarks(listOf(d.item.path)); dialog = Dialog.None },
                onCreateLink = { localOnly(d.item) { dialog = Dialog.CreateLink(d.pane, listOf(d.item)) } },
                onSplit = { localOnly(d.item) { dialog = Dialog.Split(d.pane, d.item) } },
                onEncrypt = { localOnly(d.item) { dialog = Dialog.Encrypt(d.pane, d.item) } },
                onDecrypt = { localOnly(d.item) { dialog = Dialog.Decrypt(d.pane, d.item) } },
                onProperties = { localOnly(d.item) { dialog = Dialog.Properties(listOf(d.item)) } },
                onDelete = {
                    vm.setActive(d.pane)
                    vm.clearSelection(d.pane)
                    vm.toggleSelect(d.pane, d.item, sound = false)
                    dialog = Dialog.Delete
                },
                onSmaliToDex = { localOnly(d.item) { dialog = Dialog.None; vm.smaliToDex(d.pane, d.item) } },
                onJarToDex = { localOnly(d.item) { dialog = Dialog.None; vm.jarToDex(d.pane, d.item) } },
                onMoveToVault = { localOnly(d.item) { dialog = Dialog.None; vm.moveToVault(d.pane, listOf(d.item)) } },
                onRestore = { dialog = Dialog.None; vm.restoreFromVault(d.pane, listOf(d.item)) },
            ),
            onDismiss = { dialog = Dialog.None },
            layout = ui.settings.actions.sheet,
            onCustomize = { dialog = Dialog.None; onCustomize(ToolbarTab.SHEET) },
        )
        is Dialog.Checksum -> ChecksumDialog(item = d.item, onDismiss = { dialog = Dialog.None })
        is Dialog.Properties -> PropertiesSheet(
            items = d.items,
            rootEnabled = ui.settings.rootAccess,
            onDismiss = { dialog = Dialog.None },
            onChanged = { vm.refreshAll() },
        )
        is Dialog.NewProject -> NewProjectSheet(
            parentDir = d.dir,
            session = termux,
            onCreated = { path ->
                dialog = Dialog.None
                vm.revealCreated(d.dir, path)
            },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.RunProject -> ProjectRunSheet(d.project, onDismiss = { dialog = Dialog.None }, session = termux)
    }

    if (ui.remoteEdits.isNotEmpty() && dialog == Dialog.None) {
        val edits = ui.remoteEdits
        AlertDialog(
            onDismissRequest = vm::keepRemoteEdits,
            title = { Text(stringResource(R.string.explorer_upload_title)) },
            text = {
                Text(
                    if (edits.size == 1) {
                        stringResource(
                            R.string.explorer_upload_one,
                            RemoteUri.name(edits[0].uri),
                            ui.displayPath(RemoteUri.parent(edits[0].uri) ?: edits[0].uri),
                        )
                    } else {
                        pluralStringResource(R.plurals.explorer_upload_many, edits.size, edits.size)
                    },
                )
            },
            confirmButton = { TextButton(onClick = vm::uploadRemoteEdits) { Text(stringResource(R.string.explorer_upload)) } },
            dismissButton = { TextButton(onClick = vm::keepRemoteEdits) { Text(stringResource(R.string.explorer_not_now)) } },
        )
    }
    TerminalGate(termux, appSnackbar ?: snackbar)
    when (val prompt = ui.storagePrompt) {
        null -> Unit
        is StoragePrompt.Password -> StoragePasswordDialog(prompt.connection, onConfirm = vm::unlockStorage, onDismiss = vm::dismissStoragePrompt)
        is StoragePrompt.HostKey -> HostKeyChangedDialog(
            prompt.connection, old = prompt.connection.hostKey, presented = prompt.presented,
            onTrust = vm::trustStorageKey, onDismiss = vm::dismissStoragePrompt,
        )
        is StoragePrompt.Vault -> if (prompt.setup) {
            VaultSetupDialog(onDismiss = vm::dismissStoragePrompt, onReady = vm::vaultReady)
        } else {
            VaultUnlockDialog(onDismiss = vm::dismissStoragePrompt, onUnlocked = vm::vaultReady)
        }
    }

    ui.transferPrompt?.let { prompt ->
        TransferDialog(
            move = prompt.move,
            count = prompt.count,
            destination = prompt.destination,
            conflicts = prompt.conflicts,
            onConfirm = { conflict -> vm.confirmTransfer(conflict) },
            onDismiss = { vm.cancelTransfer() },
        )
    }

    ui.passwordPrompt?.let { p ->
        PasswordDialog(
            title = stringResource(if (p.purpose == PasswordPurpose.EXTRACT) R.string.explorer_encrypted_archive else R.string.explorer_decrypt_file),
            fileName = p.item.name,
            wrong = p.wrong,
            onConfirm = vm::submitPassword,
            onDismiss = vm::dismissPasswordPrompt,
        )
    }

    ui.task?.let { TaskOverlay(it) }
    ui.build?.let { ir.solina.files.feature.build.BuildDialog(it, onClose = vm::closeBuild) }
    if ((ui.task != null || ui.build?.finished == false) && ui.settings.behavior.keepScreenOnTasks) {
        val view = LocalView.current
        DisposableEffect(view) {
            view.keepScreenOn = true
            onDispose { view.keepScreenOn = false }
        }
    }
}

@Composable
private fun ExplorerTitle(pane: PaneState, visible: List<FileItem>) {
    Column {
        Text("Solina Files", maxLines = 1)
        Text(
            countsLine(visible),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun countsLine(items: List<FileItem>): String {
    val folders = items.count { it.isDirectory }
    val files = items.size - folders
    return pluralStringResource(R.plurals.common_folders, folders, folders) + separator() + pluralStringResource(R.plurals.common_files, files, files)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBar(query: String, onQuery: (String) -> Unit, onClose: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.common_close)) }
        },
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                placeholder = { Text(stringResource(R.string.explorer_filter_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}



