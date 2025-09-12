package com.karrar.movieapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.karrar.movieapp.BR
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.HomeItemsType
import com.karrar.movieapp.ui.adapters.*
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.home.HomeInteractionListener
import com.karrar.movieapp.ui.home.HomeItem
import com.karrar.movieapp.ui.models.MediaUiState
import com.karrar.movieapp.utilities.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeAdapter(
    private var homeItems: MutableList<HomeItem>,
    private val listener: BaseInteractionListener,
    private val scope: CoroutineScope
) : BaseAdapter<HomeItem>(homeItems, listener) {
    override val layoutID: Int = 0

    fun setItem(item: HomeItem) {
        val newItems = homeItems.apply {
            removeAt(item.priority)
            add(item.priority, item)
        }
        setItems(newItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), viewType, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (homeItems.isNotEmpty())
            bind(holder as ItemViewHolder, position)
    }

    override fun bind(holder: ItemViewHolder, position: Int) {
        if (position != -1)
            when (val currentItem = homeItems[position]) {
                is HomeItem.Slider -> {
                    val adapter = PopularMovieAdapter(currentItem.items, listener as HomeInteractionListener)
                    holder.binding.setVariable(BR.adapterRecycler, adapter)

                    val viewPager = holder.binding.root.findViewById<ViewPager2>(R.id.viewpager_popular_movie)
                    viewPager?.adapter = adapter  // ⚡ Make sure adapter is set
                    viewPager?.offscreenPageLimit = 3
                    attachCarouselTransformer(viewPager)
                    startAutoScroll(viewPager, currentItem.items.size)
                }


                is HomeItem.TvShows -> {
                    holder.binding.run {
                        if (currentItem.items.isNotEmpty()) {
                            setVariable(BR.topRated, currentItem.items.first())
                            setVariable(BR.popular, currentItem.items[1])
                            setVariable(BR.latest, currentItem.items.last())
                            setVariable(BR.listener, listener as TVShowInteractionListener)
                        }
                    }
                }

                is HomeItem.Actor -> {
                    holder.binding.run {
                        setVariable(
                            BR.adapterRecycler, ActorAdapter(
                                currentItem.items,
                                R.layout.item_actor_home,
                                listener as ActorsInteractionListener
                            )
                        )
                        setVariable(BR.listener, listener as HomeInteractionListener)
                    }

                }

                is HomeItem.AiringToday -> {
                    holder.binding.run {
                        setVariable(
                            BR.adapterRecycler,
                            MediaAdapter(
                                currentItem.items.take(Constants.MAX_NUMBER_AIRING_TODAY),
                                R.layout.item_airing_today,
                                listener as MediaInteractionListener
                            )
                        )
                        setVariable(BR.count, currentItem.items.size)
                    }
                }

                is HomeItem.Adventure -> {
                    bindMovie(holder, currentItem.items, currentItem.type)
                }

                is HomeItem.Mystery -> {
                    bindMovie(holder, currentItem.items, currentItem.type)
                }

                is HomeItem.RecentlyReleased -> {
                    bindMovie(holder, currentItem.items, currentItem.type)
                }

                is HomeItem.OnTheAiring -> {
                    holder.binding.run {
                        setVariable(
                            BR.adapterRecycler,
                            TVShowAdapter(currentItem.items, listener as TVShowInteractionListener)
                        )
                        setVariable(BR.movieType, currentItem.type)
                    }
                }

                is HomeItem.Trending -> {
                    bindMovie(holder, currentItem.items, currentItem.type)
                }

                is HomeItem.Upcoming -> {
                    bindMovie(holder, currentItem.items, currentItem.type)
                }

                is HomeItem.BrowseEverything -> {
                    holder.binding.run {
                        setVariable(BR.listener, listener as HomeInteractionListener)
                    }
                }

                is HomeItem.RecentlyViewed -> {
                    holder.binding.setVariable(
                        BR.adapterRecycler,
                        RecentlyViewedAdapter(currentItem.items, listener as RecentlyViewedInteractionListener)
                    )
                }

                is HomeItem.LetUsChooseForYou -> {
                    holder.binding.run {
                        setVariable(BR.listener, listener as HomeInteractionListener)
                    }
                }
            }
    }

    private fun bindMovie(holder: ItemViewHolder, items: List<MediaUiState>, type: HomeItemsType) {
        holder.binding.run {
            setVariable(
                BR.adapterRecycler,
                MovieAdapter(items, listener as MovieInteractionListener)
            )
            setVariable(BR.movieType, type)
        }
    }

    override fun setItems(newItems: List<HomeItem>) {
        homeItems = newItems.sortedBy { it.priority }.toMutableList()
        super.setItems(homeItems)
    }

    override fun areItemsSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
        return oldItem.priority == newItem.priority
    }

    override fun areContentSame(
        oldPosition: HomeItem,
        newPosition: HomeItem,
    ): Boolean {
        return oldPosition == newPosition
    }

    override fun getItemViewType(position: Int): Int {
        if (homeItems.isNotEmpty()) {
            return when (homeItems[position]) {
                is HomeItem.BrowseEverything -> R.layout.item_browser_everything_cta
                is HomeItem.Actor -> R.layout.list_actor
                is HomeItem.TvShows -> R.layout.list_tv_shows
                is HomeItem.Slider -> R.layout.list_popular
                is HomeItem.AiringToday -> R.layout.list_airing_today
                is HomeItem.OnTheAiring -> R.layout.list_tvshow
                is HomeItem.Adventure,
                is HomeItem.Mystery,
                is HomeItem.RecentlyReleased,
                is HomeItem.Trending,
                is HomeItem.Upcoming,
                -> R.layout.list_movie

                is HomeItem.RecentlyViewed -> R.layout.list_recently_viewed
                is HomeItem.LetUsChooseForYou -> R.layout.item_let_us_choose_cta
            }
        }
        return -1
    }

    private fun attachCarouselTransformer(viewPager: ViewPager2) {
        val sidePeek = viewPager.resources.displayMetrics.widthPixels * 0.05f
        val extraLift = viewPager.context.resources.getDimensionPixelOffset(R.dimen.spacing_extra_extra_large)

        viewPager.setPageTransformer { page, position ->
            val offset = position * -sidePeek
            page.translationX = if (viewPager.layoutDirection == View.LAYOUT_DIRECTION_RTL) -offset else offset
            page.scaleX = 1f
            page.scaleY = 1f
            page.alpha = 0.8f + (1 - abs(position)) * 0.2f
            page.translationY = if (position == 0f) -extraLift.toFloat() else 0f
            page.translationZ = if (position == 0f) 1f else 0f
        }
    }
    fun startAutoScroll(viewPager: ViewPager2, itemCount: Int) {
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView ?: return

        scope.launch {
            var position = 0
            while (true) {
                delay(2000)
                position = (position + 1) % itemCount

                val smoothScroller = object : LinearSmoothScroller(viewPager.context) {
                    override fun getHorizontalSnapPreference(): Int = SNAP_TO_START
                    override fun calculateTimeForScrolling(dx: Int): Int {
                        return 400.coerceAtMost(super.calculateTimeForScrolling(dx))
                    }
                }

                smoothScroller.targetPosition = position
                recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
            }
        }
    }

}
