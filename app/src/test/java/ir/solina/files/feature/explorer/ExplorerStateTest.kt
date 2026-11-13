package ir.solina.files.feature.explorer

import ir.solina.files.core.data.SolinaSettings
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.model.FolderSorts
import ir.solina.files.core.model.SortBy
import ir.solina.files.core.model.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplorerStateTest {

    @Test
    fun `NavHistory tracks back, forward, duplicate visits, and MAX bound`() {
        var history = NavHistory()
        history = history.visit("/a", "/a")
        assertFalse(history.canBack)

        history = history.visit("/a", "/a/b")
        history = history.visit("/a/b", "/a/b/c")
        assertEquals(listOf("/a", "/a/b"), history.back)
        assertTrue(history.canBack)

        history = history.afterBack("/a/b/c")
        assertEquals(listOf("/a"), history.back)
        assertEquals(listOf("/a/b/c"), history.forward)
        assertTrue(history.canForward)

        history = history.afterForward("/a/b")
        assertEquals(listOf("/a", "/a/b"), history.back)
        assertTrue(history.forward.isEmpty())
    }

    @Test
    fun `directionOf and childTowards identify hierarchy accurately`() {
        assertEquals(NavDirection.FORWARD, directionOf("/storage", "/storage/Download"))
        assertEquals(NavDirection.BACK, directionOf("/storage/Download", "/storage"))
        assertEquals(NavDirection.JUMP, directionOf("/storage/DCIM", "/storage/Download"))
        assertEquals(NavDirection.NONE, directionOf("/storage", "/storage"))
        assertEquals("/storage/Projects", childTowards("/storage", "/storage/Projects/Solina"))
        assertNull(childTowards("/storage/DCIM", "/storage/Download"))
    }

    @Test
    fun `visibleItems filters hidden files, matches query, and sorts folders first`() {
        val items = listOf(
            FileItem("readme.md", "/dir/readme.md", false, 120L, 100L, false, true, true),
            FileItem(".secret", "/dir/.secret", false, 10L, 200L, true, true, true),
            FileItem("src", "/dir/src", true, 0L, 50L, false, true, true, childCount = 4),
            FileItem("App.kt", "/dir/App.kt", false, 800L, 300L, false, true, true),
        )
        val pane = PaneState(path = "/dir", items = items)
        val settings = SolinaSettings(
            sortOrder = SortOrder(by = SortBy.NAME, descending = false, dirsFirst = true),
            showHidden = false,
        )

        val defaultVisible = visibleItems(pane, settings, "")
        assertEquals(listOf("src", "App.kt", "readme.md"), defaultVisible.map { it.name })

        val hiddenQuery = visibleItems(pane, settings.copy(showHidden = true), "sec")
        assertEquals(listOf(".secret"), hiddenQuery.map { it.name })
    }

    @Test
    fun `FolderSorts encodes and decodes per-folder overrides`() {
        val overrides = FolderSorts.with(
            emptyMap(),
            "/storage/Download/",
            SortOrder(SortBy.DATE, descending = true, dirsFirst = true),
        )
        val json = FolderSorts.encode(overrides)
        val restored = FolderSorts.decode(json)
        val effective = FolderSorts.effective("/storage/Download", SortOrder(), restored)
        assertEquals(SortBy.DATE, effective.by)
        assertTrue(effective.descending)
    }
}
