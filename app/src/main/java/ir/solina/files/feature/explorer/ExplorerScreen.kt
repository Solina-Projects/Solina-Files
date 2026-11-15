package ir.solina.files.feature.explorer

import ir.solina.files.core.i18n.separator
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewColumn
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
import ir.solina.files.core.archive.ArchiveFormat
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
import ir.solina.files.feature.explorer.components.BottomBarActions
import ir.solina.files.feature.explorer.components.ExplorerBottomBar
import ir.solina.files.feature.explorer.components.PaneColumn
import ir.solina.files.feature.explorer.components.TaskOverlay
import ir.solina.files.feature.settings.ToolbarTab
import ir.solina.files.feature.termux.TerminalGate
import ir.solina.files.feature.termux.TermuxViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed interface Dialog {
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

    // Derive and sort the active listing once per relevant state change. These values are
    // consumed by several toolbar, selection and navigation actions in the same composition.
    val activeVisibleItems = remember(ui.activePane, ui.settings, ui.query) {
        visibleItems(ui.activePane, ui.settings, ui.query)
    }
    val activeVisiblePaths = remember(activeVisibleItems) { activeVisibleItems.map { it.path } }

    fun visibleOfActive() = activeVisiblePaths

    val selection = remember(ui.activePane.items, ui.activePane.selected) {
        ui.activePane.items.filter { it.path in ui.activePane.selected }
    }
    val single = selection.singleOrNull()
    val smaliFolder by produceState(false, single?.path, ui.active) {
        value = single != null && single.isDirectory && vm.hasSmali(ui.active, single)
    }
    val jarFile = single != null && !single.isDirectory && single.extension.equals("jar", ignoreCase = true)

    fun selectionInOrder() = activeVisibleItems.filter { it.path in ui.activePane.selected }

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
                    title = { ExplorerTitle(ui.activePane, activeVisibleItems) },
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
                gridView = ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID,
                onToggleViewLayout = { vm.toggleViewMode() },
                onTogglePaneMode = {
                    if (!dual && ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) vm.toggleViewMode()
                    vm.setDualPanePreferred(!dual)
                    quickCreateExpanded = false
                    layoutMenuExpanded = false
                },
                onSwapPane = vm::swapPanes,
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
            val state by vm.paneState(pane).collectAsStateWithLifecycle()
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
            // Only actions assigned to Quick are rendered as floating buttons; the other
            // stored zones are intentionally not surfaced in an overflow/menu.
            val floatingVisibleActions = floatingZones.getOrNull(0).orEmpty()
                .filterNot { !dual && (it == FloatingAction.ORIENTATION || it == FloatingAction.SWAP_PANES) }
            floatingVisibleActions.forEach { action ->
                when (action) {
                FloatingAction.HOME -> {
                FloatingActionButton(
                    onClick = { vm.goHome(ui.active); quickCreateExpanded = false; layoutMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = "خانه")
                }
            
                }
                FloatingAction.REFRESH -> {
                FloatingActionButton(
                    onClick = { vm.refresh(ui.active); quickCreateExpanded = false; layoutMenuExpanded = false },
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
                                shape = RoundedCornerShape(24.dp),
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
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = "ایجاد مورد جدید",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                    ExtendedFloatingActionButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFolder(ui.active) },
                                        icon = { Icon(Icons.Rounded.CreateNewFolder, contentDescription = null) },
                                        text = { Text("پوشه جدید", maxLines = 1) },
                                        expanded = true,
                                    )
                                    ExtendedFloatingActionButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFile(ui.active, "untitled.txt") },
                                        icon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                                        text = { Text("فایل متنی", maxLines = 1) },
                                        expanded = true,
                                    )
                                    ExtendedFloatingActionButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { quickCreateExpanded = false; dialog = Dialog.NewFile(ui.active, "index.html") },
                                        icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                                        text = { Text("فایل HTML", maxLines = 1) },
                                        expanded = true,
                                    )
                                    if (!ui.isRemote(ui.active)) {
                                        ExtendedFloatingActionButton(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = { quickCreateExpanded = false; dialog = Dialog.NewProject(ui.activePane.path) },
                                            icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                                            text = { Text("پروژه جدید", maxLines = 1) },
                                            expanded = true,
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
                                    contentDescription = if (quickCreateExpanded) "بستن ایجاد سریع" else "ایجاد فایل یا پوشه",
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
                FloatingAction.VIEW_LAYOUT -> {
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
                                modifier = Modifier.width(240.dp),
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
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (!dual) {
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
                                    }
                                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
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
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                FloatingAction.PANE_MODE -> {
                FloatingActionButton(
                    onClick = {
                        if (!dual && ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) vm.toggleViewMode()
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

    ExplorerDialogs(
        dialog = dialog,
        onDialog = { dialog = it },
        vm = vm,
        ui = ui,
        termux = termux,
        handleOpen = ::handleOpen,
        withLocal = ::withLocal,
        localOnly = ::localOnly,
        archiveName = ::archiveName,
        linkParent = ::linkParent,
        onOpenWith = onOpenWith,
        onOpenWithOpener = onOpenWithOpener,
        onCustomize = onCustomize,
    )

    RemoteEditsPrompt(vm, ui, visible = dialog == Dialog.None)
    TerminalGate(termux, appSnackbar ?: snackbar)
    ExplorerPrompts(vm, ui)

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



