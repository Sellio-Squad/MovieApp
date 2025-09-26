package com.karrar.movieapp.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.transition.ChangeTransform
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSearchBinding
import com.karrar.movieapp.ui.adapters.LoadUIStateAdapter
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.search.adapters.ActorSearchAdapter
import com.karrar.movieapp.ui.search.adapters.GridMediaAdapter
import com.karrar.movieapp.ui.search.adapters.MediaSearchAdapter
import com.karrar.movieapp.ui.search.adapters.MediaSearchCardAdapter
import com.karrar.movieapp.ui.search.adapters.SearchHistoryAdapter
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaSearchUIState
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaTypes
import com.karrar.movieapp.ui.search.mediaSearchUIState.ViewMode
import com.karrar.movieapp.ui.search.mediaSearchUIState.SearchDisplayMode
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.collect
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override val layoutIdFragment: Int = R.layout.fragment_search
    override val viewModel: SearchViewModel by viewModels()
    private lateinit var voiceLauncher: ActivityResultLauncher<Intent>

    private val mediaSearchAdapter by lazy { MediaSearchAdapter(viewModel) }
    private val mediaSearchCardAdapter by lazy { MediaSearchCardAdapter(viewModel) }
    private val gridMediaAdapter by lazy { GridMediaAdapter(viewModel) }
    private val actorSearchAdapter by lazy { ActorSearchAdapter(viewModel) }

    private val oldValue = MutableStateFlow(MediaSearchUIState())

    private var currentAdapterType: AdapterType = AdapterType.NONE
    private var searchResultJob: Job? = null

    private enum class AdapterType {
        NONE, MEDIA_LIST, MEDIA_GRID, ACTOR,MEDIA_SUGGESTION
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedElementEnterTransition = ChangeTransform()
        setTitle(false)
        setupTabs()
        setupToggleButton()
        setSearchHistoryAdapter()
        setupVoiceSearch()
        observeUIStateChanges()

        collect(viewModel.uiState) { state ->

            updateToggleVisibility(state.searchTypes)
            updateToggleIndicator(state.viewMode == ViewMode.GRID)
            updateViewVisibility(state)
        }

        collectLast(viewModel.searchUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun setupTabs() {
        binding.tabLayoutMediaType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.onSearchForMovie()
                    1 -> viewModel.onSearchForSeries()
                    2 -> viewModel.onSearchForActor()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupToggleButton() {
        binding.btnGridView.setOnClickListener {
            viewModel.setViewMode(ViewMode.GRID)
        }

        binding.btnListView.setOnClickListener {
            viewModel.setViewMode(ViewMode.LIST)
        }
    }

    private fun updateViewVisibility(state: MediaSearchUIState) {
        when (state.displayMode) {
            SearchDisplayMode.SUGGESTIONS -> {
                binding.recyclerSearchHistory.visibility = View.GONE
                binding.layoutHistory.visibility = View.VISIBLE
                binding.recyclerMedia.visibility = View.GONE
                binding.tabLayoutMediaType.visibility = View.GONE
                binding.toggleViewMode.visibility = View.GONE
            }
            SearchDisplayMode.RESULTS -> {
                binding.recyclerSearchHistory.visibility = View.GONE
                binding.layoutHistory.visibility = View.GONE
                binding.recyclerMedia.visibility = View.VISIBLE
                binding.tabLayoutMediaType.visibility = if (state.searchInput.isNotBlank()) View.VISIBLE else View.GONE
                binding.toggleViewMode.visibility = if (state.searchTypes == MediaTypes.ACTOR) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateToggleVisibility(searchType: MediaTypes) {
        binding.toggleViewMode.visibility = if (searchType == MediaTypes.ACTOR) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun updateToggleIndicator(isGridSelected: Boolean) {
        val indicatorMargin = if (isGridSelected) 0 else 40
        val layoutParams = binding.indicator.layoutParams as android.widget.FrameLayout.LayoutParams
        layoutParams.marginStart = (indicatorMargin * resources.displayMetrics.density).toInt()
        binding.indicator.layoutParams = layoutParams

        binding.btnGridView.isSelected = isGridSelected
        binding.btnListView.isSelected = !isGridSelected
    }

    private fun setSearchHistoryAdapter() {
        val inputMethodManager =
            binding.inputSearch.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.inputSearch, InputMethodManager.SHOW_IMPLICIT)
        binding.recyclerSearchHistory.adapter = SearchHistoryAdapter(mutableListOf(), viewModel)
    }

    @OptIn(FlowPreview::class)
    private fun observeUIStateChanges() {
        lifecycleScope.launch {
            viewModel.uiState
                .debounce(100)
                .collectLatest { newState ->
                    val oldState = oldValue.value
                    if (newState.searchInput.isBlank()) {
                        oldValue.emit(newState)
                        return@collectLatest
                    }
                    val shouldUpdate = oldState.searchInput != newState.searchInput ||
                            oldState.searchTypes != newState.searchTypes ||
                            oldState.viewMode != newState.viewMode ||
                            oldState.displayMode != newState.displayMode


                    if (shouldUpdate) {
                        if (newState.displayMode == SearchDisplayMode.SUGGESTIONS) {
                            setupSuggestionAdapter()
                            restartSearchResultCollection()
                        } else {
                            updateRecyclerView(newState)
                        }
                        oldValue.emit(newState)
                    }
                }
        }
    }
    private fun setupSuggestionAdapter() {
        val footerAdapter = LoadUIStateAdapter(mediaSearchAdapter::retry)
        binding.recyclerMedia.adapter = mediaSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        collect(
            flow = mediaSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, mediaSearchAdapter.itemCount) })
        currentAdapterType = AdapterType.MEDIA_SUGGESTION
    }

    private fun updateRecyclerView(state: MediaSearchUIState) {
        val requiredAdapterType = when (state.searchTypes) {
            MediaTypes.ACTOR -> AdapterType.ACTOR
            else -> when (state.viewMode) {
                ViewMode.LIST -> AdapterType.MEDIA_LIST
                ViewMode.GRID -> AdapterType.MEDIA_GRID
            }
        }

        if (currentAdapterType != requiredAdapterType) {
            when (requiredAdapterType) {
                AdapterType.ACTOR -> setupActorAdapter()
                AdapterType.MEDIA_LIST -> setupMediaListAdapter()
                AdapterType.MEDIA_GRID -> setupMediaGridAdapter()
                AdapterType.MEDIA_SUGGESTION -> setupSuggestionAdapter()
                AdapterType.NONE -> {}
            }
            currentAdapterType = requiredAdapterType
        }
        restartSearchResultCollection()
    }

    private fun restartSearchResultCollection() {
        searchResultJob?.cancel()
        searchResultJob = lifecycleScope.launch {
            viewModel.uiState.value.searchResult.collectLatest { pagingData ->
                when (currentAdapterType) {
                    AdapterType.MEDIA_LIST -> mediaSearchCardAdapter.submitData(pagingData)
                    AdapterType.MEDIA_SUGGESTION -> mediaSearchAdapter.submitData(pagingData)
                    AdapterType.MEDIA_GRID -> gridMediaAdapter.submitData(pagingData)
                    AdapterType.ACTOR -> actorSearchAdapter.submitData(pagingData)
                    AdapterType.NONE -> {}
                }
            }
        }
    }
    private fun setupVoiceSearch() {
        setupVoiceLauncher()
        setupMicClickListener()
    }

    private fun setupVoiceLauncher() {
        voiceLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val voiceText = result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                    .orEmpty()

                binding.inputSearch.setText(voiceText)
            }
        }
    }

    private fun setupMicClickListener() {
        binding.inputSearch.setOnTouchListener { view, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP &&
                isClickOnDrawableEnd(event.rawX)
            ) {
                startVoiceSearch()
                view.performClick()
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun isClickOnDrawableEnd(touchX: Float): Boolean {
        val drawableEndIndex = 2
        val drawableEnd = binding.inputSearch.compoundDrawables[drawableEndIndex]
        return drawableEnd != null &&
                touchX >= binding.inputSearch.right - drawableEnd.bounds.width()
    }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search")
        }

        runCatching { voiceLauncher.launch(intent) }
            .onFailure {
                Toast.makeText(requireContext(), "Voice search not supported", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupMediaListAdapter() {
        val footerAdapter = LoadUIStateAdapter(mediaSearchCardAdapter::retry)
        binding.recyclerMedia.adapter = mediaSearchCardAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager =
            LinearLayoutManager(this@SearchFragment.context, RecyclerView.VERTICAL, false)
        collect(
            flow = mediaSearchCardAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, mediaSearchCardAdapter.itemCount) })
    }

    private fun setupMediaGridAdapter() {
        val footerAdapter = LoadUIStateAdapter(gridMediaAdapter::retry)
        binding.recyclerMedia.adapter = gridMediaAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 2)
        setSpanSizeForGrid(footerAdapter)
        collect(flow = gridMediaAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, gridMediaAdapter.itemCount) })
    }

    private fun setupActorAdapter() {
        val footerAdapter = LoadUIStateAdapter(actorSearchAdapter::retry)
        binding.recyclerMedia.adapter = actorSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 3)
        setSpanSizeForActors(footerAdapter)
        collect(
            flow = actorSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, actorSearchAdapter.itemCount) })
    }

    private fun onEvent(event: SearchUIEvent) {
        when (event) {
            is SearchUIEvent.ClickActorEvent -> {
                navigateToActorDetails(event.actorID)
            }
            SearchUIEvent.ClickBackEvent -> {
                popFragment()
            }
            is SearchUIEvent.ClickMediaEvent -> {
                when (event.mediaUIState.mediaTypes.lowercase()) {
                    Constants.MOVIE -> navigateToMovieDetails(event.mediaUIState.mediaID)
                    Constants.TV_SHOWS -> navigateToSeriesDetails(event.mediaUIState.mediaID)
                }
            }
            SearchUIEvent.ClickRetryEvent -> {
                actorSearchAdapter.retry()
                mediaSearchAdapter.retry()
                gridMediaAdapter.retry()
                mediaSearchCardAdapter.retry()
            }
        }
    }

    private fun navigateToMovieDetails(movieId: Int) {
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToMovieDetailFragment(
                movieId
            )
        )
    }

    private fun navigateToSeriesDetails(seriesId: Int) {
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToTvShowDetailsFragment(
                seriesId
            )
        )
    }

    private fun navigateToActorDetails(actorId: Int) {
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToActorDetailsFragment(
                actorId
            )
        )
    }

    private fun setSpanSizeForGrid(footerAdapter: LoadUIStateAdapter) {
        val mManager = binding.recyclerMedia.layoutManager as GridLayoutManager
        mManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position == gridMediaAdapter.itemCount)
                    && footerAdapter.itemCount > 0
                ) {
                    mManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun setSpanSizeForActors(footerAdapter: LoadUIStateAdapter) {
        val mManager = binding.recyclerMedia.layoutManager as GridLayoutManager
        mManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position == actorSearchAdapter.itemCount)
                    && footerAdapter.itemCount > 0
                ) {
                    mManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun popFragment() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchResultJob?.cancel()
    }
}