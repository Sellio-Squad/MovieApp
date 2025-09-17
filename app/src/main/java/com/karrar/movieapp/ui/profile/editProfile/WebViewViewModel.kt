package com.karrar.movieapp.ui.profile.editProfile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel
class WebViewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val args = WebViewFragmentArgs.fromSavedStateHandle(savedStateHandle)
    val url = MutableStateFlow(args.url)
}