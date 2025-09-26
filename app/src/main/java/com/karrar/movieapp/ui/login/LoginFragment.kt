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
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    override val layoutIdFragment = R.layout.fragment_login
    override val viewModel: LoginViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        setTitle(false)
        collectLast(viewModel.loginEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(false)

        lifecycleScope.launch {
            viewModel.loginUIState.collect { state ->
                if (state.isLoading) {
                    binding.containedButton.text = ""
                    binding.containedButton.isEnabled = false
                } else {
                    binding.containedButton.text = getString(R.string.login)
                    binding.containedButton.isEnabled = state.isValidForm
                }
            }
        }
        collectLast(viewModel.loginEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }
    private fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.LoginEvent -> {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToProfileFragment()
                )
            }

            LoginUIEvent.SignUpEvent -> {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.TMDB_SIGNUP_URL))
                startActivity(browserIntent)
            }

            LoginUIEvent.ShowSignUpDialog -> {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToSignUpDialog()
                )
            }
            LoginUIEvent.ForgotPasswordEvent -> {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToForgotPasswordDialog()
                )
            }
        }
    }
}