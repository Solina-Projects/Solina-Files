package ir.solina.files.feature.explorer

import ir.solina.files.core.fs.Conflict
import ir.solina.files.core.model.ConflictChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExplorerRulesTest {

    @Test
    fun `ask means no automatic policy`() {
        assertNull(ExplorerRules.conflictPolicy(ConflictChoice.ASK))
    }

    @Test
    fun `each saved choice maps to its conflict policy`() {
        assertEquals(Conflict.KEEP_BOTH, ExplorerRules.conflictPolicy(ConflictChoice.KEEP_BOTH))
        assertEquals(Conflict.OVERWRITE, ExplorerRules.conflictPolicy(ConflictChoice.OVERWRITE))
        assertEquals(Conflict.SKIP, ExplorerRules.conflictPolicy(ConflictChoice.SKIP))
    }

    @Test
    fun `signed apk name replaces the extension case-insensitively`() {
        assertEquals("app-signed.apk", ExplorerRules.signedApkName("app.apk"))
        assertEquals("App-signed.apk", ExplorerRules.signedApkName("App.APK"))
    }

    @Test
    fun `signed apk name appends when there is no apk extension`() {
        assertEquals("bundle.zip-signed.apk", ExplorerRules.signedApkName("bundle.zip"))
        assertEquals("noext-signed.apk", ExplorerRules.signedApkName("noext"))
    }
}
