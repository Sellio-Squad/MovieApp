package com.karrar.movieapp.ui.profile

import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.CheckIfLoggedInUseCase
import com.karrar.movieapp.domain.usecases.GetAccountDetailsUseCase
import com.karrar.movieapp.domain.usecases.theme.ChangeThemeUseCase
import com.karrar.movieapp.domain.usecases.theme.GetCurrentThemeUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Constants.THEME_DARK
import com.karrar.movieapp.utilities.Constants.THEME_LIGHT
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getAccountDetailsUseCase: GetAccountDetailsUseCase,
    private val accountUIStateMapper: AccountUIStateMapper,
    private val checkIfLoggedInUseCase: CheckIfLoggedInUseCase,
    private val changeThemeUseCase: ChangeThemeUseCase,
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase,
) : BaseViewModel() {

    private val _profileDetailsUIState = MutableStateFlow(ProfileUIState())
    val profileDetailsUIState = _profileDetailsUIState.asStateFlow()

    private val _profileUIEvent: MutableStateFlow<Event<ProfileUIEvent?>> =
        MutableStateFlow(Event(null))
    val profileUIEvent = _profileUIEvent.asStateFlow()

    init {
        getData()
        getCurrentTheme()
    }

    private fun getCurrentTheme() {
        viewModelScope.launch {
            getCurrentThemeUseCase.getCurrentTheme().collect { currentTheme ->
                _profileDetailsUIState.update { it.copy(isSwitchChecked = currentTheme == THEME_DARK) }
            }
        }
    }

    override fun getData() {
        getProfileDetails()
    }

    private fun getProfileDetails() {
        if (checkIfLoggedInUseCase()) {
            _profileDetailsUIState.update {
                it.copy(isLoading = true, isLoggedIn = true, error = false)
            }

            viewModelScope.launch {
                try {
                    val accountDetails = accountUIStateMapper.map(getAccountDetailsUseCase())
                    _profileDetailsUIState.update {
                        it.copy(
                            avatarPath = accountDetails.avatarPath,
                            name = accountDetails.name,
                            username = '@' + accountDetails.username,
                            isLoading = false
                        )
                    }
                } catch (t: Throwable) {
                    _profileDetailsUIState.update {
                        it.copy(isLoading = false, error = true)
                    }
                }
            }
        } else {
            _profileDetailsUIState.update {
                it.copy(isLoggedIn = false)
            }
        }
    }

    fun onClickRatedMovies() {
        if (profileDetailsUIState.value.isLoggedIn) {
            _profileUIEvent.update { Event(ProfileUIEvent.RatedMoviesEvent) }
        } else {
            _profileUIEvent.update { Event(ProfileUIEvent.LoginEvent(_profileDetailsUIState.value.username)) }
        }
    }

    fun onClickLogout() {
        _profileUIEvent.update { Event(ProfileUIEvent.DialogLogoutEvent) }
    }

    fun onClickPreferences() {
        _profileUIEvent.update { Event(ProfileUIEvent.DialogPreferencesEvent) }
    }

    fun onClickLanguage() {
        _profileUIEvent.update { Event(ProfileUIEvent.DialogLanguageEvent) }
    }

    fun onClickWatchHistory() {
        if (_profileDetailsUIState.value.isLoggedIn) {
            _profileUIEvent.update { Event(ProfileUIEvent.WatchHistoryEvent) }
        } else {
            _profileUIEvent.update { Event(ProfileUIEvent.LoginEvent(_profileDetailsUIState.value.username)) }
        }
    }

    fun onClickLogin() {
        _profileUIEvent.update { Event(ProfileUIEvent.LoginEvent(_profileDetailsUIState.value.username)) }
    }

    fun changeTheme() {
        viewModelScope.launch {
            if (_profileDetailsUIState.value.isSwitchChecked) {
                changeThemeUseCase.changeTheme(THEME_LIGHT)
            } else {
                changeThemeUseCase.changeTheme(THEME_DARK)
            }
        }
    }
}