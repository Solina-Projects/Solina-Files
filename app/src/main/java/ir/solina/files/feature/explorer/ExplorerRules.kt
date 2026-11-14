package ir.solina.files.feature.explorer

import ir.solina.files.core.fs.Conflict
import ir.solina.files.core.model.ConflictChoice

/** Pure rules used by [ExplorerViewModel], kept free of Android types so they can be unit tested. */
internal object ExplorerRules {

    /** The conflict policy to apply automatically, or null when the user must be asked. */
    fun conflictPolicy(choice: ConflictChoice): Conflict? = when (choice) {
        ConflictChoice.ASK -> null
        ConflictChoice.KEEP_BOTH -> Conflict.KEEP_BOTH
        ConflictChoice.OVERWRITE -> Conflict.OVERWRITE
        ConflictChoice.SKIP -> Conflict.SKIP
    }

    fun signedApkName(name: String): String =
        if (name.endsWith(".apk", ignoreCase = true)) name.dropLast(4) + "-signed.apk" else "$name-signed.apk"
}
