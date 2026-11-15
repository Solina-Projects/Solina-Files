package ir.solina.files.feature.explorer

import ir.solina.files.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ir.solina.files.core.model.FloatingAction

/**
 * Floating quick-action buttons (home, refresh, bookmarks, create, view layout, pane mode ...) drawn over the explorer.
 * Split out of [ExplorerScreen]. The expanded/collapsed state of the two pop-up menus stays owned by the screen.
 */
@Composable
internal fun BoxScope.ExplorerFloatingActions(
    ui: ExplorerUiState,
    vm: ExplorerViewModel,
    dual: Boolean,
    horizontalSplit: Boolean,
    onToggleOrientation: () -> Unit,
    quickCreateExpanded: Boolean,
    onQuickCreateExpanded: (Boolean) -> Unit,
    layoutMenuExpanded: Boolean,
    onLayoutMenuExpanded: (Boolean) -> Unit,
    onDialog: (Dialog) -> Unit,
) {
    val createIconRotation by animateFloatAsState(if (quickCreateExpanded) 45f else 0f, label = "createFabRotation")
    val layoutIconScale by animateFloatAsState(if (layoutMenuExpanded) 0.88f else 1f, label = "layoutFabScale")
    if (!ui.searchActive) {
        Column(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(end = 16.dp, bottom = 84.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val floatingZones = ui.settings.actions.floating.zones
            // Only actions assigned to Quick are rendered as floating buttons; the other
            // stored zones are intentionally not surfaced in an overflow/menu.
            val floatingVisibleActions = floatingZones.getOrNull(0).orEmpty()
                .filterNot { !dual && (it == FloatingAction.ORIENTATION || it == FloatingAction.SWAP_PANES) }
            floatingVisibleActions.forEach { action ->
                when (action) {
                FloatingAction.HOME -> {
                FloatingActionButton(
                    onClick = { vm.goHome(ui.active); onQuickCreateExpanded(false); onLayoutMenuExpanded(false) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.fab_home))
                }
            
                }
                FloatingAction.REFRESH -> {
                FloatingActionButton(
                    onClick = { vm.refresh(ui.active); onQuickCreateExpanded(false); onLayoutMenuExpanded(false) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.fab_refresh))
                }
            
                }
                FloatingAction.BOOKMARKS -> {
                    FloatingActionButton(
                        onClick = { onDialog(Dialog.Bookmarks); onQuickCreateExpanded(false); onLayoutMenuExpanded(false) },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Icon(Icons.Rounded.Bookmarks, contentDescription = stringResource(R.string.fab_bookmarks))
                    }
                }
                FloatingAction.SWAP_PANES -> {
                if (dual) {
                FloatingActionButton(
                    onClick = { vm.swapPanes() },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = stringResource(R.string.fab_swap_panes))
                }
            
                }

                }
                FloatingAction.CREATE -> {
                    Column(
                        modifier = Modifier.width(240.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AnimatedVisibility(
                            visible = quickCreateExpanded,
                            enter = fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.94f),
                            exit = fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.96f),
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                ),
                                tonalElevation = 6.dp,
                                shadowElevation = 12.dp,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.fab_create_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                    ExtendedFloatingActionButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onQuickCreateExpanded(false); onDialog(Dialog.NewFolder(ui.active)) },
                                        icon = { Icon(Icons.Rounded.CreateNewFolder, contentDescription = null) },
                                        text = { Text(stringResource(R.string.fab_new_folder), maxLines = 1) },
                                        expanded = true,
                                    )
                                    ExtendedFloatingActionButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onQuickCreateExpanded(false); onDialog(Dialog.NewFile(ui.active, "untitled.txt")) },
                                        icon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                                        text = { Text(stringResource(R.string.fab_new_text_file), maxLines = 1) },
                                        expanded = true,
                                    )
                                    ExtendedFloatingActionButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onQuickCreateExpanded(false); onDialog(Dialog.NewFile(ui.active, "index.html")) },
                                        icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                                        text = { Text(stringResource(R.string.fab_new_html_file), maxLines = 1) },
                                        expanded = true,
                                    )
                                    if (!ui.isRemote(ui.active)) {
                                        ExtendedFloatingActionButton(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = { onQuickCreateExpanded(false); onDialog(Dialog.NewProject(ui.activePane.path)) },
                                            icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                                            text = { Text(stringResource(R.string.fab_new_project), maxLines = 1) },
                                            expanded = true,
                                        )
                                    }
                                }
                            }
                        }
                        if (FloatingAction.CREATE in floatingVisibleActions) {
                            FloatingActionButton(
                                onClick = {
                                    onQuickCreateExpanded(!quickCreateExpanded)
                                    onLayoutMenuExpanded(false)
                                },
                                containerColor = if (quickCreateExpanded) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                contentColor = if (quickCreateExpanded) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = if (quickCreateExpanded) stringResource(R.string.fab_close_quick_create) else stringResource(R.string.fab_quick_create),
                                    modifier = Modifier.graphicsLayer { rotationZ = createIconRotation },
                                )
                            }
                        }
                    }
                }
                FloatingAction.ORIENTATION -> {
                FloatingActionButton(
                    onClick = { onToggleOrientation() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(
                        imageVector = if (horizontalSplit) Icons.Rounded.ViewAgenda else Icons.Rounded.ViewColumn,
                        contentDescription = if (horizontalSplit) stringResource(R.string.fab_stack_panes) else stringResource(R.string.fab_side_panes),
                    )
                }
            
                }
                FloatingAction.VIEW_LAYOUT -> {
                Box {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedVisibility(
                            visible = layoutMenuExpanded,
                            enter = fadeIn() + scaleIn(initialScale = 0.82f),
                            exit = fadeOut() + scaleOut(targetScale = 0.82f),
                        ) {
                            Surface(
                                modifier = Modifier.width(240.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                ),
                                tonalElevation = 6.dp,
                                shadowElevation = 12.dp,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (!dual) {
                                            val isList = ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.LIST
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = if (isList) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                            ) {
                                                IconButton(
                                                    onClick = { if (!isList) vm.toggleViewMode() },
                                                    modifier = Modifier.size(48.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.ViewList,
                                                        contentDescription = stringResource(R.string.fab_list_layout),
                                                        tint = if (isList) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    )
                                                }
                                            }
                                            val isGrid = ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = if (isGrid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                        ) {
                                            IconButton(
                                                onClick = { if (!isGrid) vm.toggleViewMode() },
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Icon(
                                                    Icons.Rounded.GridView,
                                                    contentDescription = stringResource(R.string.fab_grid_layout),
                                                    tint = if (isGrid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }

                                        }
                                    }
                                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.Bottom,
                                    ) {
                                        listOf(
                                            ir.solina.files.core.model.UiDensity.COMPACT to stringResource(R.string.fab_density_compact),
                                            ir.solina.files.core.model.UiDensity.COMFORTABLE to stringResource(R.string.fab_density_comfortable),
                                            ir.solina.files.core.model.UiDensity.LARGE to stringResource(R.string.fab_density_large),
                                        ).forEach { (densityOption, label) ->
                                            val selectedDensity = ui.settings.uiDensity == densityOption
                                            Column(
                                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                            ) {
                                                Text(
                                                    label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (selectedDensity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Box(
                                                    Modifier
                                                        .size(if (selectedDensity) 28.dp else 24.dp)
                                                        .clip(CircleShape)
                                                        .background(if (selectedDensity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                                        .clickable { vm.setItemDensity(densityOption) },
                                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                                ) {
                                                    if (selectedDensity) {
                                                        Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.fab_selected, label), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        FloatingActionButton(
                            onClick = { onLayoutMenuExpanded(!layoutMenuExpanded); onQuickCreateExpanded(false) },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ) {
                            Icon(
                                imageVector = if (ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) Icons.Rounded.GridView else Icons.Rounded.ViewList,
                                contentDescription = stringResource(R.string.fab_layout_and_size),
                                modifier = Modifier.graphicsLayer { scaleX = layoutIconScale; scaleY = layoutIconScale },
                            )
                        }
                    }
                }
            
                }
                FloatingAction.PANE_MODE -> {
                FloatingActionButton(
                    onClick = {
                        if (!dual && ui.settings.list.viewMode == ir.solina.files.core.model.ViewMode.GRID) vm.toggleViewMode()
                        vm.setDualPanePreferred(!dual)
                        onQuickCreateExpanded(false)
                        onLayoutMenuExpanded(false)
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = if (dual) Icons.Rounded.ViewColumn else Icons.Rounded.ViewAgenda,
                        contentDescription = if (dual) stringResource(R.string.fab_single_pane) else stringResource(R.string.fab_dual_pane),
                    )
                }
            
                }
                }
            }
        }
    }
}
