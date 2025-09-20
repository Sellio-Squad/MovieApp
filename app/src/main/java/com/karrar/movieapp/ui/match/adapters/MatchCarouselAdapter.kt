package com.karrar.movieapp.ui.match.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.R
import com.karrar.movieapp.ui.match.MatchedMovieUIState
import com.squareup.picasso.Picasso

class MatchCarouselAdapter(
    private val onMovieClick: (Int) -> Unit
) : ListAdapter<MatchedMovieUIState, MatchCarouselAdapter.CarouselViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(getItem(position), onMovieClick)
    }

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moviePoster: ImageView = itemView.findViewById(R.id.iv_movie_poster)

        fun bind(movie: MatchedMovieUIState, onMovieClick: (Int) -> Unit) {
            val imageUrl = BuildConfig.IMAGE_BASE_PATH + movie.movieImage
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.media_place_holder)
                .error(R.drawable.media_place_holder)
                .into(moviePoster)

            itemView.setOnClickListener {
                onMovieClick(movie.movieId)
            }
        }
    }

    private class MovieDiffCallback : DiffUtil.ItemCallback<MatchedMovieUIState>() {
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