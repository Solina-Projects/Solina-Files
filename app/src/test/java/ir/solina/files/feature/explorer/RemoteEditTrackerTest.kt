package ir.solina.files.feature.explorer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RemoteEditTrackerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun file(name: String, text: String): java.io.File =
        tmp.newFile(name).apply { writeText(text); setLastModified(1_000_000L) }

    @Test
    fun `an untouched copy is not reported`() {
        val f = file("a.txt", "one")
        val tracker = RemoteEditTracker().apply { track(f, "remote://1/a.txt") }
        assertTrue(tracker.pending().isEmpty())
    }

    @Test
    fun `a changed copy is reported with its remote uri`() {
        val f = file("a.txt", "one")
        val tracker = RemoteEditTracker().apply { track(f, "remote://1/a.txt") }
        f.writeText("one and more")
        assertEquals(listOf(RemoteEdit(f.path, "remote://1/a.txt")), tracker.pending())
    }

    @Test
    fun `marking a copy as synced clears the pending edit`() {
        val f = file("a.txt", "one")
        val tracker = RemoteEditTracker().apply { track(f, "remote://1/a.txt") }
        f.writeText("changed")
        tracker.markSynced(f.path)
        assertTrue(tracker.pending().isEmpty())
    }

    @Test
    fun `a deleted copy is forgotten`() {
        val f = file("a.txt", "one")
        val tracker = RemoteEditTracker().apply { track(f, "remote://1/a.txt") }
        f.delete()
        assertTrue(tracker.pending().isEmpty())
        f.writeText("back again")
        assertTrue(tracker.pending().isEmpty())
    }

    @Test
    fun `forgetMatching drops only matching copies`() {
        val keep = file("keep.txt", "k")
        val drop = tmp.newFolder("remote-panes", "vault").resolve("d.txt").apply { writeText("d"); setLastModified(1_000_000L) }
        val tracker = RemoteEditTracker().apply { track(keep, "remote://1/k"); track(drop, "remote://v/d") }
        keep.writeText("kk"); drop.writeText("dd")
        tracker.forgetMatching("/remote-panes/vault/")
        assertEquals(listOf(keep.path), tracker.pending().map { it.copy })
    }
}
