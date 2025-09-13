package com.karrar.movieapp.ui.match.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.models.Media
import com.squareup.picasso.Picasso
import android.widget.ImageView
import com.karrar.movieapp.BuildConfig

class MatchCarouselAdapter(
    private val onMovieClick: (Int) -> Unit
) : ListAdapter<Media, MatchCarouselAdapter.CarouselViewHolder>(MovieDiffCallback()) {

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

        fun bind(movie: Media, onMovieClick: (Int) -> Unit) {
            val imageUrl = BuildConfig.IMAGE_BASE_PATH + movie.mediaImage
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.media_place_holder)
                .error(R.drawable.media_place_holder)
                .into(moviePoster)

            itemView.setOnClickListener {
                onMovieClick(movie.mediaID)
            }
        }
    }

    private class MovieDiffCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.mediaID == newItem.mediaID
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }
    }
}