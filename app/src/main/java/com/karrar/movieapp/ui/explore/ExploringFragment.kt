package com.karrar.movieapp.ui.explore

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.transition.TransitionInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentExploringBinding
import com.karrar.movieapp.ui.adapters.LoadUIStateAdapter
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.explore.adapter.CategoryListAdapter
import com.karrar.movieapp.ui.explore.exploreUIState.ExploringUIEvent
import com.karrar.movieapp.ui.explore.exploreUIState.ViewMode
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.collect
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class ExploringFragment : BaseFragment<FragmentExploringBinding>(), CategoryInteractionListener {

    override val layoutIdFragment: Int = R.layout.fragment_exploring
    override val viewModel: ExploringViewModel by viewModels()

    private val listAdapter by lazy { CategoryListAdapter(this) }
    private val gridAdapter by lazy { CategoryAdapter(this) }

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

        initRecyclerView()
        setupToggleButton()
        setupTabLayout()
        collectEvent()
        collectUIState()
        observeViewMode()
        observeTabState()
    }

    private fun initRecyclerView() {
        binding.recyclerMedia.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = gridAdapter.withLoadStateFooter(LoadUIStateAdapter(gridAdapter::retry))
        }
    }

    private fun observeViewMode() {
        collect(viewModel.uiState) { state ->
            updateLayoutManager(state.viewMode)
            updateToggleIndicator(state.viewMode == ViewMode.GRID)
        }
    }

    private fun observeTabState() {
        collect(viewModel.uiState) { state ->
            if (binding.tabLayout.selectedTabPosition != state.selectedTab) {
                binding.tabLayout.getTabAt(state.selectedTab)?.select()
            }
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


    private fun collectUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    uiState.media.collectLatest { pagingData ->
                        when (uiState.viewMode) {
                            ViewMode.GRID -> gridAdapter.submitData(lifecycle, pagingData)
                            ViewMode.LIST -> listAdapter.submitData(lifecycle, pagingData)
                        }
                    }
                }
            }
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.onTabChanged(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
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


    private fun updateToggleIndicator(isGridSelected: Boolean) {
        val indicatorMargin = if (isGridSelected) 0 else 40
        val layoutParams = binding.indicator.layoutParams as android.widget.FrameLayout.LayoutParams
        layoutParams.marginStart = (indicatorMargin * resources.displayMetrics.density).toInt()
        binding.indicator.layoutParams = layoutParams

        binding.btnGridView.isSelected = isGridSelected
        binding.btnListView.isSelected = !isGridSelected
    }


    private fun collectEvent() {
        collectLast(viewModel.exploringUIEvent) {
            it?.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: ExploringUIEvent) {
        when (event) {
            ExploringUIEvent.ActorsEvent -> {
                findNavController().navigate(
                    ExploringFragmentDirections
                        .actionExploringFragmentToActorsFragment()
                )
            }

            ExploringUIEvent.SearchEvent -> navigateToSearch()
            is ExploringUIEvent.ClickMediaEvent -> navigateToMediaDetails(event.mediaID)
            is ExploringUIEvent.SelectedCategory -> Unit
        }
    }

    private fun navigateToSearch() {
        val extras = FragmentNavigatorExtras(binding.inputSearch to "search_box")
        Navigation.findNavController(binding.root)
            .navigate(
                ExploringFragmentDirections.actionExploringFragmentToSearchFragment(),
                extras
            )
    }

    private fun navigateToMediaDetails(mediaId: Int) {
        val currentMediaType = if (binding.tabLayout.selectedTabPosition == 0) {
            Constants.MOVIE_CATEGORIES_ID
        } else {
            Constants.TV_CATEGORIES_ID
        }

        if (currentMediaType == Constants.MOVIE_CATEGORIES_ID)
            findNavController().navigate(
                ExploringFragmentDirections.actionExploringFragmentToMovieDetailFragment(mediaId)
            )
        else
            findNavController().navigate(
                ExploringFragmentDirections.actionExploringFragmentToTvShowDetailsFragment(mediaId)
            )
    }

    override fun onClickCategory(categoryId: Int) {
        viewModel.onClickMedia(categoryId)
    }
}
