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

    override val layoutIdFragment: Int = R.layout.fragment_season
    override val viewModel: SeasonViewModel by viewModels()
    private val seasonsAdapter by lazy { ShowAllSeasonsAdapterUIState(emptyList(), viewModel) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listener = viewModel
        binding.seasonList.adapter = seasonsAdapter
        collectEvents()
    }

    private fun collectEvents() {
        collectLast(viewModel.seasonsUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: SeasonsUIEvent) {
        when (event) {
            SeasonsUIEvent.OnBackClick -> {
                findNavController().navigateUp()
            }
        }
    }

}