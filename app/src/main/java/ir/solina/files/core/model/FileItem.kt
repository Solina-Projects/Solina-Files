package ir.solina.files.core.model

import java.io.File

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isHidden: Boolean,
    val canRead: Boolean,
    val canWrite: Boolean,
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
            val isDirectory = file.isDirectory
            val childCount = if (isDirectory && calculateChildCount) file.list()?.size ?: -1 else -1
            return FileItem(
                name = name,
                path = file.absolutePath,
                isDirectory = isDirectory,
                size = if (isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                isHidden = file.isHidden || name.startsWith('.'),
                canRead = file.canRead(),
                canWrite = file.canWrite(),
                isSymlink = ir.solina.files.core.fs.FsUtil.isSymlink(file),
                childCount = childCount,
            )
        }
    }
}
