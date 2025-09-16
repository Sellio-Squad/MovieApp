package com.karrar.movieapp.ui.profile.settings.language

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogChangeLanguageBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeLanguageDialog() : BaseDialogFragment<DialogChangeLanguageBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_change_language
    override val viewModel: ChangeLanguageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        collectLast(viewModel.changeLanguageUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: ChangeLanguageEvents) {
        when (event) {
            ChangeLanguageEvents.OnCloseDialog -> {
                dismiss()
            }
        }
    }

}