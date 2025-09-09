package com.karrar.movieapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentHomeBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.home.adapter.HomeAdapter
import com.karrar.movieapp.ui.home.adapter.PopularMovieAdapter
import com.karrar.movieapp.ui.home.homeUiState.HomeUIEvent
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override val layoutIdFragment = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()
    lateinit var homeAdapter: HomeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(false)
        setAdapter()
        collectEvent()
        collectHomeData()
    }

    private fun collectHomeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.homeUiState.collect { uiState ->
                homeAdapter.setItems(
                    mutableListOf(
                        uiState.popularMovies,
                        uiState.tvShowsSeries,
                        uiState.onTheAiringSeries,
                        uiState.airingTodaySeries,
                        uiState.upcomingMovies,
                        uiState.nowStreamingMovies,
                        uiState.mysteryMovies,
                        uiState.adventureMovies,
                        uiState.trendingMovies,
                        uiState.actors,
                    )
                )


                val viewPager =
                    binding.recyclerView.findViewById<ViewPager2>(R.id.viewpager_popular_movie)

                if (uiState.popularMovies is HomeItem.Slider) {
                    val items = uiState.popularMovies.items
                    if (items.isNotEmpty() && viewPager != null) {
                        val adapter = PopularMovieAdapter(items, viewModel)
                        viewPager.adapter = adapter
                        viewPager.offscreenPageLimit = 3

                        startAutoScroll(viewPager, items.size)
                    }
                }
            }
        }
    }


    private fun setAdapter() {
        homeAdapter = HomeAdapter(mutableListOf(), viewModel)
        binding.recyclerView.adapter = homeAdapter
    }

    private fun collectEvent() {
        collectLast(viewModel.homeUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: HomeUIEvent) {
        val action = when (event) {
            is HomeUIEvent.ClickActorEvent -> {
                HomeFragmentDirections.actionHomeFragmentToActorDetailsFragment(
                    event.actorID
                )
            }
            is HomeUIEvent.ClickMovieEvent -> {
                HomeFragmentDirections.actionHomeFragmentToMovieDetailFragment(
                    event.movieID
                )
            }
            HomeUIEvent.ClickSeeAllActorEvent -> {
                HomeFragmentDirections.actionHomeFragmentToActorsFragment()
            }
            is HomeUIEvent.ClickSeeAllMovieEvent -> {
                HomeFragmentDirections.actionHomeFragmentToAllMovieFragment(
                    -1, event.mediaType
                )
            }
            is HomeUIEvent.ClickSeeAllTVShowsEvent -> {
                HomeFragmentDirections.actionHomeFragmentToAllMovieFragment(
                    -1,
                    event.mediaType
                )
            }
            is HomeUIEvent.ClickSeriesEvent -> {
                HomeFragmentDirections.actionHomeFragmentToTvShowDetailsFragment(
                    event.seriesID
                )
            }
        }
        findNavController().navigate(action)
    }

    private fun startAutoScroll(viewPager: ViewPager2, itemCount: Int) {
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var position = 0
                while (true) {
                    delay(2000)
                    position = (position + 1) % itemCount

                    val smoothScroller = object : LinearSmoothScroller(viewPager.context) {
                        override fun getHorizontalSnapPreference(): Int = SNAP_TO_START
                        override fun calculateTimeForScrolling(dx: Int): Int {
                            return 400.coerceAtMost(super.calculateTimeForScrolling(dx))
                        }
                    }

                    smoothScroller.targetPosition = position
                    recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
                }
            }
        }
    }
}
