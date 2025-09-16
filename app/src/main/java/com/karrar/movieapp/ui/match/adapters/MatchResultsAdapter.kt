package com.karrar.movieapp.ui.match.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.databinding.ItemMovieBinding
import com.karrar.movieapp.domain.enums.HomeItemsType
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.ui.models.MediaUiState

class MatchResultsAdapter(
    private val onMovieClick: (Int) -> Unit
) : ListAdapter<Media, MatchResultsAdapter.ResultViewHolder>(ResultDiffCallback()) {

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

        fun bind(media: Media, onMovieClick: (Int) -> Unit) {
            val mediaUiState = MediaUiState(
                id = media.mediaID,
                imageUrl = media.mediaImage,
                rate = media.mediaRate
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

    private class ResultDiffCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.mediaID == newItem.mediaID
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }
    }
}