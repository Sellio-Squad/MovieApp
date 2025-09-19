// File: ForgotPasswordDialog.kt
package com.karrar.movieapp.ui.login.forgotpassword

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogForgotPasswordBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordDialog : BaseDialogFragment<DialogForgotPasswordBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_forgot_password
    override val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onStart() {
        super.onStart()

        collectLast(viewModel.forgotPasswordUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: ForgotPasswordEvents) {
        when (event) {
            ForgotPasswordEvents.CloseDialogEvent -> {
                dismiss()
            }
            is ForgotPasswordEvents.GoToWebsiteEvent -> {
                dismiss()
                // Navigate to WebView or open browser
                findNavController().navigate(
                    ForgotPasswordDialogDirections.actionForgotPasswordDialogToWebViewFragment(event.url)
                )
            }
        }
    }
}