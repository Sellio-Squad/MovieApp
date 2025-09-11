package com.karrar.movieapp.ui.search

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.transition.ChangeTransform
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.material.tabs.TabLayout
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSearchBinding
import com.karrar.movieapp.ui.adapters.LoadUIStateAdapter
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.base.BasePagingAdapter
import com.karrar.movieapp.ui.search.adapters.*
import com.karrar.movieapp.ui.search.mediaSearchUIState.*
import com.karrar.movieapp.utilities.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce


@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override val layoutIdFragment: Int = R.layout.fragment_search
    override val viewModel: SearchViewModel by viewModels()

    private val mediaSearchAdapter by lazy { MediaSearchAdapter(viewModel) }
    private val mediaSearchGridAdapter by lazy { MediaSearchGridAdapter(viewModel) }
    private val actorSearchAdapter by lazy { ActorSearchAdapter(viewModel) }

    private val oldValue = MutableStateFlow(MediaSearchUIState())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = ChangeTransform()
        setTitle(false)
        setupTabLayout()
        setupToggleSwitch()
        getSearchResult()
        setSearchHistoryAdapter()
        getSearchResultsBySearchTerm()
        collectLast(viewModel.searchUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun setupTabLayout() {
        binding.tabLayoutMediaType.selectTab(binding.tabLayoutMediaType.getTabAt(0))

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

    private fun setupToggleSwitch() {
        binding.btnGridView.setOnClickListener {
            viewModel.onGridViewSelected()
            animateToggleIndicator(true)
        }

        binding.btnListView.setOnClickListener {
            viewModel.onListViewSelected()
            animateToggleIndicator(false)
        }

        // Initialize toggle position
        animateToggleIndicator(viewModel.uiState.value.isGridMode)
    }

    private fun animateToggleIndicator(isGridMode: Boolean) {
        val targetPosition = if (isGridMode) 0f else 40f
        ObjectAnimator.ofFloat(binding.indicator, "translationX", targetPosition).apply {
            duration = 200
            start()
        }
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
            viewModel.uiState.debounce(500).collectLatest { searchTerm ->
                if (searchTerm.searchInput.isNotBlank()
                    && oldValue.value.searchInput != viewModel.uiState.value.searchInput
                    || oldValue.value.searchTypes != viewModel.uiState.value.searchTypes) {
                    getSearchResult()
                    oldValue.emit(viewModel.uiState.value)
                }
            }
        }
    }

    private fun getSearchResult() {
        when (viewModel.uiState.value.searchTypes) {
            MediaTypes.ACTOR -> {
                bindActors()
            }
            else -> {
                bindMedia()
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
                when (event.mediaUIState.mediaTypes) {
                    Constants.MOVIE -> navigateToMovieDetails(event.mediaUIState.mediaID)
                    Constants.TV_SHOWS -> navigateToSeriesDetails(event.mediaUIState.mediaID)
                }
            }
            SearchUIEvent.ClickRetryEvent -> {
                actorSearchAdapter.retry()
                mediaSearchAdapter.retry()
                mediaSearchGridAdapter.retry()
            }
            SearchUIEvent.ToggleViewModeEvent -> {
                getSearchResult() // Refresh the adapter based on new view mode
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

    private fun bindMedia() {
        val currentAdapter = if (viewModel.uiState.value.isGridMode) {
            mediaSearchGridAdapter
        } else {
            mediaSearchAdapter
        }

        val footerAdapter = LoadUIStateAdapter(currentAdapter::retry)
        binding.recyclerMedia.adapter = currentAdapter.withLoadStateFooter(footerAdapter)

        if (viewModel.uiState.value.isGridMode) {
            binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 3)
            setSpanSizeForGrid(footerAdapter)
        } else {
            binding.recyclerMedia.layoutManager =
                LinearLayoutManager(this@SearchFragment.context, RecyclerView.VERTICAL, false)
        }

        collect(flow = currentAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, currentAdapter.itemCount) })

        getMediaSearchResults(currentAdapter)
    }

    private fun bindActors() {
        val footerAdapter = LoadUIStateAdapter(actorSearchAdapter::retry)
        binding.recyclerMedia.adapter = actorSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 3)
        setSpanSizeForActors(footerAdapter)

        collect(flow = actorSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, actorSearchAdapter.itemCount) })

        getActorsSearchResults()
    }

    private fun getMediaSearchResults(adapter: BasePagingAdapter<MediaUIState>) {
        collectLast(viewModel.uiState.value.searchResult)
        { adapter.submitData(it) }
    }

    private fun getActorsSearchResults() {
        collectLast(viewModel.uiState.value.searchResult)
        { actorSearchAdapter.submitData(it) }
    }

    private fun setSpanSizeForGrid(footerAdapter: LoadUIStateAdapter) {
        val mManager = binding.recyclerMedia.layoutManager as GridLayoutManager
        mManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position == mediaSearchGridAdapter.itemCount)
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