package com.karrar.movieapp.ui.explore.exploreUIState

import com.karrar.movieapp.utilities.Constants

sealed class ExploringUIEvent {
    object SearchEvent : ExploringUIEvent()
    object ActorsEvent : ExploringUIEvent()
    data class ClickMediaEvent(val mediaID: Int) : ExploringUIEvent()
    data class SelectedCategory(val categoryID: Int = Constants.FIRST_CATEGORY_ID) :
        ExploringUIEvent()
}