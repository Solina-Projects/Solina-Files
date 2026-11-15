package ir.solina.files.feature.explorer

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ir.solina.files.R
import ir.solina.files.core.remote.RemoteUri
import ir.solina.files.feature.explorer.components.PasswordDialog
import ir.solina.files.feature.explorer.components.TransferDialog
import ir.solina.files.feature.remote.HostKeyChangedDialog
import ir.solina.files.feature.remote.StoragePasswordDialog
import ir.solina.files.feature.vault.VaultSetupDialog
import ir.solina.files.feature.vault.VaultUnlockDialog

/** Prompts hosted by the explorer screen, split out of [ExplorerScreen]. Behaviour is unchanged. */

/** Asks whether locally edited copies of remote files should be uploaded. Hidden while another dialog is open ([visible] false). */
@Composable
internal fun RemoteEditsPrompt(vm: ExplorerViewModel, ui: ExplorerUiState, visible: Boolean) {
    if (ui.remoteEdits.isNotEmpty() && visible) {
        val edits = ui.remoteEdits
        AlertDialog(
            onDismissRequest = vm::keepRemoteEdits,
            title = { Text(stringResource(R.string.explorer_upload_title)) },
            text = {
                Text(
                    if (edits.size == 1) {
                        stringResource(
                            R.string.explorer_upload_one,
                            RemoteUri.name(edits[0].uri),
                            ui.displayPath(RemoteUri.parent(edits[0].uri) ?: edits[0].uri),
                        )
                    } else {
                        pluralStringResource(R.plurals.explorer_upload_many, edits.size, edits.size)
                    },
                )
            },
            confirmButton = { TextButton(onClick = vm::uploadRemoteEdits) { Text(stringResource(R.string.explorer_upload)) } },
            dismissButton = { TextButton(onClick = vm::keepRemoteEdits) { Text(stringResource(R.string.explorer_not_now)) } },
        )
    }
}

/** Storage unlock / host-key / vault prompts, the transfer conflict dialog and the archive password dialog. */
@Composable
internal fun ExplorerPrompts(vm: ExplorerViewModel, ui: ExplorerUiState) {
    when (val prompt = ui.storagePrompt) {
        null -> Unit
        is StoragePrompt.Password -> StoragePasswordDialog(prompt.connection, onConfirm = vm::unlockStorage, onDismiss = vm::dismissStoragePrompt)
        is StoragePrompt.HostKey -> HostKeyChangedDialog(
            prompt.connection, old = prompt.connection.hostKey, presented = prompt.presented,
            onTrust = vm::trustStorageKey, onDismiss = vm::dismissStoragePrompt,
        )
        is StoragePrompt.Vault -> if (prompt.setup) {
            VaultSetupDialog(onDismiss = vm::dismissStoragePrompt, onReady = vm::vaultReady)
        } else {
            VaultUnlockDialog(onDismiss = vm::dismissStoragePrompt, onUnlocked = vm::vaultReady)
        }
    }

    ui.transferPrompt?.let { prompt ->
        TransferDialog(
            move = prompt.move,
            count = prompt.count,
            destination = prompt.destination,
            conflicts = prompt.conflicts,
            onConfirm = { conflict -> vm.confirmTransfer(conflict) },
            onDismiss = { vm.cancelTransfer() },
        )
    }

    ui.passwordPrompt?.let { p ->
        PasswordDialog(
            title = stringResource(if (p.purpose == PasswordPurpose.EXTRACT) R.string.explorer_encrypted_archive else R.string.explorer_decrypt_file),
            fileName = p.item.name,
            wrong = p.wrong,
            onConfirm = vm::submitPassword,
            onDismiss = vm::dismissPasswordPrompt,
        )
    }
}
