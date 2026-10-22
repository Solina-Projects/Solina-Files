package ir.solina.files.core.nativebridge

interface RarSink {
    fun begin(name: String, size: Long, flags: Int): Boolean

    fun write(buffer: ByteArray, length: Int): Boolean

    fun end(ok: Boolean)
}
