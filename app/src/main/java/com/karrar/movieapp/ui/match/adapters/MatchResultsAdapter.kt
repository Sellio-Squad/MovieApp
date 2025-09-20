package com.karrar.movieapp.ui.match.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.databinding.ItemMovieBinding
import com.karrar.movieapp.domain.enums.HomeItemsType
import com.karrar.movieapp.ui.match.MatchedMovieUIState
import com.karrar.movieapp.ui.models.MediaUiState

class MatchResultsAdapter(
    private val onMovieClick: (Int) -> Unit
) : ListAdapter<MatchedMovieUIState, MatchResultsAdapter.ResultViewHolder>(ResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position), onMovieClick)
    }

    class ResultViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(media: MatchedMovieUIState, onMovieClick: (Int) -> Unit) {
            val mediaUiState = MediaUiState(
                id = media.movieId,
                imageUrl = media.movieImage,
                rate = media.movieVoteAverage.toFloat()
            )

            binding.item = mediaUiState
            binding.listener = object : com.karrar.movieapp.ui.adapters.MovieInteractionListener {
                override fun onClickMovie(movieId: Int) {
                    onMovieClick(movieId)
                }

                override fun onClickSeeAllMovie(homeItemsType: HomeItemsType) {
                }

                override fun onClickSeeAllGallery(homeItemsType: HomeItemsType) {

                }
            }

            binding.executePendingBindings()
        }
    }

    private class ResultDiffCallback : DiffUtil.ItemCallback<MatchedMovieUIState>() {
        override fun areItemsTheSame(
            oldItem: MatchedMovieUIState,
            newItem: MatchedMovieUIState
        ): Boolean {
            return oldItem.movieId == newItem.movieId
        }

        override fun areContentsTheSame(
            oldItem: MatchedMovieUIState,
            newItem: MatchedMovieUIState
        ): Boolean {
            return oldItem == newItem
        }
    }
}