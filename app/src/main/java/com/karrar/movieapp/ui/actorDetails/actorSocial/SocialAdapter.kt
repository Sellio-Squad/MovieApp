package com.karrar.movieapp.ui.actorDetails.actorSocial

import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.ItemSocialBinding
import com.karrar.movieapp.ui.base.BaseAdapter

class SocialAdapter(val items: List<SocialItemUIState>, val listener: SocialInteractionListener) :
    BaseAdapter<SocialItemUIState>(items, listener) {

    override val layoutID: Int = R.layout.item_social

    override fun bind(holder: ItemViewHolder, position: Int) {
        super.bind(holder, position)

        if(holder.binding is ItemSocialBinding){
            setupFlexibleWidth(holder.binding, itemCount)
        }
    }

    private fun setupFlexibleWidth(binding: ItemSocialBinding, totalItems: Int){
        val layoutParams = binding.root.layoutParams as RecyclerView.LayoutParams
        val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
        val horizontalMargin = binding.root.context.resources.getDimensionPixelSize(R.dimen.spacing_medium) * 2
        val availableWidth = screenWidth - horizontalMargin
        val itemWidth = availableWidth / totalItems

        layoutParams.width = itemWidth
        binding.root.layoutParams = layoutParams
        binding.executePendingBindings()
    }

    override fun areItemsSame(oldItem: SocialItemUIState, newItem: SocialItemUIState): Boolean {
        return oldItem.url == newItem.url && oldItem.label == newItem.label
    }

    override fun areContentSame(
        oldPosition: SocialItemUIState,
        newPosition: SocialItemUIState
    ): Boolean {
        return oldPosition == newPosition
    }

}