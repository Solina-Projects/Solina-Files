package ir.solina.files.feature.explorer

import ir.solina.files.R
import ir.solina.files.core.bookmarks.Bookmark
import ir.solina.files.core.bookmarks.BookmarkRepository
import ir.solina.files.core.bookmarks.Bookmarks
import ir.solina.files.core.i18n.tr
import ir.solina.files.core.i18n.trPlural
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Bookmark persistence operations, split out of [ExplorerViewModel].
 * Messages are reported through [post]; the view model keeps the public API unchanged.
 */
internal class BookmarkEditor(
    private val repo: BookmarkRepository,
    private val scope: CoroutineScope,
    private val post: (String?) -> Unit,
) {
    fun add(paths: List<String>): Job = scope.launch {
        var added = 0
        repo.edit { list -> Bookmarks.add(list, paths).also { added = it.second }.first }
        post(
            when (added) {
                0 -> tr(R.string.explorer_bookmark_exists)
                1 -> tr(R.string.explorer_bookmark_added)
                else -> trPlural(R.plurals.explorer_bookmarks_added, added, added)
            },
        )
    }

    fun update(path: String, edited: Bookmark): Job = scope.launch {
        var error: String? = null
        repo.edit { list -> Bookmarks.update(list, path, edited).getOrElse { e -> error = e.message; list } }
        post(error)
    }

    fun remove(path: String): Job = scope.launch { repo.edit { Bookmarks.remove(it, listOf(path)) } }

    fun move(path: String, offset: Int): Job = scope.launch { repo.edit { Bookmarks.move(it, path, offset) } }

    fun deleteGroup(group: String): Job = scope.launch { repo.edit { Bookmarks.deleteGroup(it, group) } }

    fun renameGroup(from: String, to: String): Job = scope.launch { repo.edit { Bookmarks.renameGroup(it, from, to) } }
}
