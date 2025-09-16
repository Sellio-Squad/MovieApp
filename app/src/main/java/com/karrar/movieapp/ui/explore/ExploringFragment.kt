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
import com.karrar.movieapp.ui.explore.exploreUIState.ExploreUIState
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
    private lateinit var voiceLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerMedia.adapter = listAdapter.withLoadStateFooter(
            LoadUIStateAdapter(listAdapter::retry)
        )
        binding.recyclerMedia.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        collectEvent()
        collectUIState()
        setupToggleButton()
        setupVoiceSearch()
        setupTabLayout()

        collect(viewModel.uiState) { state ->
            updateRecyclerView(state)
            updateToggleIndicator(state.viewMode == ViewMode.GRID)
        }
    }

    private fun updateRecyclerView(state: ExploreUIState) {
        when (state.viewMode) {
            ViewMode.LIST -> {
                binding.recyclerMedia.adapter =
                    listAdapter.withLoadStateFooter(LoadUIStateAdapter(listAdapter::retry))
                binding.recyclerMedia.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            }
            ViewMode.GRID -> {
                binding.recyclerMedia.adapter =
                    gridAdapter.withLoadStateFooter(LoadUIStateAdapter(gridAdapter::retry))
                binding.recyclerMedia.layoutManager = GridLayoutManager(requireContext(), 2)
            }
        }
    }
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.onTabChanged(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                viewModel.onTabChanged(tab.position)
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


    private fun updateToggleIndicator(isGridSelected: Boolean) {
        val indicatorMargin = if (isGridSelected) 0 else 40
        val layoutParams = binding.indicator.layoutParams as android.widget.FrameLayout.LayoutParams
        layoutParams.marginStart = (indicatorMargin * resources.displayMetrics.density).toInt()
        binding.indicator.layoutParams = layoutParams
    }

    private fun setupVoiceSearch() {
        voiceLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val voiceText =
                    data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
                binding.inputSearch.setText(voiceText)
            }
        }

        binding.inputSearch.setOnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == android.view.MotionEvent.ACTION_UP &&
                event.rawX >= binding.inputSearch.right - binding.inputSearch.compoundDrawables[DRAWABLE_END].bounds.width()
            ) {
                startVoiceSearch()
                v.performClick()
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search")
        }

        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(), "Voice search not supported", Toast.LENGTH_SHORT
            )
                .show()
        }
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
            is ExploringUIEvent.ClickMediaEvent -> {
                navigateToMediaDetails(event.mediaID)
            }

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


    fun navigateToMovieDetails(movieId: Int) {
        val action =
            ExploringFragmentDirections.actionExploringFragmentToMovieDetailFragment(movieId)
        findNavController().navigate(action)
    }

    fun navigateToTvShowDetails(tvShowId: Int) {
        val action =
            ExploringFragmentDirections.actionExploringFragmentToTvShowDetailsFragment(tvShowId)
        findNavController().navigate(action)
    }

    private fun collectUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    uiState.media.collectLatest { pagingData ->
                        when (uiState.viewMode) {
                            ViewMode.GRID -> gridAdapter.submitData(pagingData)
                            ViewMode.LIST -> listAdapter.submitData(pagingData)
                        }
                    }
                }
            }
        }
    }




    private fun navigateToMediaDetails(mediaId: Int) {
        val currentMediaType = if (binding.tabLayout.selectedTabPosition == 0) {
            Constants.MOVIE_CATEGORIES_ID
        } else {
            Constants.TV_CATEGORIES_ID
        }

        if (currentMediaType == Constants.MOVIE_CATEGORIES_ID) {
            navigateToMovieDetails(mediaId)
        } else {
            navigateToTvShowDetails(mediaId)
        }
    }

    override fun onClickCategory(categoryId: Int) {
        viewModel.onClickMedia(categoryId)
    }

}