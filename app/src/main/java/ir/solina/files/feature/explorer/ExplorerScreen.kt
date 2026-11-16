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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Layers
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
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.res.painterResource
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
import ir.solina.files.core.vault.Vault
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
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
    // Perf item 1: collect narrow slices of ExplorerUiState instead of the
    // whole state, so a query keystroke, a selection change or a task tick
    // only recomposes the readers of that slice. Slices are derived with
    // map + distinctUntilChanged (data-class slices compare by value).
    val active by remember(vm) {
        vm.state.map { it.active }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.active)
    val settings by remember(vm) {
        vm.state.map { it.settings }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.settings)
    val leftPane by remember(vm) {
        vm.state.map { it.left }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.left)
    val rightPane by remember(vm) {
        vm.state.map { it.right }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.right)
    val clipboard by remember(vm) {
        vm.state.map { it.clipboard }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.clipboard)
    // Task progress ticks arrive per file copied; sample them to at most one
    // UI update per 100 ms (the overlay only shows title/detail/fraction).
    // Throttling lives in this collector only — the ViewModel is untouched.
    val task by remember(vm) {
        vm.state.map { it.task }.sample(100).distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.task)
    val build by remember(vm) {
        vm.state.map { it.build }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.build)
    val query by remember(vm) {
        vm.state.map { it.query }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.query)
    val searchActive by remember(vm) {
        vm.state.map { it.searchActive }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.searchActive)
    val message by remember(vm) {
        vm.state.map { it.message }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.message)
    val bookmarks by remember(vm) {
        vm.state.map { it.bookmarks }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.bookmarks)
    val renameHistory by remember(vm) {
        vm.state.map { it.renameHistory }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.renameHistory)
    val passwordPrompt by remember(vm) {
        vm.state.map { it.passwordPrompt }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.passwordPrompt)
    val transferPrompt by remember(vm) {
        vm.state.map { it.transferPrompt }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.transferPrompt)
    val storages by remember(vm) {
        vm.state.map { it.storages }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.storages)
    val remoteEdits by remember(vm) {
        vm.state.map { it.remoteEdits }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.remoteEdits)
    val storagePrompt by remember(vm) {
        vm.state.map { it.storagePrompt }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.storagePrompt)
    val undo by remember(vm) {
        vm.state.map { it.undo }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.undo)
    val incoming by remember(vm) {
        vm.state.map { it.incoming }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(vm.state.value.incoming)
    fun paneState(p: Pane) = if (p == Pane.LEFT) leftPane else rightPane
    val activePane = paneState(active)
    fun isRemotePane(p: Pane) = RemoteUri.isRemote(paneState(p).path)
    fun isVaultPane(p: Pane) = Vault.isVault(paneState(p).path)
    // Same logic as ExplorerUiState.displayPath, but over the slices above.
    fun displayPath(path: String): String = when {
        Vault.isVault(path) -> RemoteUri.display(path, tr(R.string.model_protocol_vault))
        RemoteUri.isRemote(path) -> RemoteUri.display(path, storages.firstOrNull { it.id == RemoteUri.id(path) }?.let { it.name.ifBlank { it.host } })
        else -> path
    }
    // Perf item 2: the title used to recompute visibleItems (filter + sort)
    // on every recomposition. Compute it once per (items, sort, hidden,
    // query) and share it with the title; PaneContent keeps its own
    // remembered copy per pane.
    val activeVisible = remember(activePane.items, settings.sortFor(activePane.path), settings.showHidden, query) {
        visibleItems(activePane, settings, query)
    }
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<Dialog>(Dialog.None) }
    var overflow by remember { mutableStateOf(false) }
    var lastBackPress by remember { mutableLongStateOf(0L) }
    val rootPath = vm.rootPath
    val dual = settings.dualPanePreferred
    var horizontalSplit by rememberSaveable { mutableStateOf(false) }
    var quickCreateExpanded by rememberSaveable { mutableStateOf(false) }
    var layoutMenuExpanded by remember { mutableStateOf(false) }
    val createIconRotation by animateFloatAsState(if (quickCreateExpanded) 45f else 0f, label = "createFabRotation")
    val paneIconRotation by animateFloatAsState(if (dual) 180f else 0f, label = "paneFabRotation")
    val termux = viewModel<TermuxViewModel>().session
    val sshHere = RemoteUri.parse(activePane.path)?.let { (id, dir) ->
        storages.firstOrNull { it.id == id && it.protocol == RemoteProtocol.SFTP }?.let { it to dir }
    }
    val activePath = activePane.path
    val project by produceState<Project?>(null, activePath, activePane.items) {
        value = if (RemoteUri.isRemote(activePath)) null else withContext(Dispatchers.IO) { TermuxProjects.detect(java.io.File(activePath)) }
    }

    LaunchedEffect(message) {
        message?.let {
            vm.consumeMessage()
            (appSnackbar ?: snackbar).showSnackbar(it)
        }
    }

    BackHandler(enabled = !drawerOpen) {
        val pane = activePane
        when {
            pane.hasSelection -> vm.clearSelection(active)
            searchActive -> vm.setSearchActive(false)
            pane.path != rootPath && pane.path != "/" -> vm.navigateUp(active)
            !settings.behavior.doubleBackToExit -> (context as? Activity)?.finish()
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

    fun visibleOfActive() = visibleItems(activePane, settings, query).map { it.path }

    val selection = activePane.items.filter { it.path in activePane.selected }
    val single = selection.singleOrNull()
    val smaliFolder by produceState(false, single?.path, active) {
        value = single != null && single.isDirectory && vm.hasSmali(active, single)
    }
    val jarFile = single != null && !single.isDirectory && single.extension.equals("jar", ignoreCase = true)

    fun selectionInOrder() = visibleItems(activePane, settings, query).filter { it.path in activePane.selected }

    fun archiveName(items: List<FileItem>, folder: String): String =
        items.singleOrNull()?.let { ArchiveFormat.ZIP.defaultName(it).removeSuffix(ArchiveFormat.ZIP.extension) }
            ?: java.io.File(folder).name.ifEmpty { "archive" }

    fun linkParent(pane: Pane): String =
        if (dual && paneState(pane.other).path != paneState(pane).path) paneState(pane.other).path else paneState(pane).path

    // Perf item 7: the bottom-bar callbacks are created once and remembered,
    // keyed on exactly the values their behaviour depends on, so unrelated
    // recompositions (query keystrokes, folder-stats ticks, task progress)
    // reuse the same BottomBarActions instance and ExplorerBottomBar can skip.
    // ViewModel callbacks are bound method references, created once here.
    val bottomBarActions = remember(active, activePane.path, selection, single, project, dual, query, settings, onCustomize, onOpenSearch) {
        BottomBarActions(
            onBack = { vm.goBack(active) },
            onForward = { vm.goForward(active) },
            onUp = { vm.navigateUp(active) },
            onHome = { vm.goHome(active) },
            onSwap = vm::swapPanes,
            onNewFolder = { dialog = Dialog.NewFolder(active) },
            onNewFile = { dialog = Dialog.NewFile(active) },
            onRefresh = { vm.refresh(active) },
            onPaste = { vm.paste(active) },
            onCancelClipboard = vm::clearClipboard,
            onSelectAll = { vm.selectAll(active, visibleOfActive()) },
            onClearSelection = { vm.clearSelection(active) },
            onCopyToOther = { vm.beginTransfer(move = false) },
            onMoveToOther = { vm.beginTransfer(move = true) },
            onCopyToClipboard = { vm.copySelectionToClipboard(active, move = false) },
            onCutToClipboard = { vm.copySelectionToClipboard(active, move = true) },
            onDelete = { dialog = Dialog.Delete },
            onRename = { selection.firstOrNull()?.let { dialog = Dialog.Rename(active, it) } },
            onCompress = { dialog = Dialog.CreateArchive(active, selectionInOrder()) },
            onShare = { IntentUtils.share(context, selection) },
            onProperties = { dialog = Dialog.Properties(selection) },
            onInvert = { vm.invertSelection(active, visibleOfActive()) },
            onBookmarks = { dialog = Dialog.Bookmarks },
            onBookmarkFolder = { vm.addBookmarks(listOf(activePane.path)) },
            onBookmarkSelection = { vm.addSelectionToBookmarks(active) },
            onBatchRename = { dialog = Dialog.BatchRename(active, selectionInOrder()) },
            onCreateLinks = { dialog = Dialog.CreateLink(active, selectionInOrder()) },
            onMerge = { dialog = Dialog.Merge(active, selectionInOrder().filterNot { it.isDirectory }) },
            onCompare = {
                val two = selectionInOrder().filterNot { it.isDirectory }
                if (two.size == 2) onCompare(two[0], two[1])
            },
            onNewProject = { dialog = Dialog.NewProject(activePane.path) },
            onOpenTerminal = { termux.openTerminal(activePane.path) },
            onRunProject = { project?.let { dialog = Dialog.RunProject(it) } },
            onSmaliToDex = { single?.let { vm.smaliToDex(active, it) } },
            onJarToDex = { single?.let { vm.jarToDex(active, it) } },
            onFilter = { vm.setSearchActive(true) },
            onSearch = {
                if (RemoteUri.isRemote(activePane.path)) {
                    scope.launch { snackbar.showSnackbar(tr(R.string.explorer_search_local_only)) }
                } else {
                    onOpenSearch(activePane.path)
                }
            },
            onSort = { dialog = Dialog.Sort },
            onJump = { dialog = Dialog.JumpTo(active) },
            onToggleHidden = { vm.setShowHidden(!settings.showHidden) },
            onCustomize = onCustomize,
            onMoveToVault = { vm.moveSelectionToVault(active) },
            onRestore = { vm.restoreFromVault(active) },
        )
    }
    val onToggleOrientation = remember { { horizontalSplit = !horizontalSplit } }
    val onToggleViewLayout: () -> Unit = remember(vm) { { vm.toggleViewMode() } }
    val onTogglePaneMode = remember(dual, settings.list.viewMode) {
        {
            if (!dual && settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) vm.toggleViewMode()
            vm.setDualPanePreferred(!dual)
            quickCreateExpanded = false
            layoutMenuExpanded = false
        }
    }
    val onSwapPane = remember(vm) { vm::swapPanes }

    val drag = remember { FileDragState() }
    drag.haptics = LocalHapticFeedback.current
    drag.onDrop = { session, spot -> vm.dropInto(session.pane, session.items, spot.dir) }
    drag.onSpring = { spot -> Pane.entries.getOrNull(spot.pane)?.let { p -> vm.setActive(p); vm.openPath(p, spot.dir) } }
    val host = remember { arrayOfNulls<LayoutCoordinates>(1) }

    LaunchedEffect(undo?.seq) {
        val u = undo ?: return@LaunchedEffect
        val result = (appSnackbar ?: snackbar).showSnackbar(u.message, actionLabel = tr(R.string.explorer_undo), duration = SnackbarDuration.Long)
        if (result == SnackbarResult.ActionPerformed) vm.undoMove(u) else vm.consumeUndo(u.seq)
    }

    LaunchedEffect(incoming?.seq) {
        val c = incoming ?: return@LaunchedEffect
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
            if (searchActive) {
                FilterBar(query = query, onQuery = vm::setQuery, onClose = { vm.setSearchActive(false) })
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
                    title = { ExplorerTitle(activePane, activeVisible) },
                    actions = {
                        IconButton(onClick = { vm.setSearchActive(true) }) { Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.explorer_filter)) }
                        IconButton(onClick = {
                            if (RemoteUri.isRemote(activePane.path)) {
                                scope.launch { snackbar.showSnackbar(tr(R.string.explorer_search_local_only)) }
                            } else {
                                onOpenSearch(activePane.path)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ManageSearch, contentDescription = stringResource(R.string.explorer_advanced_search))
                        }
                        IconButton(onClick = { dialog = Dialog.Sort }) { Icon(Icons.Rounded.SortByAlpha, contentDescription = stringResource(R.string.common_sort)) }
                        Box {
                            IconButton(onClick = { overflow = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.explorer_options)) }
                            DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.explorer_refresh_both)) }, onClick = { overflow = false; vm.refreshAll() })
                                DropdownMenuItem(text = { Text(stringResource(R.string.explorer_jump_to_path_more)) }, onClick = { overflow = false; dialog = Dialog.JumpTo(active) })
                                DropdownMenuItem(
                                    text = { Text(stringResource(if (settings.showHidden) R.string.common_hide_hidden else R.string.common_show_hidden)) },
                                    onClick = { overflow = false; vm.setShowHidden(!settings.showHidden) },
                                )
                                val mount = activePane.mount
                                if (settings.rootAccess && mount != null && mount.point in Mounts.SYSTEM_POINTS) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(if (mount.readOnly) R.string.explorer_mount_rw else R.string.explorer_mount_ro, mount.point)) },
                                        onClick = { overflow = false; vm.remount(active, writable = mount.readOnly) },
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
                selectionCount = activePane.selected.size,
                canBack = activePane.history.canBack,
                canForward = activePane.history.canForward,
                activeIsLeft = active == Pane.LEFT,
                dual = dual,
                clipboardCount = clipboard?.items?.size ?: 0,
                clipboardIsMove = clipboard?.move == true,
                selectedFiles = selection.count { !it.isDirectory },
                remote = isRemotePane(active),
                projectLabel = project?.kind?.label,
                smaliFolder = smaliFolder,
                jarFile = jarFile,
                vault = isVaultPane(active),
                browse = settings.actions.browse,
                selection = settings.actions.selection,
                showHidden = settings.showHidden,
                horizontalSplit = horizontalSplit,
                onToggleOrientation = onToggleOrientation,
                gridView = settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID,
                onToggleViewLayout = onToggleViewLayout,
                onTogglePaneMode = onTogglePaneMode,
                onSwapPane = onSwapPane,
                actions = bottomBarActions,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
        PaneModeBar(
            dual = dual,
            onSelectDual = {
                if (!dual) {
                    if (settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) vm.toggleViewMode()
                    vm.setDualPanePreferred(true)
                    quickCreateExpanded = false
                    layoutMenuExpanded = false
                }
            },
            onSelectSingle = {
                if (dual) {
                    vm.setDualPanePreferred(false)
                    quickCreateExpanded = false
                    layoutMenuExpanded = false
                }
            },
        )
        val panes = if (dual) listOf(Pane.LEFT, Pane.RIGHT) else listOf(active)
        @Composable
        fun PaneContent(pane: Pane, modifier: Modifier) {
            val state = paneState(pane)
            val visible = remember(state.items, settings, query) { visibleItems(state, settings, query) }
            // Perf item 7: remember the per-pane callbacks (keyed on the pane
            // and, for onOpen, on the parent callbacks) so PaneColumn can skip
            // when nothing it depends on changed.
            val onActivate = remember(pane) { { vm.setActive(pane) } }
            val onUp = remember(pane) { { vm.setActive(pane); vm.navigateUp(pane) } }
            val onOpen = remember(pane, onOpenFile, onOpenWithOpener, onOpenWith) { { item: FileItem -> handleOpen(pane, item) } }
            val onToggleSelect = remember(pane) { { item: FileItem -> vm.toggleSelect(pane, item) } }
            val onItemMenu = remember(pane) { { item: FileItem -> dialog = Dialog.ItemSheet(pane, item) } }
            val onJumpTo = remember(pane) { { dialog = Dialog.JumpTo(pane) } }
            val onRefresh = remember(pane) { { vm.setActive(pane); vm.refresh(pane) } }
            val onLayoutClickLambda = remember { { layoutMenuExpanded = !layoutMenuExpanded; quickCreateExpanded = false } }
            PaneColumn(
                state = state,
                visible = visible,
                isActive = active == pane,
                showActive = dual,
                rootPath = rootPath,
                onActivate = onActivate,
                onUp = onUp,
                onOpen = onOpen,
                onToggleSelect = onToggleSelect,
                onItemMenu = onItemMenu,
                onJumpTo = onJumpTo,
                onRefresh = onRefresh,
                modifier = modifier,
                displayPath = if (RemoteUri.isRemote(state.path)) displayPath(state.path) else null,
                pane = pane,
                horizontalItems = dual && horizontalSplit,
                singlePane = !dual,
                // Redesign v2: no grid/layout button in dual-pane.
                onLayoutClick = if (!dual) onLayoutClickLambda else null,
                isGrid = settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID,
            )
        }
        AnimatedContent(
            targetState = dual to horizontalSplit,
            transitionSpec = {
                (fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.92f))
                    .togetherWith(fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.96f))
            },
            label = "singleDualPaneTransition",
            modifier = Modifier.weight(1f),
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
                PaneContent(active, Modifier.fillMaxSize().padding(horizontal = 4.dp))
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
    }
    // Redesign v2: the layout menu opens from the path header's button.
    // The invisible scrim closes it on any tap or scroll outside it.
    if (layoutMenuExpanded && !searchActive) {
        Box(
            Modifier.fillMaxSize().dismissMenuOnOutsideGesture { layoutMenuExpanded = false },
        )
        // The panel sits below the path header, so the header's layout
        // button stays visible while the menu is open.
        Box(
            Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(top = 212.dp, end = 16.dp),
        ) {
            LayoutMenuPanel(
                dual = dual,
                viewMode = settings.list.viewMode,
                onToggleViewMode = { vm.toggleViewMode() },
                density = settings.uiDensity,
                onDensity = { vm.setItemDensity(it) },
                onClose = { layoutMenuExpanded = false },
            )
        }
    }
    if (!searchActive) {
        Column(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(end = 16.dp, bottom = 84.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val floatingZones = settings.actions.floating.zones
            // Only actions assigned to Quick are rendered as floating buttons; the other
            // stored zones are intentionally not surfaced in an overflow/menu.
            val floatingVisibleActions = floatingZones.getOrNull(0).orEmpty()
                .filterNot { !dual && (it == FloatingAction.ORIENTATION || it == FloatingAction.SWAP_PANES) }
                // Redesign v2: the layout toggle lives in the path header now,
                // not floating over the file list.
                .filterNot { it == FloatingAction.VIEW_LAYOUT }
                // Redesign v2: the pane switch lives in the new mode bar now,
                // not floating over the file list.
                .filterNot { it == FloatingAction.PANE_MODE }
            floatingVisibleActions.forEach { action ->
                when (action) {
                FloatingAction.HOME -> {
                FloatingActionButton(
                    onClick = { vm.goHome(active); quickCreateExpanded = false; layoutMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = "خانه")
                }
            
                }
                FloatingAction.REFRESH -> {
                FloatingActionButton(
                    onClick = { vm.refresh(active); quickCreateExpanded = false; layoutMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "تازه‌سازی")
                }
            
                }
                FloatingAction.BOOKMARKS -> {
                    FloatingActionButton(
                        onClick = { dialog = Dialog.Bookmarks; quickCreateExpanded = false; layoutMenuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Icon(Icons.Rounded.Bookmarks, contentDescription = "نشانک‌ها")
                    }
                }
                FloatingAction.SWAP_PANES -> {
                if (dual) {
                FloatingActionButton(
                    onClick = { vm.swapPanes() },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = "تعویض محتوای دو پنل")
                }
            
                }

                }
                FloatingAction.CREATE -> {
                    Column(
                        modifier = Modifier.width(240.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AnimatedVisibility(
                            visible = quickCreateExpanded,
                            enter = fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.94f),
                            exit = fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.96f),
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                ),
                                tonalElevation = 6.dp,
                                shadowElevation = 12.dp,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.explorer_quick_create_title),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontSize = MaterialTheme.typography.titleSmall.fontSize * 0.8f,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                    QuickCreateButton(
                                        icon = Icons.Rounded.CreateNewFolder,
                                        label = stringResource(R.string.explorer_quick_new_folder),
                                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFolder(active) },
                                    )
                                    QuickCreateButton(
                                        icon = Icons.Rounded.Description,
                                        label = stringResource(R.string.explorer_quick_new_text_file),
                                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFile(active, "untitled.txt") },
                                    )
                                    QuickCreateButton(
                                        icon = Icons.Rounded.Code,
                                        label = stringResource(R.string.explorer_quick_new_html_file),
                                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFile(active, "index.html") },
                                    )
                                    if (!isRemotePane(active)) {
                                        QuickCreateButton(
                                            icon = Icons.Rounded.Code,
                                            label = stringResource(R.string.explorer_quick_new_project),
                                            onClick = { quickCreateExpanded = false; dialog = Dialog.NewProject(activePane.path) },
                                        )
                                    }
                                }
                            }
                        }
                        if (FloatingAction.CREATE in floatingVisibleActions) {
                            FloatingActionButton(
                                onClick = {
                                    quickCreateExpanded = !quickCreateExpanded
                                    layoutMenuExpanded = false
                                },
                                containerColor = if (quickCreateExpanded) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                contentColor = if (quickCreateExpanded) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = if (quickCreateExpanded) stringResource(R.string.explorer_quick_create_close) else stringResource(R.string.explorer_quick_create_open),
                                    modifier = Modifier.graphicsLayer { rotationZ = createIconRotation },
                                )
                            }
                        }
                    }
                }
                FloatingAction.ORIENTATION -> {
                FloatingActionButton(
                    onClick = { horizontalSplit = !horizontalSplit },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(
                        imageVector = if (horizontalSplit) Icons.Rounded.ViewAgenda else Icons.Rounded.ViewColumn,
                        contentDescription = if (horizontalSplit) "چیدن پنل‌ها روی هم" else "چیدن پنل‌ها کنار هم",
                    )
                }
            
                }
                // Redesign v2: no floating button for the layout toggle any
                // more — it lives in the path header. Kept so the `when`
                // stays exhaustive over FloatingAction.
                FloatingAction.VIEW_LAYOUT -> Unit
                FloatingAction.PANE_MODE -> {
                FloatingActionButton(
                    onClick = {
                        if (!dual && settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) vm.toggleViewMode()
                        vm.setDualPanePreferred(!dual)
                        quickCreateExpanded = false
                        layoutMenuExpanded = false
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = if (dual) Icons.Rounded.ViewColumn else Icons.Rounded.ViewAgenda,
                        contentDescription = if (dual) "تبدیل به تک‌پنجره" else "تبدیل به دوپنجره",
                    )
                }
            
                }
                }
            }
        }
    }
    DragLayer(drag) { host[0]?.positionInRoot() ?: Offset.Zero }
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
            bookmarks = bookmarks,
            actions = BookmarkActions(
                onOpen = { vm.openBookmark(active, it); dialog = Dialog.None },
                onAddCurrent = { vm.addBookmarks(listOf(activePane.path)) },
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
            defaultName = archiveName(d.items, paneState(d.pane).path),
            onConfirm = { name, options, independent ->
                vm.createArchive(d.pane, d.items, name, options, independent)
                dialog = Dialog.None
            },
            onDismiss = { dialog = Dialog.None },
        )
        is Dialog.BatchRename -> BatchRenameDialog(
            items = d.items,
            history = renameHistory,
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
            existingNames = paneState(d.pane).items.mapTo(HashSet()) { it.name },
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
            title = stringResource(R.string.explorer_jump_to_path), label = stringResource(R.string.common_path), initial = paneState(d.pane).path,
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
            count = activePane.selected.size,
            recycleByDefault = settings.trashByDefault,
            allowRecycle = remember(active, activePane.path, activePane.viaRoot) { vm.canRecycle(active) },
            onConfirm = { toBin -> vm.deleteSelection(active, toBin); dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
        )
        Dialog.Sort -> {
            val folder = activePane.path
            var own by remember(folder) { mutableStateOf(settings.folderSorts.containsKey(FolderSorts.key(folder))) }
            var order by remember(folder) { mutableStateOf(settings.sortFor(folder)) }
            SortSheet(
                current = order,
                showHidden = settings.showHidden,
                onChange = { o ->
                    order = o
                    if (own) vm.setFolderSort(folder, o) else vm.setSort(o)
                },
                onToggleHidden = vm::setShowHidden,
                onDismiss = { dialog = Dialog.None },
                thisFolderOnly = own,
                folderName = displayPath(folder).substringAfterLast('/').ifBlank { displayPath(folder) },
                onThisFolderOnly = { on ->
                    own = on
                    vm.setFolderSort(folder, if (on) order else null)
                    if (!on) order = settings.sortOrder
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
            layout = settings.actions.sheet,
            onCustomize = { dialog = Dialog.None; onCustomize(ToolbarTab.SHEET) },
        )
        is Dialog.Checksum -> ChecksumDialog(item = d.item, onDismiss = { dialog = Dialog.None })
        is Dialog.Properties -> PropertiesSheet(
            items = d.items,
            rootEnabled = settings.rootAccess,
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

    if (remoteEdits.isNotEmpty() && dialog == Dialog.None) {
        val edits = remoteEdits
        AlertDialog(
            onDismissRequest = vm::keepRemoteEdits,
            title = { Text(stringResource(R.string.explorer_upload_title)) },
            text = {
                Text(
                    if (edits.size == 1) {
                        stringResource(
                            R.string.explorer_upload_one,
                            RemoteUri.name(edits[0].uri),
                            displayPath(RemoteUri.parent(edits[0].uri) ?: edits[0].uri),
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
    when (val prompt = storagePrompt) {
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

    transferPrompt?.let { prompt ->
        TransferDialog(
            move = prompt.move,
            count = prompt.count,
            destination = prompt.destination,
            conflicts = prompt.conflicts,
            onConfirm = { conflict -> vm.confirmTransfer(conflict) },
            onDismiss = { vm.cancelTransfer() },
        )
    }

    passwordPrompt?.let { p ->
        PasswordDialog(
            title = stringResource(if (p.purpose == PasswordPurpose.EXTRACT) R.string.explorer_encrypted_archive else R.string.explorer_decrypt_file),
            fileName = p.item.name,
            wrong = p.wrong,
            onConfirm = vm::submitPassword,
            onDismiss = vm::dismissPasswordPrompt,
        )
    }

    task?.let { TaskOverlay(it) }
    build?.let { ir.solina.files.feature.build.BuildDialog(it, onClose = vm::closeBuild) }
    if ((task != null || build?.finished == false) && settings.behavior.keepScreenOnTasks) {
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




// Redesign v2: compact quick-create row — 20% smaller font and 10%
// smaller icon than the old extended FAB, with tighter padding.
@Composable
private fun QuickCreateButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth().height(44.dp),
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.8f,
                ),
                maxLines = 1,
            )
        }
    }
}

// Redesign v2: fixed pane-mode switch bar above the path header, always
// visible in both modes. RTL row order puts dual-pane on the right and
// single-pane on the left, as requested.
@Composable
private fun PaneModeBar(
    dual: Boolean,
    onSelectDual: () -> Unit,
    onSelectSingle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PaneModeSegment(
                selected = dual,
                icon = painterResource(R.drawable.ic_pane_dual),
                label = stringResource(R.string.explorer_pane_dual),
                contentDescription = stringResource(R.string.explorer_pane_dual_desc),
                onClick = onSelectDual,
                modifier = Modifier.weight(1f),
            )
            PaneModeSegment(
                selected = !dual,
                icon = painterResource(R.drawable.ic_pane_single),
                label = stringResource(R.string.explorer_pane_single),
                contentDescription = stringResource(R.string.explorer_pane_single_desc),
                onClick = onSelectSingle,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PaneModeSegment(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        modifier = modifier.height(40.dp),
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

// Redesign v2: closes the layout menu on a tap or a scroll anywhere outside
// it, without swallowing the scroll itself — the gesture is only observed,
// never consumed, so the file list keeps scrolling underneath.
private fun Modifier.dismissMenuOnOutsideGesture(onDismiss: () -> Unit): Modifier = pointerInput(onDismiss) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val up = awaitTouchSlopOrCancellation(down.id) { _, _ -> onDismiss() }
        if (up != null) onDismiss()
    }
}

// Redesign v2: layout menu (list/grid + density). Its trigger moved from
// the floating button into the path header; the panel floats under the header.
@Composable
private fun LayoutMenuPanel(
    dual: Boolean,
    viewMode: ir.solina.files.core.model.ViewMode,
    onToggleViewMode: () -> Unit,
    density: ir.solina.files.core.model.UiDensity,
    onDensity: (ir.solina.files.core.model.UiDensity) -> Unit,
    onClose: () -> Unit,
) {
                            Surface(
                                modifier = Modifier.width(220.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                ),
                                tonalElevation = 6.dp,
                                shadowElevation = 12.dp,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    // Redesign v2: a close button of its own, so the
                                    // menu never traps the user.
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        IconButton(
                                            onClick = onClose,
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = stringResource(R.string.common_close),
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (!dual) {
                                            val isList = viewMode == ir.solina.files.core.model.ViewMode.LIST
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = if (isList) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                            ) {
                                                IconButton(
                                                    onClick = { if (!isList) onToggleViewMode() },
                                                    modifier = Modifier.size(44.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.ViewList,
                                                        contentDescription = stringResource(R.string.explorer_layout_list),
                                                        tint = if (isList) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    )
                                                }
                                            }
                                            val isGrid = viewMode == ir.solina.files.core.model.ViewMode.GRID
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = if (isGrid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                        ) {
                                            IconButton(
                                                onClick = { if (!isGrid) onToggleViewMode() },
                                                modifier = Modifier.size(44.dp),
                                            ) {
                                                Icon(
                                                    Icons.Rounded.GridView,
                                                    contentDescription = stringResource(R.string.explorer_layout_grid),
                                                    tint = if (isGrid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }

                                        }
                                    }
                                    androidx.compose.foundation.layout.Spacer(Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.Bottom,
                                    ) {
                                        listOf(
                                            ir.solina.files.core.model.UiDensity.COMPACT,
                                            ir.solina.files.core.model.UiDensity.COMFORTABLE,
                                            ir.solina.files.core.model.UiDensity.LARGE,
                                        ).forEach { densityOption ->
                                            val label = stringResource(densityOption.label)
                                            val selectedDensity = density == densityOption
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
                                                        .size(if (selectedDensity) 24.dp else 20.dp)
                                                        .clip(CircleShape)
                                                        .background(if (selectedDensity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                                        .clickable { onDensity(densityOption) },
                                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                                ) {
                                                    if (selectedDensity) {
                                                        Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.explorer_density_selected, label), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
}
