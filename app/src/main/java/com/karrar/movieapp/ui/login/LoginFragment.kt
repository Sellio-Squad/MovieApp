package com.karrar.movieapp.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentLoginBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.utilities.CineVerseTextField
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    override val layoutIdFragment = R.layout.fragment_login
    override val viewModel: LoginViewModel by viewModels()

    private lateinit var usernameField: CineVerseTextField
    private lateinit var passwordField: CineVerseTextField

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameField = CineVerseTextField(
            binding.usernameField.root,
            leadingIconRes = R.drawable.ic_profile,
        ).apply {
            setLabel(getString(R.string.username))
            setHint(getString(R.string.username))
        }

        passwordField = CineVerseTextField(
            binding.passwordField.root,
            leadingIconRes = R.drawable.ic_lock,
            isPassword = true
        ).apply {
            setLabel(getString(R.string.password))
            setHint(getString(R.string.password))
        }

        setupTextFieldListeners()
        observeUiState()
    }


    override fun onStart() {
        super.onStart()
        setTitle(false)
        collectLast(viewModel.loginEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun setupTextFieldListeners() {
        usernameField.addTextChangedListener { text ->
            viewModel.onUserNameInputChange(text)
        }

        passwordField.addTextChangedListener { text ->
            viewModel.onPasswordInputChange(text)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.loginUIState.collectLatest { state ->
                usernameField.setError(
                    state.userNameHelperText.ifEmpty { null }
                )

                passwordField.setError(
                    state.passwordHelperText.ifEmpty { null }
                )
            }
        }
    }
    private fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.LoginEvent -> {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToProfileFragment())
            }
            LoginUIEvent.SignUpEvent -> {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.TMDB_SIGNUP_URL))
                startActivity(browserIntent)
            }
        }
    }
}