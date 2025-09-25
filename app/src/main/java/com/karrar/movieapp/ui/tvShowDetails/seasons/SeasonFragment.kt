package com.karrar.movieapp.ui.tvShowDetails.seasons

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSeasonBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.tvShowDetails.SeasonAdapterUIState
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeasonFragment: BaseFragment<FragmentSeasonBinding>() {

    override val layoutIdFragment = R.layout.fragment_season
    override val viewModel: SeasonViewModel by viewModels()
    private val adapter by lazy { SeasonAdapterUIState(emptyList(), viewModel) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seasonList.adapter = adapter

        lifecycleScope.launch {
            viewModel.seasons.collectLatest { seasons ->
                adapter.setItems(seasons)
            }
        }
        
        collectEvents()
    }

    private fun collectEvents() {
        collectLast(viewModel.seasonUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: SeasonUIEvent) {
        var action: NavDirections? = null
        when (event) {
            is SeasonUIEvent.ClickSeasonEvent -> {
                // Get tvShowId from arguments
                val tvShowId = arguments?.getInt("tvShowId") ?: 0
                action = SeasonFragmentDirections.actionSeasonFragmentToEpisodesFragment(
                    tvShowId,
                    event.seasonNumber
                )
            }
        }
        action?.let { findNavController().navigate(it) }
    }

}