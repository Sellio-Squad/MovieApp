package com.karrar.movieapp.ui.movieDetails.createList

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.DialogCreateMovieListBinding
import com.karrar.movieapp.ui.base.BaseDialogFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateListDialog : BaseDialogFragment<DialogCreateMovieListBinding>() {

    override val layoutIdFragment = R.layout.dialog_create_movie_list
    override val viewModel: CreateListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        collectLast(viewModel.events) {
            it.getContentIfNotHandled()?.let { event ->
                when (event) {
                    is CreateListUIEvent.ListCreated -> dismiss()
                    is CreateListUIEvent.Dismiss -> dismiss()
                    is CreateListUIEvent.Error ->
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.createButton.setOnClickListener {
            viewModel.onCreateList()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.onCancel()
        }
    }
}
