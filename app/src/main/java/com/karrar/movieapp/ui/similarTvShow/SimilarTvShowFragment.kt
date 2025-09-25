package com.karrar.movieapp.ui.similarTvShow

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSimilarDetailsBinding
import com.karrar.movieapp.domain.enums.TvShowItemsType
import com.karrar.movieapp.ui.adapters.LoadUIStateAdapter
import com.karrar.movieapp.ui.adapters.TvShowDetailsInteractionListener
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.explore.exploreUIState.ViewMode
import com.karrar.movieapp.ui.models.MediaUiState
import com.karrar.movieapp.utilities.collect
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimilarTvShowFragment : BaseFragment<FragmentSimilarDetailsBinding>(),
    TvShowDetailsInteractionListener {

    override val layoutIdFragment: Int = R.layout.fragment_similar_details
    override val viewModel: SimilarTvShowViewModel by viewModels()

    private val listAdapter by lazy { SimilarListAdapter(this) }
    private val gridAdapter by lazy { SimilarGridAdapter(this) }

    private val gridAdapterWithFooter by lazy {
        gridAdapter.withLoadStateFooter(LoadUIStateAdapter { gridAdapter.retry() })
    }

    private val listAdapterWithFooter by lazy {
        listAdapter.withLoadStateFooter(LoadUIStateAdapter { listAdapter.retry() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.textView.text = getString(R.string.because_you_watched)

        initRecyclerView()
        setupToggleButton()
        collectEvent()
        observeViewMode()
        observeSimilarTvShows()
    }

    private fun initRecyclerView() {
        binding.recyclerMedia.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = gridAdapterWithFooter
            //gridAdapter.withLoadStateFooter(LoadUIStateAdapter(gridAdapter::retry))
        }
    }

    private fun observeViewMode() {
        collect(viewModel.uiState) { state ->
            updateLayoutManager(state.viewMode)
            updateToggleIndicator(state.viewMode == ViewMode.GRID)

//            when (state.viewMode) {
//                ViewMode.GRID -> gridAdapter.submitList(state.similarTvShowResult)
//                ViewMode.LIST -> listAdapter.submitList(state.similarTvShowResult)
//            }
        }
    }

    private fun observeSimilarTvShows() {
        collect(viewModel.uiState) { state ->
//            when (state.viewMode) {
//                ViewMode.GRID -> gridAdapter.submitList(state.similarTvShowResult)
//                ViewMode.LIST -> listAdapter.submitList(state.similarTvShowResult)
//            }
        }
    }

    private fun updateLayoutManager(viewMode: ViewMode) {
        val layoutManager = when (viewMode) {
            ViewMode.GRID -> GridLayoutManager(requireContext(), 2)
            ViewMode.LIST -> LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        if (binding.recyclerMedia.layoutManager!!::class != layoutManager::class) {
            binding.recyclerMedia.layoutManager = layoutManager
        }

        val newAdapter = when (viewMode) {
            ViewMode.GRID -> gridAdapterWithFooter
            ViewMode.LIST -> listAdapterWithFooter
        }

        if (binding.recyclerMedia.adapter != newAdapter) {
            binding.recyclerMedia.adapter = newAdapter
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


    private fun setupToggleButton() {
        binding.btnGridView.setOnClickListener {
            viewModel.setViewMode(ViewMode.GRID)
        }

        binding.btnListView.setOnClickListener {
            viewModel.setViewMode(ViewMode.LIST)
        }
    }

    private fun collectEvent() {
        collectLast(viewModel.SimilarTvShowUIEvent) {
            it?.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: SimilarTvShowUIEvent) {
        when (event) {
            is SimilarTvShowUIEvent.ClickTvShowEvent -> {
                findNavController().navigate(
                    SimilarTvShowFragmentDirections.actionSimilarTvShowFragmentToTvShowDetailsFragment(
                        event.tvShowID
                    )
                )
            }
        }
    }

    override fun onClickTvShow(item: MediaUiState) {
        viewModel.onClickTvShow(item.id)
    }

    override fun onClickSeeAllTvShows(tvShowItemsType: TvShowItemsType) {
        TODO("Not yet implemented")
    }
}