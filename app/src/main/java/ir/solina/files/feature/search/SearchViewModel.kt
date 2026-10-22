package ir.solina.files.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.solina.files.core.search.NameMatchMode
import ir.solina.files.core.search.SearchEngine
import ir.solina.files.core.search.SearchEvent
import ir.solina.files.core.search.SearchQuery
import ir.solina.files.core.search.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TypeFilter { ALL, FILES, DIRS }

data class SearchUiState(
    val root: String = "",
    val namePattern: String = "",
    val nameMode: NameMatchMode = NameMatchMode.GLOB,
    val content: String = "",
    val contentRegex: Boolean = false,
    val caseSensitive: Boolean = false,
    val includeSubdirs: Boolean = true,
    val includeHidden: Boolean = false,
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val running: Boolean = false,
    val scanned: Int = 0,
    val results: List<SearchResult> = emptyList(),
    val finished: Boolean = false,
    val stoppedEarly: Boolean = false,
)

class SearchViewModel : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun initRoot(root: String) {
        if (_state.value.root.isEmpty()) _state.update { it.copy(root = root) }
    }

    fun setNamePattern(v: String) = _state.update { it.copy(namePattern = v) }
    fun setNameMode(v: NameMatchMode) = _state.update { it.copy(nameMode = v) }
    fun setContent(v: String) = _state.update { it.copy(content = v) }
    fun setContentRegex(v: Boolean) = _state.update { it.copy(contentRegex = v) }
    fun setCaseSensitive(v: Boolean) = _state.update { it.copy(caseSensitive = v) }
    fun setIncludeSubdirs(v: Boolean) = _state.update { it.copy(includeSubdirs = v) }
    fun setIncludeHidden(v: Boolean) = _state.update { it.copy(includeHidden = v) }
    fun setTypeFilter(v: TypeFilter) = _state.update { it.copy(typeFilter = v) }

    fun start() {
        val s = _state.value
        if (s.root.isEmpty()) return
        job?.cancel()
        val query = SearchQuery(
            root = s.root,
            namePattern = s.namePattern.trim(),
            nameMode = s.nameMode,
            caseSensitive = s.caseSensitive,
            includeSubdirs = s.includeSubdirs,
            includeHidden = s.includeHidden,
            onlyFiles = s.typeFilter == TypeFilter.FILES,
            onlyDirs = s.typeFilter == TypeFilter.DIRS,
            content = s.content,
            contentRegex = s.contentRegex,
        )
        _state.update { it.copy(running = true, results = emptyList(), scanned = 0, finished = false, stoppedEarly = false) }
        job = viewModelScope.launch {
            val acc = ArrayList<SearchResult>()
            SearchEngine.search(query).collect { event ->
                when (event) {
                    is SearchEvent.Progress -> _state.update { it.copy(scanned = event.scanned) }
                    is SearchEvent.Found -> {
                        acc.add(event.result)
                        if (acc.size <= 20 || acc.size % 20 == 0) {
                            _state.update { it.copy(results = acc.toList()) }
                        }
                    }
                    is SearchEvent.Finished -> _state.update {
                        it.copy(results = acc.toList(), running = false, finished = true, stoppedEarly = event.stoppedEarly)
                    }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        _state.update { it.copy(running = false, finished = true) }
    }

    override fun onCleared() {
        job?.cancel()
    }
}
