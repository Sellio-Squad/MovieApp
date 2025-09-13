package com.karrar.movieapp.ui.profile.login

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogLogInBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogInDialog : BaseDialogFragment<DialogLogInBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_log_in
    override val viewModel: LogInViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        collectLast(viewModel.loginUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }

    }

    private fun onEvent(event: LogInEvents) {
        when (event) {
            LogInEvents.CloseDialogEvent -> {
                dismiss()
            }

            LogInEvents.LogInEvent -> {
                findNavController().navigate(
                    LogInDialogDirections.actionLogInDialogToLoginFragment(Constants.PROFILE)
                )
            }

            is LogInEvents.GoToWebsiteEvent -> {
                findNavController().navigate(
                    LogInDialogDirections.actionLogInDialogToWebViewFragment(event.url)
                )
            }
        }
    }

}