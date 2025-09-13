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
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSearchBinding
import com.karrar.movieapp.ui.adapters.LoadUIStateAdapter
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.search.adapters.ActorSearchAdapter
import com.karrar.movieapp.ui.search.adapters.MediaSearchAdapter
import com.karrar.movieapp.ui.search.adapters.SearchHistoryAdapter
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaSearchUIState
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaTypes
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
    private val actorSearchAdapter by lazy { ActorSearchAdapter(viewModel) }

    private val oldValue = MutableStateFlow(MediaSearchUIState())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = ChangeTransform()
        setTitle(false)
        getSearchResult()
        setSearchHistoryAdapter()
        getSearchResultsBySearchTerm()
        collectLast(viewModel.searchUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
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
            viewModel.uiState
                .debounce(500)
                .collectLatest { newState ->
                    val oldState = oldValue.value

                    val shouldUpdate = newState.searchInput.isNotBlank() &&
                            (oldState.searchInput != newState.searchInput ||
                                    oldState.searchTypes != newState.searchTypes ||
                                    oldState.displayMode != newState.displayMode)

                    if (shouldUpdate) {
                        getSearchResult()
                        oldValue.emit(newState)
                    }
                }
        }
    }

    private fun getSearchResult() {
        when (viewModel.uiState.value.displayMode) {
            SearchDisplayMode.SUGGESTIONS -> {
                when (viewModel.uiState.value.searchTypes) {
                    MediaTypes.ACTOR -> bindActors()
                    else -> bindMedia()
                }
            }
            SearchDisplayMode.RESULTS -> {
                bindMediaCard()
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
        val footerAdapter = LoadUIStateAdapter(mediaSearchAdapter::retry)
        binding.recyclerMedia.adapter = mediaSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager =
            LinearLayoutManager(this@SearchFragment.context, RecyclerView.VERTICAL, false)

        collect(
            flow = mediaSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, mediaSearchAdapter.itemCount) })

        getMediaSearchResults()
    }
    private fun bindMediaCard() {
        val footerAdapter = LoadUIStateAdapter(mediaSearchAdapter::retry)
        binding.recyclerMedia.adapter = mediaSearchCardAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager =
            LinearLayoutManager(this@SearchFragment.context, RecyclerView.VERTICAL, false)

        collect(flow = mediaSearchCardAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, mediaSearchCardAdapter.itemCount) })

        getMediaSearchCardResults()
    }

    private fun bindActors() {
        val footerAdapter = LoadUIStateAdapter(actorSearchAdapter::retry)
        binding.recyclerMedia.adapter = actorSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 3)
        setSpanSize(footerAdapter)

        collect(
            flow = actorSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, actorSearchAdapter.itemCount) })

        getActorsSearchResults()
    }

    private fun getMediaSearchResults() {
        collectLast(viewModel.uiState.value.searchResult)
        { mediaSearchAdapter.submitData(it) }
    }
    private fun getMediaSearchCardResults() {
        collectLast(viewModel.uiState.value.searchResult)
        { mediaSearchCardAdapter.submitData(it) }
    }

    private fun getActorsSearchResults() {
        collectLast(viewModel.uiState.value.searchResult)
        { actorSearchAdapter.submitData(it) }
    }

    private fun setSpanSize(footerAdapter: LoadUIStateAdapter) {
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