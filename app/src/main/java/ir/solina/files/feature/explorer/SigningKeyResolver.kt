package ir.solina.files.feature.explorer

import ir.solina.files.R
import ir.solina.files.core.apk.KeyStoreFactory
import ir.solina.files.core.apk.KeystoreFile
import ir.solina.files.core.apk.KeystoreFormat
import ir.solina.files.core.apk.SavedKeys
import ir.solina.files.core.apk.SigningKey
import ir.solina.files.core.apk.SigningKeys
import ir.solina.files.core.build.BuildListener
import ir.solina.files.core.fs.FsUtil
import ir.solina.files.core.i18n.tr
import ir.solina.files.feature.apk.SignRequest
import java.io.File

/**
 * Turns a [SignRequest] into a [SigningKey], split out of [ExplorerViewModel.signApk].
 * Behaviour is unchanged: new keys are saved to the app's keystore folder and may also be exported as a .jks next to the APK.
 */
internal class SigningKeyResolver(private val filesDir: File) {

    fun stepLabel(request: SignRequest): String = tr(
        when (request) {
            is SignRequest.Quick -> R.string.explorer_step_default_key
            is SignRequest.SavedKey -> R.string.explorer_step_saved_key
            is SignRequest.NewKey -> R.string.explorer_step_new_key
            is SignRequest.ExistingKeystore -> R.string.explorer_step_keystore
        },
    )

    /** [onKept] receives the message to show once signing succeeds (called again when a .jks copy was also written). */
    suspend fun resolve(request: SignRequest, dir: String, log: BuildListener, onKept: (String) -> Unit): SigningKey =
        when (request) {
            is SignRequest.Quick -> SigningKeys.default(filesDir)
            is SignRequest.SavedKey -> SavedKeys(File(filesDir, "keystores")).load(request.id)
            is SignRequest.NewKey -> {
                val key = KeyStoreFactory.generateKey(request.identity)
                SavedKeys(File(filesDir, "keystores")).save(request.alias, key)
                log.info(tr(R.string.explorer_kept_as, request.alias))
                onKept(tr(R.string.explorer_signed_kept))
                if (request.jksCopy) {
                    val jks = FsUtil.uniqueSibling(File(dir, "${request.alias}.jks"), isDirectory = false)
                    jks.writeBytes(KeystoreFile.export(KeystoreFormat.JKS, listOf(request.alias to key), request.storePassword, request.keyPassword))
                    log.info(tr(R.string.explorer_saved_file, jks.name))
                    onKept(tr(R.string.explorer_signed_kept_jks, jks.name))
                }
                key
            }
            is SignRequest.ExistingKeystore -> KeyStoreFactory.load(request.spec)
        }
}
