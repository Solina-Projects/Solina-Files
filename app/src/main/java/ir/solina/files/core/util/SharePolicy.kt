package ir.solina.files.core.util

import ir.solina.files.core.fs.FsUtil
import java.io.File

object SharePolicy {
    fun mayShare(file: File, privateDirs: List<File>, cacheDirs: List<File>): Boolean =
        privateDirs.none { FsUtil.isSameOrDescendant(it, file) } || cacheDirs.any { FsUtil.isSameOrDescendant(it, file) }
}
