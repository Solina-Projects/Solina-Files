package ir.solina.files.feature.explorer

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.model.ViewMode
import ir.solina.files.core.remote.RemoteUri
import ir.solina.files.core.termux.Project
import ir.solina.files.core.termux.TermuxSession
import ir.solina.files.core.util.IntentUtils
import ir.solina.files.feature.explorer.components.BottomBarActions
import ir.solina.files.feature.explorer.components.ExplorerBottomBar
import ir.solina.files.feature.settings.ToolbarTab

/**
 * Bottom action bar of the explorer. Pulled out of [ExplorerScreen] so the screen only wires
 * state; behaviour is unchanged. Dialog requests go out through [onDialog].
 */
@Composable
internal fun ExplorerBottomBarHost(
    ui: ExplorerUiState,
    vm: ExplorerViewModel,
    dual: Boolean,
    selection: List<FileItem>,
    single: FileItem?,
    project: Project?,
    smaliFolder: Boolean,
    jarFile: Boolean,
    horizontalSplit: Boolean,
    termux: TermuxSession,
    visibleOfActive: () -> List<String>,
    selectionInOrder: () -> List<FileItem>,
    onToggleOrientation: () -> Unit,
    onResetMenus: () -> Unit,
    onDialog: (Dialog) -> Unit,
    onCompare: (FileItem, FileItem) -> Unit,
    onOpenSearch: (String) -> Unit,
    onSearchLocalOnly: () -> Unit,
    onCustomize: (ToolbarTab) -> Unit,
) {
    val context = LocalContext.current
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
    onToggleOrientation = onToggleOrientation,
    gridView = ui.settings.list.viewMode == ViewMode.GRID,
    onToggleViewLayout = { vm.toggleViewMode() },
    onTogglePaneMode = {
        if (!dual && ui.settings.list.viewMode == ViewMode.GRID) vm.toggleViewMode()
        vm.setDualPanePreferred(!dual)
        onResetMenus()
    },
    onSwapPane = vm::swapPanes,
    actions = BottomBarActions(
        onBack = { vm.goBack(ui.active) },
        onForward = { vm.goForward(ui.active) },
        onUp = { vm.navigateUp(ui.active) },
        onHome = { vm.goHome(ui.active) },
        onSwap = vm::swapPanes,
        onNewFolder = { onDialog(Dialog.NewFolder(ui.active)) },
        onNewFile = { onDialog(Dialog.NewFile(ui.active)) },
        onRefresh = { vm.refresh(ui.active) },
        onPaste = { vm.paste(ui.active) },
        onCancelClipboard = vm::clearClipboard,
        onSelectAll = { vm.selectAll(ui.active, visibleOfActive()) },
        onClearSelection = { vm.clearSelection(ui.active) },
        onCopyToOther = { vm.beginTransfer(move = false) },
        onMoveToOther = { vm.beginTransfer(move = true) },
        onCopyToClipboard = { vm.copySelectionToClipboard(ui.active, move = false) },
        onCutToClipboard = { vm.copySelectionToClipboard(ui.active, move = true) },
        onDelete = { onDialog(Dialog.Delete) },
        onRename = { selection.firstOrNull()?.let { onDialog(Dialog.Rename(ui.active, it)) } },
        onCompress = { onDialog(Dialog.CreateArchive(ui.active, selectionInOrder())) },
        onShare = { IntentUtils.share(context, selection) },
        onProperties = { onDialog(Dialog.Properties(selection)) },
        onInvert = { vm.invertSelection(ui.active, visibleOfActive()) },
        onBookmarks = { onDialog(Dialog.Bookmarks) },
        onBookmarkFolder = { vm.addBookmarks(listOf(ui.activePane.path)) },
        onBookmarkSelection = { vm.addSelectionToBookmarks(ui.active) },
        onBatchRename = { onDialog(Dialog.BatchRename(ui.active, selectionInOrder())) },
        onCreateLinks = { onDialog(Dialog.CreateLink(ui.active, selectionInOrder())) },
        onMerge = { onDialog(Dialog.Merge(ui.active, selectionInOrder().filterNot { it.isDirectory })) },
        onCompare = {
            val two = selectionInOrder().filterNot { it.isDirectory }
            if (two.size == 2) onCompare(two[0], two[1])
        },
        onNewProject = { onDialog(Dialog.NewProject(ui.activePane.path)) },
        onOpenTerminal = { termux.openTerminal(ui.activePane.path) },
        onRunProject = { project?.let { onDialog(Dialog.RunProject(it)) } },
        onSmaliToDex = { single?.let { vm.smaliToDex(ui.active, it) } },
        onJarToDex = { single?.let { vm.jarToDex(ui.active, it) } },
        onFilter = { vm.setSearchActive(true) },
        onSearch = {
            if (RemoteUri.isRemote(ui.activePane.path)) {
                onSearchLocalOnly()
            } else {
                onOpenSearch(ui.activePane.path)
            }
        },
        onSort = { onDialog(Dialog.Sort) },
        onJump = { onDialog(Dialog.JumpTo(ui.active)) },
        onToggleHidden = { vm.setShowHidden(!ui.settings.showHidden) },
        onCustomize = onCustomize,
        onMoveToVault = { vm.moveSelectionToVault(ui.active) },
        onRestore = { vm.restoreFromVault(ui.active) },
    ),
)
}
