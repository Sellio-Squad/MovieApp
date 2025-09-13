package com.karrar.movieapp.ui.profile.settings.contentPreferences

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogContentPreferencesBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContentPreferencesDialog : BaseDialogFragment<DialogContentPreferencesBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_content_preferences
    override val viewModel: ContentPreferencesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        collectLast(viewModel.uiState) {
            binding.recycler.adapter = ContentPreferencesAdapter(emptyList(), viewModel)

        }
        collectLast(viewModel.preferenceUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: ContentPreferencesEvents) {
        when (event) {
            ContentPreferencesEvents.CloseDialogEvent -> {
                dismiss()
            }
        }
    }
}