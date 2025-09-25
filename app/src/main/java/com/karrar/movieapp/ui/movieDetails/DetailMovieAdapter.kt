package com.karrar.movieapp.ui.movieDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.karrar.movieapp.BR
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.MovieItemsType
import com.karrar.movieapp.ui.adapters.*
import com.karrar.movieapp.ui.adapters.moviedetailsadapters.SimilarMovieAdapter
import com.karrar.movieapp.ui.adapters.moviedetailsadapters.SimilarMovieInteractionListener
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.DetailMovieUIState

class DetailMovieAdapter(
    private var items: List<DetailMovieUIState>,
    private val listener: BaseInteractionListener,
) : BaseAdapter<DetailMovieUIState>(items, listener) {
    override val layoutID: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), viewType, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bind(holder as ItemViewHolder, position)
    }

    override fun bind(holder: ItemViewHolder, position: Int) {
        when (val currentItem = items[position]) {
            is DetailMovieUIState.OverView -> {
                holder.binding.run {
                    setVariable(BR.item, currentItem.data)
                }

            }

            is DetailMovieUIState.Cast -> {
                holder.binding.run {
                    setVariable(
                        BR.adapterRecycler,
                        ActorAdapter(
                            currentItem.data,
                            R.layout.item_cast,
                            listener as ActorsInteractionListener
                        )
                    )
                }
            }

            is DetailMovieUIState.SimilarMovies -> {
                holder.binding.run {
                    val adapter = SimilarMovieAdapter(currentItem.data, listener as SimilarMovieInteractionListener)
                    setVariable(BR.adapterRecycler, adapter)
                    setVariable(BR.movieType, MovieItemsType.YOU_MIGHT_ALSO_LIKE)
                }
            }


            is DetailMovieUIState.Crew -> {
                holder.binding.run {
                    setVariable(
                        BR.adapterRecycler,
                        CrewAdapter(currentItem.data, R.layout.item_crew, listener)
                    )
                }
            }
            is DetailMovieUIState.Rating -> {
                holder.binding.run {
                    setVariable(BR.viewModel, currentItem.viewModel)
                }
            }
            is DetailMovieUIState.Comment -> {
                holder.binding.run {
                    setVariable(BR.item, currentItem.data)
                    setVariable(BR.listener, listener)
                }
            }

            DetailMovieUIState.ReviewText ->{
                holder.binding.run {
                    setVariable(BR.listener, listener as DetailInteractionListener)
                }
            }
        }
    }

    override fun setItems(newItems: List<DetailMovieUIState>) {
        items = newItems.sortedBy { it.priority }
        super.setItems(items)
    }

    override fun areItemsSame(oldItem: DetailMovieUIState, newItem: DetailMovieUIState): Boolean {
        return oldItem.priority == newItem.priority
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DetailMovieUIState.OverView -> R.layout.item_movie_overview
            is DetailMovieUIState.Cast -> R.layout.list_cast
            is DetailMovieUIState.SimilarMovies -> R.layout.list_similar_movie
            is DetailMovieUIState.Rating -> R.layout.item_rating
            is DetailMovieUIState.ReviewText -> R.layout.item_see_all_reviews
            is DetailMovieUIState.Comment -> R.layout.item_movie_review
            is DetailMovieUIState.Crew -> R.layout.list_crew
        }
    }

}


