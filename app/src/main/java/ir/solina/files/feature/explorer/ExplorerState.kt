package ir.solina.files.feature.explorer

import ir.solina.files.R
import ir.solina.files.core.i18n.tr

import ir.solina.files.core.bookmarks.Bookmark
import ir.solina.files.core.data.SolinaSettings
import ir.solina.files.core.fs.MountEntry
import ir.solina.files.core.data.sortFor
import ir.solina.files.core.fs.OpProgress
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.remote.RemoteConnection
import ir.solina.files.core.remote.RemoteUri
import ir.solina.files.core.rename.RenameRule

enum class Pane {
    LEFT, RIGHT;

    val other: Pane get() = if (this == LEFT) RIGHT else LEFT
}

enum class NavDirection { NONE, FORWARD, BACK, JUMP }

data class NavHistory(val back: List<String> = emptyList(), val forward: List<String> = emptyList()) {
    val canBack: Boolean get() = back.isNotEmpty()
    val canForward: Boolean get() = forward.isNotEmpty()

    fun visit(from: String, to: String): NavHistory =
        if (from == to) this else NavHistory((back + from).takeLast(MAX), emptyList())

    fun afterBack(from: String): NavHistory = NavHistory(back.dropLast(1), (forward + from).takeLast(MAX))

    fun afterForward(from: String): NavHistory = NavHistory((back + from).takeLast(MAX), forward.dropLast(1))

    fun outsideStorages(home: String): List<String> = (back.filter { !RemoteUri.isRemote(it) }.asReversed() + home).distinct()

    companion object {
        const val MAX = 64
    }
}

enum class HistoryOp { VISIT, BACK, FORWARD, NONE }

data class PaneState(
    val path: String,
    val items: List<FileItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val selected: Set<String> = emptySet(),
    val direction: NavDirection = NavDirection.NONE,
    val cameFrom: String? = null,
    val viaRoot: Boolean = false,
    val history: NavHistory = NavHistory(),
    val reveal: Set<String> = emptySet(),
    val revealSeq: Int = 0,
    val mount: MountEntry? = null,
    val readOnly: Boolean = false,
) {
    val hasSelection: Boolean get() = selected.isNotEmpty()
}

data class ClipboardState(
    val items: List<FileItem>,
    val move: Boolean,
    val sourceDir: String = items.firstOrNull()?.parentPath ?: "/",
    val viaRoot: Boolean = false,
)

enum class PasswordPurpose { EXTRACT, DECRYPT }

data class PasswordPrompt(val pane: Pane, val item: FileItem, val purpose: PasswordPurpose, val wrong: Boolean = false)

data class TransferPrompt(
    val move: Boolean,
    val count: Int,
    val destination: String,
    val conflicts: List<String>,
    val pastePane: Pane? = null,
    val drop: DropRequest? = null,
)

data class DropRequest(val pane: Pane, val items: List<FileItem>, val destination: String)

data class MoveUndo(val message: String, val moved: List<String>, val backTo: String, val seq: Int)

data class IncomingCopy(val uri: String, val name: String, val seq: Int)

data class TaskState(
    val title: String,
    val detail: String = "",
    val fraction: Float = 0f,
    val indeterminate: Boolean = false,
)

fun taskState(title: String, p: OpProgress.Running): TaskState {
    val fraction = when {
        p.totalBytes > 0 -> (p.doneBytes.toDouble() / p.totalBytes).toFloat()
        p.totalFiles > 0 -> p.doneFiles.toFloat() / p.totalFiles
        else -> 0f
    }
    return TaskState(
        title = title,
        detail = p.currentName,
        fraction = fraction.coerceIn(0f, 1f),
        indeterminate = p.totalBytes <= 0 && p.totalFiles <= 0,
    )
}

data class ExplorerUiState(
    val left: PaneState,
    val right: PaneState,
    val active: Pane = Pane.LEFT,
    val settings: SolinaSettings = SolinaSettings(),
    val clipboard: ClipboardState? = null,
    val task: TaskState? = null,
    val build: ir.solina.files.core.build.BuildRun? = null,
    val query: String = "",
    val searchActive: Boolean = false,
    val message: String? = null,
    val bookmarks: List<Bookmark> = emptyList(),
    val renameHistory: List<RenameRule> = emptyList(),
    val passwordPrompt: PasswordPrompt? = null,
    val transferPrompt: TransferPrompt? = null,
    val storages: List<RemoteConnection> = emptyList(),
    val remoteEdits: List<RemoteEdit> = emptyList(),
    val storagePrompt: StoragePrompt? = null,
    val undo: MoveUndo? = null,
    val incoming: IncomingCopy? = null,
) {
    fun isRemote(pane: Pane): Boolean = RemoteUri.isRemote(pane(pane).path)

    fun displayPath(path: String): String = when {
        ir.solina.files.core.vault.Vault.isVault(path) -> RemoteUri.display(path, tr(R.string.model_protocol_vault))
        RemoteUri.isRemote(path) -> RemoteUri.display(path, storages.firstOrNull { it.id == RemoteUri.id(path) }?.let { it.name.ifBlank { it.host } })
        else -> path
    }

    fun isVault(pane: Pane): Boolean = ir.solina.files.core.vault.Vault.isVault(pane(pane).path)

    fun pane(p: Pane) = if (p == Pane.LEFT) left else right
    val activePane: PaneState get() = pane(active)
    val inactive: Pane get() = active.other
}

fun visibleItems(pane: PaneState, settings: SolinaSettings, query: String): List<FileItem> =
    pane.items.asSequence()
        .filter { settings.showHidden || !it.isHidden }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        .sortedWith(settings.sortFor(pane.path).comparator())
        .toList()

fun directionOf(from: String, to: String): NavDirection = when {
    from == to -> NavDirection.NONE
    to.startsWith(from.trimEnd('/') + "/") -> NavDirection.FORWARD
    from.startsWith(to.trimEnd('/') + "/") || to == "/" -> NavDirection.BACK
    else -> NavDirection.JUMP
}

fun childTowards(ancestor: String, descendant: String): String? {
    val base = ancestor.trimEnd('/')
    val rest = descendant.removePrefix("$base/")
    if (rest == descendant || rest.isEmpty()) return null
    return "$base/${rest.substringBefore('/')}"
}

data class RemoteEdit(val copy: String, val uri: String)

sealed interface StoragePrompt {
    val pane: Pane
    val path: String
    val connection: RemoteConnection

    data class Password(override val pane: Pane, override val path: String, override val connection: RemoteConnection) : StoragePrompt

    data class HostKey(override val pane: Pane, override val path: String, override val connection: RemoteConnection, val presented: String) : StoragePrompt

    data class Vault(
        override val pane: Pane,
        override val path: String,
        override val connection: RemoteConnection,
        val setup: Boolean,
        val moving: List<FileItem> = emptyList(),
    ) : StoragePrompt
}
