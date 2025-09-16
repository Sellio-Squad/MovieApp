package com.karrar.movieapp.ui.adapters


import androidx.databinding.library.baseAdapters.BR
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.models.CrewUIState

class CrewAdapter(
    items: List<CrewUIState>,
    val layout: Int,
    val listener: BaseInteractionListener
) : BaseAdapter<CrewUIState>(items = items, listener = listener) {
    override val layoutID: Int = layout
    override fun bind(holder: ItemViewHolder, position: Int) {
        super.bind(holder, position)
        holder.binding.setVariable(BR.isLast, position == itemCount - 1)
    }
}