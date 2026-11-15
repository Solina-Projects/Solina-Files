package ir.solina.files.feature.explorer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.remote.RemoteUri
import ir.solina.files.feature.explorer.components.PaneColumn

/** The single / dual (horizontal or vertical) pane area of the explorer. Behaviour unchanged. */
@Composable
internal fun ExplorerPaneArea(
    padding: PaddingValues,
    ui: ExplorerUiState,
    vm: ExplorerViewModel,
    dual: Boolean,
    horizontalSplit: Boolean,
    rootPath: String,
    onOpen: (Pane, FileItem) -> Unit,
    onDialog: (Dialog) -> Unit,
) {
    @Composable
    fun PaneContent(pane: Pane, modifier: Modifier) {
        val state by vm.paneState(pane).collectAsStateWithLifecycle()
        val visible = remember(state.items, ui.settings, ui.query) { visibleItems(state, ui.settings, ui.query) }
        PaneColumn(
            state = state,
            visible = visible,
            isActive = ui.active == pane,
            showActive = dual,
            rootPath = rootPath,
            onActivate = { vm.setActive(pane) },
            onUp = { vm.setActive(pane); vm.navigateUp(pane) },
            onOpen = { onOpen(pane, it) },
            onToggleSelect = { vm.toggleSelect(pane, it) },
            onItemMenu = { onDialog(Dialog.ItemSheet(pane, it)) },
            onJumpTo = { onDialog(Dialog.JumpTo(pane)) },
            onRefresh = { vm.setActive(pane); vm.refresh(pane) },
            modifier = modifier,
            displayPath = if (RemoteUri.isRemote(state.path)) ui.displayPath(state.path) else null,
            pane = pane,
            horizontalItems = dual && horizontalSplit,
            singlePane = !dual,
        )
    }
    AnimatedContent(
        targetState = dual to horizontalSplit,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.92f))
                .togetherWith(fadeOut() + scaleOut(targetScale = 0.96f))
        },
        label = "singleDualPaneTransition",
        modifier = Modifier.fillMaxSize().padding(padding),
    ) { (showDual, splitHorizontal) ->
        if (showDual && splitHorizontal) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                PaneContent(Pane.LEFT, Modifier.weight(1f).fillMaxWidth())
                PaneContent(Pane.RIGHT, Modifier.weight(1f).fillMaxWidth())
            }
        } else if (!showDual) {
            PaneContent(ui.active, Modifier.fillMaxSize().padding(horizontal = 4.dp))
        } else {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(Pane.LEFT, Pane.RIGHT).forEach { pane ->
                    PaneContent(pane, Modifier.weight(1f).fillMaxSize())
                }
            }
        }
    }
}
