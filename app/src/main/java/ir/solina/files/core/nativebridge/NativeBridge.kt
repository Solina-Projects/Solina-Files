package ir.solina.files.core.nativebridge

import ir.solina.files.R
import ir.solina.files.core.i18n.tr
import java.io.IOException

object NativeBridge {

    @Volatile
    private var loaded = false

    @Synchronized
    private fun ensureLoaded(): Boolean {
        if (loaded) return true
        return try {
            System.loadLibrary("solina_native")
            loaded = true
            true
        } catch (_: Throwable) {
            false
        }
    }

    val available: Boolean get() = ensureLoaded()

    private external fun version(): String
    private external fun hashFile(path: String, algo: Int): String
    private external fun fileSize(path: String): Long
    private external fun readRange(path: String, offset: Long, length: Int): ByteArray?
    private external fun indexOf(path: String, pattern: ByteArray, from: Long): Long
    private external fun lastIndexOf(path: String, pattern: ByteArray, from: Long): Long
    private external fun writeBytes(path: String, offset: Long, data: ByteArray): Boolean
    private external fun listDir(path: String): ByteArray?
    private external fun dirStats(path: String, token: Long, out: LongArray): Boolean
    private external fun dirStatsBatch(path: String, childNames: Array<String>, token: Long, out: LongArray): Boolean
    private external fun cancelScan(token: Long)
    private external fun rarList(path: String, password: String?, status: IntArray): Array<String>?
    private external fun rarExtract(path: String, password: String?, sink: RarSink): Int
    private external fun encDeriveKey(password: ByteArray, salt: ByteArray, mCostKib: Int, tCost: Int, pCost: Int): ByteArray?
    private external fun encSeal(key: ByteArray, nonce: ByteArray, aad: ByteArray, data: ByteArray, length: Int): ByteArray?
    private external fun encOpen(key: ByteArray, nonce: ByteArray, aad: ByteArray, data: ByteArray, length: Int): ByteArray?

    /** One entry of [listDirectory]; [flags] bits: 1 dir, 2 symlink, 4 hidden, 8 readable. */
    class DirEntry(val name: String, val size: Long, val lastModified: Long, val flags: Int) {
        val isDirectory: Boolean get() = flags and 1 != 0
        val isSymlink: Boolean get() = flags and 2 != 0
        val isHidden: Boolean get() = flags and 4 != 0
        val canRead: Boolean get() = flags and 8 != 0
    }

    /** Lists [path] in a single native call, or null if native is unavailable or the read failed. */
    fun listDirectory(path: String): List<DirEntry>? {
        if (!ensureLoaded()) return null
        val buf = runCatching { listDir(path) }.getOrNull() ?: return null
        return runCatching {
            val bb = java.nio.ByteBuffer.wrap(buf).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            val count = bb.int
            val out = ArrayList<DirEntry>(count)
            repeat(count) {
                val flags = bb.get().toInt() and 0xff
                val size = bb.long
                val mtime = bb.long
                val len = bb.short.toInt() and 0xffff
                val name = String(buf, bb.position(), len, Charsets.UTF_8)
                bb.position(bb.position() + len)
                out.add(DirEntry(name, size, mtime, flags))
            }
            out
        }.getOrNull()
    }

    private val scanTokens = java.util.concurrent.atomic.AtomicLong(1)

    /** Total bytes and file count below [path] (symlinks not followed); null on failure or cancel. */
    fun folderStats(path: String, token: Long = scanTokens.getAndIncrement()): Pair<Long, Int>? {
        if (!ensureLoaded()) return null
        val out = LongArray(3)
        val ok = runCatching { dirStats(path, token, out) }.getOrDefault(false)
        return if (ok) out[0] to out[1].toInt() else null
    }



    /**
     * Computes direct child folder statistics with one native traversal.
     * Returns null when native scanning is unavailable so callers can fall back.
     */
    fun folderStatsBatch(
        path: String,
        childNames: List<String>,
        token: Long = scanTokens.getAndIncrement(),
    ): Map<String, Pair<Long, Int>>? {
        if (!ensureLoaded()) return null
        if (childNames.isEmpty()) return emptyMap()
        val names = childNames.toTypedArray()
        val out = LongArray(names.size * 3)
        val ok = runCatching { dirStatsBatch(path, names, token, out) }.getOrDefault(false)
        if (!ok) return null
        return buildMap(names.size) {
            names.forEachIndexed { index, name ->
                val offset = index * 3
                put(name, out[offset] to out[offset + 1].toInt())
            }
        }
    }

    fun newScanToken(): Long = scanTokens.getAndIncrement()

    fun cancelScanToken(token: Long) {
        if (ensureLoaded()) runCatching { cancelScan(token) }
    }

    fun nativeVersion(): String? = if (ensureLoaded()) runCatching { version() }.getOrNull() else null

    enum class HashAlgo(val code: Int, val label: String) {
        CRC32(0, "CRC32"),
        MD5(1, "MD5"),
        SHA1(2, "SHA-1"),
        SHA256(3, "SHA-256"),
    }

    fun hash(path: String, algo: HashAlgo): String? {
        if (!ensureLoaded()) return null
        return runCatching { hashFile(path, algo.code) }.getOrNull()?.ifEmpty { null }
    }

    fun size(path: String): Long? {
        if (!ensureLoaded()) return null
        return fileSize(path).takeIf { it >= 0 }
    }

    fun read(path: String, offset: Long, length: Int): ByteArray? {
        if (!ensureLoaded()) return null
        return readRange(path, offset, length)
    }

    fun find(path: String, pattern: ByteArray, from: Long): Long {
        check(ensureLoaded()) { tr(R.string.files_native_unavailable) }
        val r = indexOf(path, pattern, from)
        if (r < -1) throw IOException(tr(R.string.files_native_search_failed))
        return r
    }

    fun findLast(path: String, pattern: ByteArray, from: Long): Long {
        check(ensureLoaded()) { tr(R.string.files_native_unavailable) }
        val r = lastIndexOf(path, pattern, from)
        if (r < -1) throw IOException(tr(R.string.files_native_search_failed))
        return r
    }

    fun write(path: String, offset: Long, data: ByteArray): Boolean {
        if (!ensureLoaded()) return false
        return writeBytes(path, offset, data)
    }

    fun deriveKey(password: ByteArray, salt: ByteArray, mCostKib: Int, tCost: Int, pCost: Int): ByteArray? {
        if (!ensureLoaded()) return null
        return encDeriveKey(password, salt, mCostKib, tCost, pCost)
    }

    fun seal(key: ByteArray, nonce: ByteArray, aad: ByteArray, data: ByteArray, length: Int = data.size): ByteArray? {
        check(ensureLoaded()) { tr(R.string.files_native_unavailable) }
        return encSeal(key, nonce, aad, data, length)
    }

    fun open(key: ByteArray, nonce: ByteArray, aad: ByteArray, data: ByteArray, length: Int = data.size): ByteArray? {
        check(ensureLoaded()) { tr(R.string.files_native_unavailable) }
        return encOpen(key, nonce, aad, data, length)
    }

    fun rarHeaders(path: String, password: String?, status: IntArray): Array<String>? =
        if (!ensureLoaded()) null.also { status[0] = -1 } else rarList(path, password, status)

    fun rarStream(path: String, password: String?, sink: RarSink): Int =
        if (!ensureLoaded()) -1 else rarExtract(path, password, sink)
}
