package ir.solina.files.core.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteConnectionsTest {

    @Test
    fun `upsert clears password when savePassword is false and assigns id`() {
        val draft = RemoteConnection(
            id = "",
            name = "Solina SFTP",
            protocol = RemoteProtocol.SFTP,
            host = "192.168.1.20",
            username = "admin",
            password = "secret-password",
            savePassword = false,
        )

        val list = RemoteConnections.upsert(emptyList(), draft)
        assertEquals(1, list.size)
        val saved = list.first()
        assertTrue(saved.id.isNotBlank())
        assertEquals("", saved.password)
    }

    @Test
    fun `encode and decode roundtrips connections and handles corrupt input safely`() {
        val conn = RemoteConnection(
            id = "conn-1",
            name = "NAS",
            protocol = RemoteProtocol.SMB,
            host = "10.0.0.5",
            username = "user",
            password = "pw",
            savePassword = true,
        )
        val encoded = RemoteConnections.encode(listOf(conn))
        val decoded = RemoteConnections.decode(encoded)
        assertEquals(listOf(conn), decoded)
        assertNotNull(RemoteConnections.find(decoded, "conn-1"))
        assertTrue(RemoteConnections.remove(decoded, "conn-1").isEmpty())
        assertNull(RemoteConnections.find(decoded, "missing"))
        assertTrue(RemoteConnections.decode("{corrupt-json").isEmpty())
        assertTrue(RemoteConnections.decode(null).isEmpty())
    }
}
