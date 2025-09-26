package com.karrar.movieapp.ui.tvShowDetails.ratingTvShowDialog

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogRateTheTvShowBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingTvShowDialog : BaseDialogFragment<DialogRateTheTvShowBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_rate_the_tv_show
    override val viewModel: RatingTvShowDialogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        collectLast(viewModel.ratingDialogEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: RatingTvShowDialogEvents) {
        when (event) {
            is RatingTvShowDialogEvents.CloseDialogEvent -> {
                dismiss()
            }

            is RatingTvShowDialogEvents.MessageAppear -> {
                Toast.makeText(context, getString(R.string.submit_toast), Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }
}

