package ir.solina.files.core.model

import android.os.Build
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isHidden: Boolean,
    val canRead: Boolean,
    val isSymlink: Boolean = false,
    val childCount: Int = -1,
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "").lowercase()

    val parentPath: String?
        get() = File(path).parent

    companion object {
        fun of(file: File, calculateChildCount: Boolean = true): FileItem {
            val name = file.name
            // One stat instead of three: readAttributes returns the type, size
            // and mtime together. It throws if the file vanished between the
            // directory listing and this call, so fall back to the individual
            // File calls then (same values as before, just slower).
            // File.toPath() / Files.readAttributes need API 26+; minSdk is 24,
            // so on Android 7.x fall back to the individual File calls.
            val attrs: BasicFileAttributes? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runCatching {
                    Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                }.getOrNull()
            } else null
            val isDirectory = attrs?.isDirectory ?: file.isDirectory
            val childCount = if (isDirectory && calculateChildCount) file.list()?.size ?: -1 else -1
            return FileItem(
                name = name,
                path = file.absolutePath,
                isDirectory = isDirectory,
                size = if (isDirectory) 0L else attrs?.size() ?: file.length(),
                lastModified = attrs?.lastModifiedTime()?.toMillis() ?: file.lastModified(),
                // File.isHidden() on Unix is exactly "name starts with '.'",
                // so the extra stat call for it was redundant.
                isHidden = name.startsWith('.'),
                canRead = file.canRead(),
                isSymlink = ir.solina.files.core.fs.FsUtil.isSymlink(file),
                childCount = childCount,
            )
        }
    }
}
