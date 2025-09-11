package com.karrar.movieapp.ui.galleryActor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentGalleryBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryActorFragment: BaseFragment<FragmentGalleryBinding>() {

    override val layoutIdFragment: Int = R.layout.fragment_gallery
    override val viewModel: GalleryActorViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.galleryActor.adapter = GalleryActorAdapter(mutableListOf(), viewModel)

        collectLast(viewModel.galleryActorUIEvent){
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }


    }

    private fun onEvent(event: GalleryActorUIEvent) {
        when (event) {
            GalleryActorUIEvent.BackEvent -> {
                removeFragment()
            }

        }
    }

    private fun removeFragment() {
        findNavController().popBackStack()
    }
}