package ir.solina.files.feature.explorer

import java.io.File

/**
 * Remembers local copies of remote files that were opened for viewing or editing, and detects which
 * ones changed since they were downloaded or last synced. Split out of [ExplorerViewModel].
 *
 * The methods are synchronized because copies are registered from an IO thread while the UI thread checks for edits.
 */
internal class RemoteEditTracker {

    private data class Opened(val uri: String, val stamp: Long, val length: Long)

    private val opened = LinkedHashMap<String, Opened>()

    /** Starts (or restarts) tracking [copy], taking its current size and time as the unmodified state. */
    @Synchronized
    fun track(copy: File, uri: String) {
        opened[copy.path] = Opened(uri, copy.lastModified(), copy.length())
    }

    /** Copies that still exist and differ from their tracked state. Copies deleted from disk are forgotten. */
    @Synchronized
    fun pending(): List<RemoteEdit> {
        opened.keys.filter { !File(it).exists() }.forEach { opened.remove(it) }
        return opened
            .filter { (copy, o) -> File(copy).let { it.exists() && (it.lastModified() != o.stamp || it.length() != o.length) } }
            .map { (copy, o) -> RemoteEdit(copy, o.uri) }
    }

    /** Accepts the current content of [copy] as the new unmodified state (after an upload, or when the user keeps the edit local). */
    @Synchronized
    fun markSynced(copy: String) {
        val o = opened[copy] ?: return
        val f = File(copy)
        opened[copy] = o.copy(stamp = f.lastModified(), length = f.length())
    }

    /** Forgets every tracked copy whose path contains [marker]. */
    @Synchronized
    fun forgetMatching(marker: String) {
        opened.keys.filter { it.contains(marker) }.forEach { opened.remove(it) }
    }
}
