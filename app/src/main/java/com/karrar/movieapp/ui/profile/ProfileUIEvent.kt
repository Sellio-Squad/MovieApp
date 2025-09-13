package com.karrar.movieapp.ui.profile

sealed interface ProfileUIEvent {
    class LoginEvent(val userName: String) : ProfileUIEvent
    object RatedMoviesEvent : ProfileUIEvent
    object DialogLogoutEvent : ProfileUIEvent
    object DialogPreferencesEvent : ProfileUIEvent
    object DialogLanguageEvent : ProfileUIEvent
    object WatchHistoryEvent : ProfileUIEvent
}
