package com.karrar.movieapp.ui.login.signup

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogSignUpBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpDialog : BaseDialogFragment<DialogSignUpBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_sign_up
    override val viewModel: SignUpViewModel by viewModels()

    override fun onStart() {
        super.onStart()

        collectLast(viewModel.signUpUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: SignUpEvents) {
        when (event) {
            SignUpEvents.CloseDialogEvent -> {
                dismiss()
            }
            is SignUpEvents.GoToWebsiteEvent -> {
                dismiss()
                // Navigate to WebView or open browser
                findNavController().navigate(
                    SignUpDialogDirections.actionSignUpDialogToWebViewFragment(event.url)
                )
            }
        }
    }
}