package ir.solina.files.feature.explorer

import ir.solina.files.core.termux.TermuxSession
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ir.solina.files.R
import ir.solina.files.core.data.sortFor
import ir.solina.files.core.model.FolderSorts
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.open.Opener
import ir.solina.files.core.util.IntentUtils
import ir.solina.files.feature.apk.SignApkDialog
import ir.solina.files.feature.explorer.components.BatchRenameDialog
import ir.solina.files.feature.explorer.components.BookmarkActions
import ir.solina.files.feature.explorer.components.BookmarksSheet
import ir.solina.files.feature.explorer.components.ChecksumDialog
import ir.solina.files.feature.explorer.components.CreateArchiveDialog
import ir.solina.files.feature.explorer.components.CreateLinkDialog
import ir.solina.files.feature.explorer.components.DeleteDialog
import ir.solina.files.feature.explorer.components.EncryptDialog
import ir.solina.files.feature.explorer.components.InputDialog
import ir.solina.files.feature.explorer.components.InputSelection
import ir.solina.files.feature.explorer.components.ItemActionSheet
import ir.solina.files.feature.explorer.components.ItemActions
import ir.solina.files.feature.explorer.components.MergeDialog
import ir.solina.files.feature.explorer.components.PasswordDialog
import ir.solina.files.feature.explorer.components.SortSheet
import ir.solina.files.feature.explorer.components.SplitDialog
import ir.solina.files.feature.props.PropertiesSheet
import ir.solina.files.feature.settings.ToolbarTab
import ir.solina.files.feature.termux.NewProjectSheet
import ir.solina.files.feature.termux.ProjectRunSheet

/**
 * Hosts every dialog and sheet that [ExplorerScreen] can open, split out of the screen.
 * The screen owns the [dialog] state; this function only renders it and asks for changes through [onDialog].
 */
@Composable
internal fun ExplorerDialogs(
    dialog: Dialog,
    onDialog: (Dialog) -> Unit,
    vm: ExplorerViewModel,
    ui: ExplorerUiState,
    termux: TermuxSession,
    handleOpen: (Pane, FileItem) -> Unit,
    withLocal: (FileItem, (FileItem) -> Unit) -> Unit,
    localOnly: (FileItem, () -> Unit) -> Unit,
    archiveName: (List<FileItem>, String) -> String,
    linkParent: (Pane) -> String,
    onOpenWith: (FileItem) -> Unit,
    onOpenWithOpener: (FileItem, Opener) -> Unit,
    onCustomize: (ToolbarTab) -> Unit,
) {
    val context = LocalContext.current
    when (val d = dialog) {
        Dialog.None -> Unit
        is Dialog.NewFolder -> InputDialog(
            title = stringResource(R.string.common_new_folder), label = stringResource(R.string.explorer_folder_name),
            onConfirm = { vm.newFolder(d.pane, it); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.NewFile -> InputDialog(
            title = stringResource(R.string.common_new_file), label = stringResource(R.string.explorer_file_name), initial = d.initial,
            onConfirm = { vm.newFile(d.pane, it); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        Dialog.Bookmarks -> BookmarksSheet(
            bookmarks = ui.bookmarks,
            actions = BookmarkActions(
                onOpen = { vm.openBookmark(ui.active, it); onDialog(Dialog.None) },
                onAddCurrent = { vm.addBookmarks(listOf(ui.activePane.path)) },
                onUpdate = { path, edited -> vm.updateBookmark(path, edited) },
                onMove = { path, offset -> vm.moveBookmark(path, offset) },
                onDelete = { vm.removeBookmark(it) },
                onRenameGroup = { from, to -> vm.renameBookmarkGroup(from, to) },
                onDeleteGroup = { vm.deleteBookmarkGroup(it) },
            ),
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.CreateArchive -> CreateArchiveDialog(
            items = d.items,
            defaultName = archiveName(d.items, ui.pane(d.pane).path),
            onConfirm = { name, options, independent ->
                vm.createArchive(d.pane, d.items, name, options, independent)
                onDialog(Dialog.None)
            },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.BatchRename -> BatchRenameDialog(
            items = d.items,
            history = ui.renameHistory,
            preview = { rule, order, descending -> vm.previewRename(d.pane, d.items, rule, order, descending) },
            onApply = { plan, rule -> vm.applyRename(d.pane, plan, rule); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.CreateLink -> CreateLinkDialog(
            items = d.items,
            defaultParent = linkParent(d.pane),
            onConfirm = { parent, requests -> vm.createLinks(d.pane, parent, requests); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.Split -> SplitDialog(
            item = d.item,
            onConfirm = { method, pattern -> vm.splitFile(d.pane, d.item, method, pattern); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.Merge -> MergeDialog(
            parts = d.items,
            existingNames = ui.pane(d.pane).items.mapTo(HashSet()) { it.name },
            onConfirm = { ordered, output, pdf -> vm.mergeFiles(d.pane, ordered, output, pdf); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.Encrypt -> EncryptDialog(
            item = d.item,
            onConfirm = { pw -> vm.encryptFile(d.pane, d.item, pw); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.Decrypt -> PasswordDialog(
            title = stringResource(R.string.explorer_decrypt_file),
            fileName = d.item.name,
            wrong = false,
            onConfirm = { pw -> vm.decryptFile(d.pane, d.item, pw); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.SignApk -> SignApkDialog(
            item = d.item,
            onConfirm = { request -> vm.signApk(d.item, request); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.JumpTo -> InputDialog(
            title = stringResource(R.string.explorer_jump_to_path), label = stringResource(R.string.common_path), initial = ui.pane(d.pane).path,
            confirmText = stringResource(R.string.explorer_go), select = InputSelection.ALL, ltr = true,
            onConfirm = { vm.openPath(d.pane, it.trim()); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.Rename -> InputDialog(
            title = stringResource(R.string.common_rename), label = stringResource(R.string.explorer_new_name), initial = d.item.name,
            select = if (d.item.isDirectory) InputSelection.ALL else InputSelection.BASE_NAME,
            onConfirm = { vm.rename(d.pane, d.item, it); vm.clearSelection(d.pane); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.Transfer -> Unit
        Dialog.Delete -> DeleteDialog(
            count = ui.activePane.selected.size,
            recycleByDefault = ui.settings.trashByDefault,
            allowRecycle = remember(ui.active, ui.activePane.path, ui.activePane.viaRoot) { vm.canRecycle(ui.active) },
            onConfirm = { toBin -> vm.deleteSelection(ui.active, toBin); onDialog(Dialog.None) },
            onDismiss = { onDialog(Dialog.None) },
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
                onDismiss = { onDialog(Dialog.None) },
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
                onSelect = { vm.setActive(d.pane); vm.toggleSelect(d.pane, d.item); onDialog(Dialog.None) },
                onOpen = {
                    onDialog(Dialog.None)
                    handleOpen(d.pane, d.item)
                },
                onOpenWith = { onDialog(Dialog.None); withLocal(d.item) { onOpenWith(it) } },
                onOpenAsText = { onDialog(Dialog.None); withLocal(d.item) { onOpenWithOpener(it, Opener.TEXT_EDITOR) } },
                onHex = { onDialog(Dialog.None); withLocal(d.item) { onOpenWithOpener(it, Opener.HEX_EDITOR) } },
                onRename = { onDialog(Dialog.Rename(d.pane, d.item)) },
                onShare = { onDialog(Dialog.None); withLocal(d.item) { IntentUtils.share(context, listOf(it)) } },
                onChecksum = { onDialog(Dialog.None); withLocal(d.item) { onDialog(Dialog.Checksum(it)) } },
                onExtract = { localOnly(d.item) { vm.extractArchive(d.pane, d.item); onDialog(Dialog.None) } },
                onApkInfo = { onDialog(Dialog.None); withLocal(d.item) { onOpenWithOpener(it, Opener.APK_VIEWER) } },
                onView = { onDialog(Dialog.None); withLocal(d.item) { onOpenWithOpener(it, Opener.ARCHIVE_VIEWER) } },
                onSignApk = { localOnly(d.item) { onDialog(Dialog.SignApk(d.item)) } },
                onCompress = { localOnly(d.item) { onDialog(Dialog.CreateArchive(d.pane, listOf(d.item))) } },
                onAddBookmark = { vm.addBookmarks(listOf(d.item.path)); onDialog(Dialog.None) },
                onCreateLink = { localOnly(d.item) { onDialog(Dialog.CreateLink(d.pane, listOf(d.item))) } },
                onSplit = { localOnly(d.item) { onDialog(Dialog.Split(d.pane, d.item)) } },
                onEncrypt = { localOnly(d.item) { onDialog(Dialog.Encrypt(d.pane, d.item)) } },
                onDecrypt = { localOnly(d.item) { onDialog(Dialog.Decrypt(d.pane, d.item)) } },
                onProperties = { localOnly(d.item) { onDialog(Dialog.Properties(listOf(d.item))) } },
                onDelete = {
                    vm.setActive(d.pane)
                    vm.clearSelection(d.pane)
                    vm.toggleSelect(d.pane, d.item, sound = false)
                    onDialog(Dialog.Delete)
                },
                onSmaliToDex = { localOnly(d.item) { onDialog(Dialog.None); vm.smaliToDex(d.pane, d.item) } },
                onJarToDex = { localOnly(d.item) { onDialog(Dialog.None); vm.jarToDex(d.pane, d.item) } },
                onMoveToVault = { localOnly(d.item) { onDialog(Dialog.None); vm.moveToVault(d.pane, listOf(d.item)) } },
                onRestore = { onDialog(Dialog.None); vm.restoreFromVault(d.pane, listOf(d.item)) },
            ),
            onDismiss = { onDialog(Dialog.None) },
            layout = ui.settings.actions.sheet,
            onCustomize = { onDialog(Dialog.None); onCustomize(ToolbarTab.SHEET) },
        )
        is Dialog.Checksum -> ChecksumDialog(item = d.item, onDismiss = { onDialog(Dialog.None) })
        is Dialog.Properties -> PropertiesSheet(
            items = d.items,
            rootEnabled = ui.settings.rootAccess,
            onDismiss = { onDialog(Dialog.None) },
            onChanged = { vm.refreshAll() },
        )
        is Dialog.NewProject -> NewProjectSheet(
            parentDir = d.dir,
            session = termux,
            onCreated = { path ->
                onDialog(Dialog.None)
                vm.revealCreated(d.dir, path)
            },
            onDismiss = { onDialog(Dialog.None) },
        )
        is Dialog.RunProject -> ProjectRunSheet(d.project, onDismiss = { onDialog(Dialog.None) }, session = termux)
    }
}
