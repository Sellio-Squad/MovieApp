package com.karrar.movieapp.ui.explore

import com.karrar.movieapp.ui.base.BaseInteractionListener

interface CategoryInteractionListener: BaseInteractionListener {
    fun onClickCategory(categoryId: Int)
}