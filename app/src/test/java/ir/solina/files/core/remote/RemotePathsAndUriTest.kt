package ir.solina.files.core.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemotePathsAndUriTest {

    @Test
    fun `normalize resolves traversal and backslashes safely`() {
        assertEquals("/a/c", RemotePaths.normalize("/a\\b\\../c/./"))
        assertEquals("/", RemotePaths.normalize("/../../.."))
        assertEquals("/docs/solina", RemotePaths.join("/docs/tmp/..", "solina"))
        assertEquals("report.md", RemotePaths.name("/docs/report.md"))
        assertEquals("/", RemotePaths.parent("/docs"))
    }

    @Test
    fun `breadcrumbs builds cumulative hierarchy from root`() {
        assertEquals(listOf("/"), RemotePaths.breadcrumbs("/"))
        assertEquals(
            listOf("/", "/storage", "/storage/solina"),
            RemotePaths.breadcrumbs("/storage/solina"),
        )
    }

    @Test
    fun `RemoteUri builds, parses, and prevents invalid ids`() {
        val uri = RemoteUri.of("nas-1", "/media/photos")
        assertTrue(RemoteUri.isRemote(uri))
        assertFalse(RemoteUri.isRemote("/storage/emulated/0"))
        assertEquals("nas-1" to "/media/photos", RemoteUri.parse(uri))
        assertEquals("remote://nas-1/media/photos/2026", RemoteUri.child(uri, "2026"))
        assertEquals("remote://nas-1/media", RemoteUri.parent(uri))
        assertNull(RemoteUri.parent(RemoteUri.of("nas-1", "/")))
        assertEquals("HomeNAS:/media/photos", RemoteUri.display(uri, "HomeNAS"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `RemoteUri rejects connection id containing slash`() {
        RemoteUri.of("bad/id", "/")
    }
}
