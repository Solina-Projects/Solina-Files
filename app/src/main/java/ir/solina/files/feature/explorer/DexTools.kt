package ir.solina.files.feature.explorer

import ir.solina.files.R
import ir.solina.files.core.build.BuildListener
import ir.solina.files.core.dex.DexConvert
import ir.solina.files.core.fs.FileAccess
import ir.solina.files.core.i18n.tr
import ir.solina.files.core.model.FileItem
import ir.solina.files.feature.build.runBuild
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Smali → Dex and Jar → Dex conversion, split out of [ExplorerViewModel]. It reads and writes
 * the shared UI state through [state]; the view model keeps the public API unchanged.
 */
internal class DexTools(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ExplorerUiState>,
    private val access: suspend (dir: String, write: Boolean, viaRoot: Boolean) -> FileAccess,
    private val clearSelection: (Pane) -> Unit,
    private val refreshAllRevealing: (String, Collection<String>) -> Unit,
    private val refreshAll: () -> Unit,
    private val notOnStorage: () -> Unit,
) {
    suspend fun hasSmali(pane: Pane, folder: FileItem): Boolean {
        if (!folder.isDirectory || state.value.isRemote(pane)) return false
        val ps = state.value.pane(pane)
        return withContext(Dispatchers.IO) {
            runCatching {
                val from = access(ps.path, false, ps.viaRoot)
                DexConvert.containsSmali(from.file(folder.path), from)
            }.getOrDefault(false)
        }
    }

    fun smaliToDex(pane: Pane, folder: FileItem) {
        if (state.value.isRemote(pane)) return notOnStorage()
        val ps = state.value.pane(pane)
        build("Smali → Dex", folder.name, listOf(DexConvert.STEP_COLLECT), pane, ps.path) { log ->
            val from = access(ps.path, false, ps.viaRoot)
            val to = access(ps.path, true, ps.viaRoot)
            DexConvert.smaliToDex(folder.path, from, to, log)
        }
    }

    fun jarToDex(pane: Pane, jar: FileItem) {
        if (state.value.isRemote(pane)) return notOnStorage()
        val ps = state.value.pane(pane)
        build("Jar → Dex", jar.name, listOf(DexConvert.STEP_READ_JAR, DexConvert.STEP_D8), pane, ps.path) { log ->
            val from = access(ps.path, false, ps.viaRoot)
            val to = access(ps.path, true, ps.viaRoot)
            DexConvert.jarToDex(jar.path, from, to, log = log)
        }
    }

    private fun build(title: String, subject: String, plan: List<String>, pane: Pane?, dir: String, onDone: (File) -> Unit = {}, work: suspend (BuildListener) -> File) {
        if (state.value.build?.finished == false) return
        scope.launch {
            val result = runBuild(
                title,
                subject,
                plan,
                publish = { run -> state.update { it.copy(build = run) } },
                summary = { out: File -> tr(R.string.explorer_wrote, out.name) },
                work = work,
            )
            result.onSuccess { out ->
                pane?.let { clearSelection(it) }
                refreshAllRevealing(dir, listOf(out.absolutePath))
                onDone(out)
            }.onFailure { refreshAll() }
        }
    }
}
