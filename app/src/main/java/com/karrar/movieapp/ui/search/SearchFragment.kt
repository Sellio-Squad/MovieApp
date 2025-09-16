package com.karrar.movieapp.ui.search

import android.content.Context
import android.os.Bundle
import android.transition.ChangeTransform
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override val layoutIdFragment: Int = R.layout.fragment_search
    override val viewModel: SearchViewModel by viewModels()

    private val mediaSearchAdapter by lazy { MediaSearchAdapter(viewModel) }
    private val mediaSearchCardAdapter by lazy { MediaSearchCardAdapter(viewModel) }
    private val gridMediaAdapter by lazy { GridMediaAdapter(viewModel) }
    private val actorSearchAdapter by lazy { ActorSearchAdapter(viewModel) }

    private val oldValue = MutableStateFlow(MediaSearchUIState())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = ChangeTransform()
        setTitle(false)
        setupTabs()
        setupToggleButton()
        setSearchHistoryAdapter()
        getSearchResultsBySearchTerm()

        // Observe UI state changes
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
            override fun onTabReselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.onSearchForMovie()
                    1 -> viewModel.onSearchForSeries()
                    2 -> viewModel.onSearchForActor()
                }
            }
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
                // Show search history/suggestions
                binding.recyclerSearchHistory.visibility = View.VISIBLE
                binding.layoutHistory.visibility = View.VISIBLE
                binding.recyclerMedia.visibility = View.GONE
                binding.tabLayoutMediaType.visibility = View.GONE
                binding.toggleViewMode.visibility = View.GONE
            }
            SearchDisplayMode.RESULTS -> {
                // Show search results
                binding.recyclerSearchHistory.visibility = View.GONE
                binding.layoutHistory.visibility = View.GONE
                binding.recyclerMedia.visibility = View.VISIBLE
                binding.tabLayoutMediaType.visibility = if (state.searchInput.isNotBlank()) View.VISIBLE else View.GONE

                // Show toggle only for movie/tv shows, not actors
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
        val indicatorMargin = if (isGridSelected) 0 else 40 // 40dp for moving to the right
        val layoutParams = binding.indicator.layoutParams as android.widget.FrameLayout.LayoutParams
        layoutParams.marginStart = (indicatorMargin * resources.displayMetrics.density).toInt()
        binding.indicator.layoutParams = layoutParams
    }

    private fun setSearchHistoryAdapter() {
        val inputMethodManager =
            binding.inputSearch.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.inputSearch, InputMethodManager.SHOW_IMPLICIT)

        binding.recyclerSearchHistory.adapter = SearchHistoryAdapter(mutableListOf(), viewModel)
    }

    @OptIn(FlowPreview::class)
    private fun getSearchResultsBySearchTerm() {
        lifecycleScope.launch {
            viewModel.uiState
                .debounce(500)
                .collectLatest { newState ->
                    val oldState = oldValue.value

                    // Only update when we're in results mode and something relevant changed
                    val shouldUpdate = newState.displayMode == SearchDisplayMode.RESULTS &&
                            newState.searchInput.isNotBlank() &&
                            (oldState.searchInput != newState.searchInput ||
                                    oldState.searchTypes != newState.searchTypes ||
                                    oldState.viewMode != newState.viewMode)

                    if (shouldUpdate) {
                        updateRecyclerView(newState)
                        oldValue.emit(newState)
                    }
                }
        }
    }

    private fun updateRecyclerView(state: MediaSearchUIState) {
        when (state.searchTypes) {
            MediaTypes.ACTOR -> bindActors()
            else -> {
                when (state.viewMode) {
                    ViewMode.LIST -> bindMediaList()
                    ViewMode.GRID -> bindMediaGrid()
                }
            }
        }
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

    private fun bindMediaList() {
        val footerAdapter = LoadUIStateAdapter(mediaSearchCardAdapter::retry)
        binding.recyclerMedia.adapter = mediaSearchCardAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager =
            LinearLayoutManager(this@SearchFragment.context, RecyclerView.VERTICAL, false)

        collect(
            flow = mediaSearchCardAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, mediaSearchCardAdapter.itemCount) })

        // Collect the search results and submit to adapter
        lifecycleScope.launch {
            viewModel.uiState.value.searchResult.collectLatest { pagingData ->
                mediaSearchCardAdapter.submitData(pagingData)
            }
        }
    }

    private fun bindMediaGrid() {
        val footerAdapter = LoadUIStateAdapter(gridMediaAdapter::retry)
        binding.recyclerMedia.adapter = gridMediaAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 2)
        setSpanSizeForGrid(footerAdapter)

        collect(flow = gridMediaAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, gridMediaAdapter.itemCount) })

        // Collect the search results and submit to adapter
        lifecycleScope.launch {
            viewModel.uiState.value.searchResult.collectLatest { pagingData ->
                gridMediaAdapter.submitData(pagingData)
            }
        }
    }

    private fun bindActors() {
        val footerAdapter = LoadUIStateAdapter(actorSearchAdapter::retry)
        binding.recyclerMedia.adapter = actorSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 3)
        setSpanSizeForActors(footerAdapter)

        collect(
            flow = actorSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, actorSearchAdapter.itemCount) })

        // Collect the search results and submit to adapter
        lifecycleScope.launch {
            viewModel.uiState.value.searchResult.collectLatest { pagingData ->
                actorSearchAdapter.submitData(pagingData)
            }
        }
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
}