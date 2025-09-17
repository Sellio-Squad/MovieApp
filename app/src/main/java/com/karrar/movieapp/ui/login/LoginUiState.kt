package com.karrar.movieapp.ui.login

data class LoginUiState(
    val userName :String = "",
    val password :String = "",
    val userNameHelperText :String? = null,
    val passwordHelperText :String? = null,
    val isLoading:Boolean = false,
    val isValidForm : Boolean = false,
    val error:String = "",

)