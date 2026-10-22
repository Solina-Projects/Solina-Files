package ir.solina.files.core.fs

import ir.solina.files.R
import ir.solina.files.core.i18n.tr
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.remote.RemoteUri
import ir.solina.files.core.root.RootPaths
import java.io.IOException

data class Listing(val items: List<FileItem>, val viaRoot: Boolean)

class DirectoryLister(
    private val storageRoot: String,
    private val local: suspend (String) -> List<FileItem>?,
    private val root: suspend (String) -> List<FileItem>?,
    private val missing: suspend (String) -> Boolean = { false },
    private val fallback: suspend (String) -> List<FileItem>? = { null },
    private val remote: (suspend (String) -> List<FileItem>)? = null,
) {
    suspend fun list(path: String, rootEnabled: Boolean): Listing {
        if (RemoteUri.isRemote(path)) {
            val listRemote = remote ?: throw IOException(tr(R.string.files_list_no_network))
            return Listing(listRemote(path), viaRoot = false)
        }
        val preferRoot = rootEnabled && RootPaths.isRestrictedShared(path, storageRoot)
        if (!preferRoot) local(path)?.let { return Listing(it, viaRoot = false) }
        if (rootEnabled) root(path)?.let { return Listing(it, viaRoot = true) }
        if (preferRoot) local(path)?.let { return Listing(it, viaRoot = false) }
        fallback(path)?.let { return Listing(it, viaRoot = false) }
        if (missing(path)) throw IOException(tr(R.string.files_list_missing))
        throw IOException(tr(if (rootEnabled) R.string.files_list_unreadable else R.string.files_list_denied))
    }
}
