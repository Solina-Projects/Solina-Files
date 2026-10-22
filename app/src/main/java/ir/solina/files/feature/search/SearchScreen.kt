package ir.solina.files.feature.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.solina.files.R
import ir.solina.files.core.model.FileItem
import ir.solina.files.core.search.NameMatchMode
import ir.solina.files.core.search.SearchResult
import ir.solina.files.ui.asPath
import ir.solina.files.ui.expressive.ArtTone
import ir.solina.files.ui.expressive.ExpressiveEmptyState
import ir.solina.files.ui.isolated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    root: String,
    onBack: () -> Unit,
    onOpenItem: (FileItem) -> Unit,
    vm: SearchViewModel = viewModel(),
) {
    LaunchedEffect(root) { vm.initRoot(root) }
    val ui by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                title = {
                    Column {
                        Text(stringResource(R.string.common_search))
                        Text(
                            ui.root,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QueryForm(ui, vm)
            StatusBar(ui, vm)
            HorizontalDivider()
            if (ui.finished && ui.results.isEmpty()) {
                ExpressiveEmptyState(
                    stringResource(R.string.search_nothing_found),
                    Modifier.fillMaxSize().padding(24.dp),
                    icon = Icons.Rounded.SearchOff,
                    tone = ArtTone.SECONDARY,
                    body = stringResource(R.string.search_nothing_found_body),
                    maxHero = 136.dp,
                )
            } else {
                ResultsList(ui.results, onOpenItem)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryForm(ui: SearchUiState, vm: SearchViewModel) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ui.namePattern,
                onValueChange = vm::setNamePattern,
                label = { Text(stringResource(R.string.search_file_name)) },
                placeholder = { Text(stringResource(R.string.search_file_name_hint)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                modifier = Modifier.fillMaxWidth(),
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                NameMatchMode.entries.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = ui.nameMode == mode,
                        onClick = { vm.setNameMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(i, NameMatchMode.entries.size),
                    ) { Text(stringResource(mode.label)) }
                }
            }
            OutlinedTextField(
                value = ui.content,
                onValueChange = vm::setContent,
                label = { Text(stringResource(R.string.search_contains_text)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(ui.contentRegex, { vm.setContentRegex(!ui.contentRegex) }, label = { Text(stringResource(R.string.search_mode_regex)) })
                FilterChip(ui.caseSensitive, { vm.setCaseSensitive(!ui.caseSensitive) }, label = { Text(stringResource(R.string.search_case)) })
                FilterChip(ui.includeSubdirs, { vm.setIncludeSubdirs(!ui.includeSubdirs) }, label = { Text(stringResource(R.string.search_subdirs)) })
                FilterChip(ui.includeHidden, { vm.setIncludeHidden(!ui.includeHidden) }, label = { Text(stringResource(R.string.search_hidden)) })
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TypeFilter.entries.forEach { t ->
                    FilterChip(
                        selected = ui.typeFilter == t,
                        onClick = { vm.setTypeFilter(t) },
                        label = { Text(stringResource(t.label)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBar(ui: SearchUiState, vm: SearchViewModel) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (ui.running) {
            OutlinedButton(onClick = vm::stop) {
                Icon(Icons.Rounded.Stop, contentDescription = null)
                Text(stringResource(R.string.common_stop))
            }
            Text(stringResource(R.string.search_scanned, ui.scanned, ui.results.size), style = MaterialTheme.typography.bodySmall)
        } else {
            Button(onClick = vm::start, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.Search, contentDescription = null)
                Text(stringResource(R.string.common_search))
            }
            if (ui.finished) {
                val summary = if (ui.stoppedEarly) R.string.search_results_limit else R.string.search_results
                Text(stringResource(summary, ui.results.size), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (ui.running) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
private fun ResultsList(results: List<SearchResult>, onOpenItem: (FileItem) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
        items(results, key = { it.item.path }) { result ->
            ResultRow(result, onOpenItem)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultRow(result: SearchResult, onOpenItem: (FileItem) -> Unit) {
    Card(
        onClick = { onOpenItem(result.item) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(isolated(result.item.name), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                result.item.parentPath ?: result.item.path,
                style = MaterialTheme.typography.bodySmall.asPath(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (result.contentHits.isNotEmpty()) {
                val first = result.contentHits.first()
                Text(
                    stringResource(R.string.search_hit, result.contentHits.size, first.line, first.preview),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private val NameMatchMode.label: Int
    get() = when (this) {
        NameMatchMode.GLOB -> R.string.search_mode_glob
        NameMatchMode.REGEX -> R.string.search_mode_regex
        NameMatchMode.PLAIN -> R.string.search_mode_plain
    }

private val TypeFilter.label: Int
    get() = when (this) {
        TypeFilter.ALL -> R.string.search_type_all
        TypeFilter.FILES -> R.string.search_type_files
        TypeFilter.DIRS -> R.string.search_type_dirs
    }
