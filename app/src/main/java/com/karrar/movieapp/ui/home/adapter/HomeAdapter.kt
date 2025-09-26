package com.karrar.movieapp.ui.home.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.karrar.movieapp.BR
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.HomeItemsType
import com.karrar.movieapp.ui.adapters.MovieAdapter
import com.karrar.movieapp.ui.adapters.MovieInteractionListener
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.home.HomeInteractionListener
import com.karrar.movieapp.ui.home.HomeItem
import com.karrar.movieapp.ui.home.homeUiState.PopularUiState
import com.karrar.movieapp.ui.models.MediaUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class HomeAdapter(
    private var homeItems: MutableList<HomeItem>,
    private val listener: BaseInteractionListener,
    private val scope: CoroutineScope,
    private val lifecycleOwner: LifecycleOwner
) : BaseAdapter<HomeItem>(homeItems, listener) {
    override val layoutID: Int = 0

    private var autoScrollJob: Job? = null
    private var currentViewPager: ViewPager2? = null
    private var fadeAnimator: ValueAnimator? = null

    private var isUserScrolling = false
    private var userInteractionStartTime = 0L
    private var lastUserInteractionTime = 0L
    private var isUserDragging = false

    private var fixedRatingText: TextView? = null
    private var fixedMovieTitle: TextView? = null
    private var fixedMovieGenre: TextView? = null

    private var isAnimating = false
    private var pendingItem: PopularUiState? = null

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
                    setupSliderWithFixedElements(holder, currentItem)
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

                is HomeItem.Upcoming -> {
                    bindMovie(holder, currentItem.items, currentItem.type)
                }

                is HomeItem.BrowseEverything -> {
                    holder.binding.run {
                        setVariable(BR.listener, listener as HomeInteractionListener)
                    }
                }

                is HomeItem.RecentlyViewed -> {
                    bindSectionVisibility(holder, currentItem.items) {
                        holder.binding.setVariable(
                            BR.adapterRecycler,
                            RecentlyViewedAdapter(
                                currentItem.items,
                                listener as RecentlyViewedInteractionListener
                            )
                        )
                    }
                }

                is HomeItem.LetUsChooseForYou -> {
                    holder.binding.run {
                        setVariable(BR.listener, listener as HomeInteractionListener)
                    }
                }

                is HomeItem.CollectionsList -> {
                    bindSectionVisibility(holder, currentItem.items) {
                        holder.binding.setVariable(
                            BR.adapterRecycler,
                            YourCollectionsAdapter(
                                currentItem.items,
                                listener as YourCollectionsInteractionListener
                            )
                        )
                    }
                }

                is HomeItem.FeaturedCollections -> {
                    holder.binding.run {
                        setVariable(
                            BR.adapterRecycler,
                            FeaturedCollectionsAdapter(
                                currentItem.items,
                                listener as MovieInteractionListener
                            )
                        )
                    }
                }

                is HomeItem.MatchesYourVibes -> {
                    bindSectionVisibility(holder, currentItem.items) {
                        bindMovie(
                            holder,
                            currentItem.items,
                            currentItem.type
                        )
                    }
                }
            }
    }

    private fun bindSectionVisibility(
        holder: ItemViewHolder,
        items: List<*>,
        bindAction: () -> Unit
    ) {
        val itemView = holder.itemView
        val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams

        if (items.isNotEmpty()) {
            itemView.visibility = View.VISIBLE
            layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT
            val verticalMargin =
                itemView.context.resources.getDimensionPixelSize(R.dimen.spacing_medium)
            layoutParams.topMargin = verticalMargin
            layoutParams.bottomMargin = verticalMargin
            bindAction()
        } else {
            itemView.visibility = View.GONE
            layoutParams.height = 0
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0
        }
        itemView.layoutParams = layoutParams
    }

    private fun setupSliderWithFixedElements(holder: ItemViewHolder, currentItem: HomeItem.Slider) {
        val adapter = PopularMovieAdapter(currentItem.items, listener as HomeInteractionListener)
        holder.binding.setVariable(BR.adapterRecycler, adapter)

        val viewPager = holder.binding.root.findViewById<ViewPager2>(R.id.viewpager_popular_movie)
        viewPager?.let { vp ->
            vp.adapter = adapter
            vp.offscreenPageLimit = 2
            vp.clipToPadding = false
            vp.clipChildren = false

            vp.getChildAt(0)?.let { child ->
                if (child is RecyclerView) {
                    child.clipToPadding = false
                    child.clipChildren = false
                    child.overScrollMode = View.OVER_SCROLL_NEVER
                }
            }

            findFixedElements(holder.binding.root)
            attachEnhancedCarouselTransformer(vp, currentItem.items)
            stopAutoScroll()
            currentViewPager = vp
            startAutoScrollWithLifecycle(vp, currentItem.items.size)
            if (currentItem.items.isNotEmpty()) {
                updateFixedContent(currentItem.items[0])
            }
        }
    }

    private fun findFixedElements(rootView: View) {
        fixedRatingText = rootView.findViewById(R.id.fixed_rating_text)
        fixedMovieTitle = rootView.findViewById(R.id.fixed_movie_title)
        fixedMovieGenre = rootView.findViewById(R.id.fixed_movie_genre)
    }

    private fun attachEnhancedCarouselTransformer(
        viewPager: ViewPager2,
        items: List<PopularUiState>
    ) {
        val displayMetrics = viewPager.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val density = displayMetrics.density

        val sideCardScale = 1.20f
        val sideCardAlpha = 0.7f
        val verticalOffsetDp = 55f

        viewPager.setPageTransformer { page, position ->
            val pageOffset = position.absoluteValue

            val scale = when {
                pageOffset <= 0.5f -> {
                    1.1f - (pageOffset * 2f * (1f - sideCardScale))
                }

                pageOffset <= 1f -> {
                    sideCardScale
                }

                else -> sideCardScale
            }

            val alpha = when {
                pageOffset <= 0.5f -> {
                    1.0f - (pageOffset * 2f * (1f - sideCardAlpha))
                }

                pageOffset <= 1f -> {
                    sideCardAlpha
                }

                else -> sideCardAlpha
            }

            val verticalOffset = when {
                pageOffset <= 0.5f -> {
                    (pageOffset * 2f) * verticalOffsetDp * density
                }

                pageOffset <= 1f -> {
                    verticalOffsetDp * density
                }

                else -> verticalOffsetDp * density
            }

            val horizontalOffset = when {
                position > 0 -> {
                    -screenWidth * 0.01f * (pageOffset.coerceAtMost(1f))
                }

                position < 0 -> {
                    screenWidth * 0.01f * (pageOffset.coerceAtMost(1f))
                }

                else -> 0f
            }

            val elevation = when {
                pageOffset <= 0.5f -> 12f - (pageOffset * 8f)
                pageOffset <= 1f -> 4f
                else -> 2f
            }

            page.scaleX = scale
            page.scaleY = scale
            page.alpha = alpha
            page.translationY = verticalOffset
            page.translationX = horizontalOffset
            page.translationZ = elevation

            val rotation = when {
                position > 0.5f -> 2f * (position - 0.5f).coerceAtMost(0.5f)
                position < -0.5f -> -2f * (-position - 0.5f).coerceAtMost(0.5f)
                else -> 0f
            }
            page.rotation = rotation
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private var lastSelectedPosition = -1
            private var updateJob: Job? = null

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                val currentTime = System.currentTimeMillis()

                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        isUserDragging = true
                        isUserScrolling = true
                        userInteractionStartTime = currentTime
                        lastUserInteractionTime = currentTime
                        stopAutoScroll()
                        updateJob?.cancel()
                    }

                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        if (isUserDragging) {
                            lastUserInteractionTime = currentTime
                        }
                    }

                    ViewPager2.SCROLL_STATE_IDLE -> {
                        if (isUserDragging) {
                            isUserDragging = false
                            isUserScrolling = false
                            lastUserInteractionTime = currentTime

                            restartAutoScrollAfterDelay(viewPager, items.size)
                        }
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < items.size && position != lastSelectedPosition) {
                    lastSelectedPosition = position

                    updateJob?.cancel()

                    updateJob = lifecycleOwner.lifecycleScope.launch {
                        delay(50)
                        if (position < items.size) {
                            updateFixedContentWithAnimation(items[position])
                        }
                    }
                }
            }
        })
    }

    private fun updateFixedContent(item: PopularUiState) {
        fixedRatingText?.text = String.format("%.1f", item.movieRate)
        fixedMovieTitle?.text = item.title
        fixedMovieGenre?.text = item.genre.joinToString(" • ")
    }

    private fun updateFixedContentWithAnimation(item: PopularUiState) {
        if (fixedMovieTitle?.text == item.title) {
            return
        }

        if (isAnimating) {
            pendingItem = item
            return
        }

        isAnimating = true
        fadeAnimator?.cancel()

        fadeAnimator = ValueAnimator.ofFloat(1f, 0f, 1f).apply {
            duration = 300
            var hasUpdatedContent = false

            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float

                when {
                    progress <= 0.5f -> {
                        val alpha = 1f - (progress * 2f)
                        fixedRatingText?.alpha = alpha
                        fixedMovieTitle?.alpha = alpha
                        fixedMovieGenre?.alpha = alpha

                        if (progress >= 0.45f && !hasUpdatedContent) {
                            updateFixedContent(item)
                            hasUpdatedContent = true
                        }
                    }

                    else -> {
                        val alpha = (progress - 0.5f) * 2f
                        fixedRatingText?.alpha = alpha
                        fixedMovieTitle?.alpha = alpha
                        fixedMovieGenre?.alpha = alpha
                    }
                }
            }

            doOnEnd {
                isAnimating = false
                pendingItem?.let { pending ->
                    pendingItem = null
                    fixedMovieTitle?.post {
                        updateFixedContentWithAnimation(pending)
                    }
                }
            }

            start()
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

    override fun areContentSame(oldPosition: HomeItem, newPosition: HomeItem): Boolean {
        return oldPosition == newPosition
    }

    override fun getItemViewType(position: Int): Int {
        if (homeItems.isNotEmpty()) {
            return when (homeItems[position]) {
                is HomeItem.BrowseEverything -> R.layout.item_browser_everything_cta
                is HomeItem.Slider -> R.layout.list_popular
                is HomeItem.OnTheAiring -> R.layout.list_tvshow
                is HomeItem.RecentlyReleased,
                is HomeItem.Upcoming,
                is HomeItem.MatchesYourVibes -> R.layout.list_movie

                is HomeItem.RecentlyViewed -> R.layout.list_recently_viewed
                is HomeItem.LetUsChooseForYou -> R.layout.item_let_us_choose_cta
                is HomeItem.CollectionsList -> R.layout.list_your_collections
                is HomeItem.FeaturedCollections -> R.layout.list_featured_collections
            }
        }
        return -1
    }

    private fun startAutoScrollWithLifecycle(viewPager: ViewPager2, itemCount: Int) {
        if (itemCount <= 1) return

        autoScrollJob?.cancel()
        autoScrollJob = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var position = viewPager.currentItem

                while (true) {
                    delay(4000)

                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastInteraction = currentTime - lastUserInteractionTime

                    if (viewPager.isAttachedToWindow &&
                        currentViewPager == viewPager &&
                        !isUserScrolling &&
                        !isUserDragging &&
                        timeSinceLastInteraction > 3000
                    ) {

                        position = (position + 1) % itemCount

                        try {
                            viewPager.setCurrentItem(position, true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            break
                        }
                    }
                }
            }
        }
    }

    private fun restartAutoScrollAfterDelay(viewPager: ViewPager2, itemCount: Int) {
        lifecycleOwner.lifecycleScope.launch {
            delay(4000)

            if (!isUserScrolling && !isUserDragging) {
                startAutoScrollWithLifecycle(viewPager, itemCount)
            }
        }
    }

    private fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        stopAutoScroll()
        fadeAnimator?.cancel()
        fadeAnimator = null
        currentViewPager = null
        isUserScrolling = false
        isUserDragging = false
        userInteractionStartTime = 0L
        lastUserInteractionTime = 0L
        isAnimating = false
        pendingItem = null
    }
}