package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hello.data.CampusLocation
import edu.nd.pmcburne.hello.data.PlacemarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class MainUiState(
    val selectedTag: String = "core",
    val tags: List<String> = emptyList(),
    val locations: List<CampusLocation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlacemarkRepository =
        AppContainer.getRepository(application.applicationContext)

    private val _selectedTag = MutableStateFlow("core")
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val selectedTag: StateFlow<String> = _selectedTag.asStateFlow()

    private val tagsFlow: StateFlow<List<String>> = repository.observeAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val locationsFlow: StateFlow<List<CampusLocation>> = _selectedTag
        .flatMapLatest { tag -> repository.observePlacemarksForTag(tag) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<MainUiState> = combine(
        _selectedTag,
        tagsFlow,
        locationsFlow,
        _isLoading,
        _errorMessage
    ) { selectedTag, tags, locations, isLoading, errorMessage ->
        MainUiState(
            selectedTag = selectedTag,
            tags = tags,
            locations = locations,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        observeTagValidity()
        synchronize()
    }

    fun onTagSelected(tag: String) {
        _selectedTag.value = tag
    }

    private fun observeTagValidity() {
        viewModelScope.launch {
            tagsFlow.collect { tags ->
                if (tags.isNotEmpty() && _selectedTag.value !in tags) {
                    _selectedTag.value = if ("core" in tags) "core" else tags.first()
                }
            }
        }
    }

    private fun synchronize() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            runCatching {
                repository.synchronizeFromApi()
            }.onFailure { throwable ->
                _errorMessage.update {
                    throwable.message ?: "Failed to synchronize placemarks."
                }
            }

            _isLoading.value = false
        }
    }
}