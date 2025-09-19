package com.karrar.movieapp.ui.movieDetails

import android.animation.ValueAnimator
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
import com.karrar.movieapp.databinding.FragmentMovieDetailsBinding
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.profile.ProfileFragmentDirections
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailsFragment : BaseFragment<FragmentMovieDetailsBinding>(),
    DetailInteractionListener {

    override val layoutIdFragment = R.layout.fragment_movie_details
    override val viewModel: MovieDetailsViewModel by viewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()
    private val detailAdapter by lazy { DetailAdapter(emptyList(), viewModel) }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(false)
        binding.viewModel = viewModel
        binding.apply {
            viewModel = this@MovieDetailsFragment.viewModel
            listener = this@MovieDetailsFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        collectMovieDetailsItems()
        collectEvents()
        setupRecyclerWithHeaderAnimation()

    }

    private fun collectMovieDetailsItems() {
        binding.recyclerView.adapter = detailAdapter
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                detailAdapter.setItems(viewModel.uiState.value.detailItemResult)
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    private fun collectEvents() {
        collectLast(viewModel.movieDetailsUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: MovieDetailsUIEvent) {
        var action: NavDirections? = null
        when (event) {
            MovieDetailsUIEvent.ClickBackEvent -> {
                findNavController().navigateUp()
            }

            is MovieDetailsUIEvent.ClickCastEvent -> {
                action =
                    MovieDetailsFragmentDirections.actionMovieDetailFragmentToActorDetailsFragment(
                        event.castID
                    )
            }

            is MovieDetailsUIEvent.ClickMovieEvent -> {
                viewModelStore.clear()
                action = MovieDetailsFragmentDirections.actionMovieDetailsFragment(event.movieID)
            }

            MovieDetailsUIEvent.ClickPlayTrailerEvent -> {
                action =
                    MovieDetailsFragmentDirections.actionMovieDetailFragmentToYoutubePlayerActivity(
                        args.movieId, MediaType.MOVIE
                    )
            }

            MovieDetailsUIEvent.ClickReviewsEvent -> {
                action = MovieDetailsFragmentDirections.actionMovieDetailsFragmentToReviewFragment(
                    args.movieId, MediaType.MOVIE
                )
            }

            MovieDetailsUIEvent.ClickSaveEvent -> {
                action = MovieDetailsFragmentDirections.actionMovieDetailsFragmentToSaveMovieDialog(
                    args.movieId
                )
            }

            MovieDetailsUIEvent.MessageAppear -> {
                Toast.makeText(context, getString(R.string.submit_toast), Toast.LENGTH_SHORT).show()
            }

            MovieDetailsUIEvent.ShowLoginDialogEvent -> {
                action = MovieDetailsFragmentDirections.actionMovieDetailFragmentToLogInDialog("")
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
        val recyclerView = binding.recyclerView
        val motionLayout = binding.headerMotionLayout

        var lastProgress = 0f

        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            val range = recyclerView.computeVerticalScrollRange()
            val extent = recyclerView.computeVerticalScrollExtent()

            val canScroll = range > extent
            if (!canScroll) {
                motionLayout.progress = 0f
                lastProgress = 0f
                return@setOnScrollChangeListener
            }

            val offset = recyclerView.computeVerticalScrollOffset().toFloat()
            val maxScroll = (range - extent).toFloat()

            val targetProgress = if (maxScroll > 0) {
                (offset / maxScroll).coerceIn(0f, 1f)
            } else {
                0f
            }

            if (targetProgress != lastProgress) {
                ValueAnimator.ofFloat(lastProgress, targetProgress).apply {
                    duration = 50
                    interpolator = android.view.animation.DecelerateInterpolator()
                    addUpdateListener { animator ->
                        motionLayout.progress = animator.animatedValue as Float
                    }
                    start()
                }
            }
            lastProgress = targetProgress
        }
    }
}