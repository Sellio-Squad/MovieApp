package com.karrar.movieapp.ui.tvShowDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.karrar.movieapp.BR
import com.karrar.movieapp.R
import com.karrar.movieapp.ui.adapters.*
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.movieDetails.DetailInteractionListener
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIState.DetailItemUIState

class DetailUIStateAdapter(
    private var items: List<DetailItemUIState>,
    private val listener: BaseInteractionListener
) : BaseAdapter<DetailItemUIState>(items, listener) {
    override val layoutID: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bind(holder as ItemViewHolder, position)
    }

    override fun bind(holder: ItemViewHolder, position: Int) {
        when (val currentItem = items[position]) {
            is DetailItemUIState.OverView -> {
                holder.binding.run {
                    setVariable(BR.item, currentItem.data)
                    setVariable(BR.listener, listener as DetailInteractionListener)
                }
            }
            is DetailItemUIState.Seasons -> {
                holder.binding.run {
                    setVariable(
                        BR.adapterRecycler,
                        SeasonAdapterUIState(currentItem.data, listener as SeasonInteractionListener)
                    )
                }
            }
            is DetailItemUIState.Cast -> {
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
            is DetailItemUIState.SimilarTvShow -> {
                holder.binding.run {
                    setVariable(
                        BR.adapterRecycler,
                        TvShowDetailsAdapter(currentItem.data, listener as TvShowDetailsInteractionListener)
                    )
                }
            }
            is DetailItemUIState.Crew -> {
                holder.binding.run {
                    setVariable(
                        BR.adapterRecycler,
                        CrewAdapter(
                            currentItem.data,
                            R.layout.item_crew,
                            listener
                        )
                    )
                }
            }
            is DetailItemUIState.Rating -> {
                holder.binding.run {
                    setVariable(BR.viewModel, currentItem.viewModel)
                }
            }
            is DetailItemUIState.Comment -> {
                holder.binding.run {
                    setVariable(BR.item, currentItem.data)
                    setVariable(BR.listener, listener)
                }
            }
            DetailItemUIState.SeeAllReviewsButton -> {
                holder.binding.run {
                    setVariable(BR.listener, listener as DetailInteractionListener)
                }
            }
        }
    }

    override fun setItems(newItems: List<DetailItemUIState>) {
        items = newItems.sortedBy { it.priority }
        super.setItems(items)
    }

    override fun areItemsSame(oldItem: DetailItemUIState, newItem: DetailItemUIState): Boolean {
        return oldItem.priority == newItem.priority
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DetailItemUIState.OverView -> R.layout.item_tv_show_overview
            is DetailItemUIState.Cast -> R.layout.list_cast
            is DetailItemUIState.SimilarTvShow -> R.layout.list_similar_tv_show
            is DetailItemUIState.Seasons -> R.layout.list_season
            is DetailItemUIState.Rating -> R.layout.item_tvshow_rating
            is DetailItemUIState.Comment -> R.layout.item_tvshow_review
            DetailItemUIState.SeeAllReviewsButton -> R.layout.item_see_all_reviews
            is DetailItemUIState.Crew -> R.layout.list_crew
        }
    }
}
