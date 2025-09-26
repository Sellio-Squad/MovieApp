package com.karrar.movieapp.ui.movieDetails.ratingMovieDialog

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogRateTheMovieBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingMovieDialog : BaseDialogFragment<DialogRateTheMovieBinding>() {
    override val layoutIdFragment: Int = R.layout.dialog_rate_the_movie
    override val viewModel: RatingMovieMovieDialogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        collectLast(viewModel.ratingDialogEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: RatingDialogEvents) {
        when (event) {
            is RatingDialogEvents.CloseDialogEvent -> {
                dismiss()
            }

            is RatingDialogEvents.MessageAppear -> {
                Toast.makeText(context, getString(R.string.submit_toast), Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }
}
