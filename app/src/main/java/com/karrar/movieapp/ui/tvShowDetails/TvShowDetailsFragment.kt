package com.karrar.movieapp.ui.tvShowDetails

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentTvShowDetailsBinding
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.movieDetails.DetailInteractionListener
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TvShowDetailsFragment : BaseFragment<FragmentTvShowDetailsBinding>(),
    DetailInteractionListener {

    override val layoutIdFragment = R.layout.fragment_tv_show_details
    override val viewModel: TvShowDetailsViewModel by viewModels()
    private val args: TvShowDetailsFragmentArgs by navArgs()
    private val detailAdapter by lazy { DetailUIStateAdapter(emptyList(), viewModel) }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(false)
        binding.apply {
            viewModel = this@TvShowDetailsFragment.viewModel
            listener = this@TvShowDetailsFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        collectTVShowDetailsItems()
        collectEvents()
        setupRecyclerWithHeaderAnimation()
    }

    private fun collectTVShowDetailsItems() {
        binding.recyclerView.adapter = detailAdapter
        lifecycleScope.launch {
            viewModel.stateFlow.collectLatest {
                detailAdapter.setItems(viewModel.stateFlow.value.detailItemResult)
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    private fun collectEvents() {
        collectLast(viewModel.tvShowDetailsUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: TvShowDetailsUIEvent) {
        var action: NavDirections? = null
        when (event) {
            TvShowDetailsUIEvent.ClickBackEvent -> {
                findNavController().navigateUp()
            }
            is TvShowDetailsUIEvent.ClickCastEvent -> {
                action =
                    TvShowDetailsFragmentDirections.actionTvShowDetailFragmentToActorDetailsFragment(
                        event.castID
                    )
            }
            is TvShowDetailsUIEvent.ClickSeasonEvent -> {
                action =
                    TvShowDetailsFragmentDirections.actionTvShowDetailsFragmentToEpisodesFragment(
                        args.tvShowId,
                        event.seasonId
                    )
            }
            TvShowDetailsUIEvent.ClickPlayTrailerEvent -> {
                action =
                    TvShowDetailsFragmentDirections.actionTvShowDetailFragmentToYoutubePlayerActivity(
                        args.tvShowId, MediaType.TV_SHOW
                    )
            }
            TvShowDetailsUIEvent.ClickReviewsEvent -> {
                action =
                    TvShowDetailsFragmentDirections.actionTvShowDetailsFragmentToReviewFragment(
                        args.tvShowId, MediaType.TV_SHOW
                    )
            }
            TvShowDetailsUIEvent.MessageAppear -> {
                Toast.makeText(context, getString(R.string.submit_toast), Toast.LENGTH_SHORT).show()
            }

            is TvShowDetailsUIEvent.ClickTvShowEvent -> {
                viewModelStore.clear()
                action =
                    TvShowDetailsFragmentDirections.actionTvShowDetailFragmentToTvShowDetailFragment(
                        event.tvShowID
                    )
            }
        }
        action?.let { findNavController().navigate(it) }
    }

    override fun onclickBack() {
        findNavController().navigateUp()

    }

    override fun onClickSave() {
    }

    override fun onClickPlayTrailer() {
    }

    override fun onclickViewReviews() {
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupRecyclerWithHeaderAnimation() {

        binding.fullHeader.root.post {
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val headerHeight = binding.fullHeader.root.height
                val progress = (scrollY.toFloat() / headerHeight).coerceIn(0f, 1f)
                binding.headerMotionLayout.progress = progress
            }

        }
    }

}
