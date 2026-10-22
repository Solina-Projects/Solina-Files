package ir.solina.files.core.vault

import ir.solina.files.R
import ir.solina.files.core.i18n.tr
import ir.solina.files.core.remote.RemoteConnection
import ir.solina.files.core.remote.RemoteProtocol
import ir.solina.files.core.remote.RemoteUri
import java.io.File
import java.io.IOException

object Vault {

    const val ID = "vault"

    @Volatile
    private var instance: VaultStore? = null

    val store: VaultStore get() = instance ?: throw IOException(tr(R.string.vault_err_unavailable))

    val ready: Boolean get() = instance != null

    @Synchronized
    fun init(filesDir: File, cacheDir: File) {
        if (instance != null) return
        val store = VaultStore(File(filesDir, "vault"))
        store.onLock { clearOpened(cacheDir) }
        clearOpened(cacheDir)
        instance = store
    }

    fun install(store: VaultStore?) {
        instance = store
    }

    val root: String get() = RemoteUri.of(ID)

    fun isVault(path: String?): Boolean = path != null && RemoteUri.parse(path)?.first == ID

    fun connection(): RemoteConnection = RemoteConnection(ID, tr(R.string.model_protocol_vault), RemoteProtocol.VAULT, host = "", anonymous = true)

    fun openedDir(cacheDir: File): File = File(cacheDir, "remote-panes/$ID")

    private fun clearOpened(cacheDir: File) {
        val dir = openedDir(cacheDir)
        if (dir.exists()) ir.solina.files.core.fs.FsUtil.deleteRecursively(dir)
    }
}
