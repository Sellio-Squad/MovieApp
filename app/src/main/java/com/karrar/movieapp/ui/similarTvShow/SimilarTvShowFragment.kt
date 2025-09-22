package com.karrar.movieapp.ui.similarTvShow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSimilarDetailsBinding
import com.karrar.movieapp.ui.base.BaseFragment

class SimilarTvShowFragment : BaseFragment<FragmentSimilarDetailsBinding>() {
    override val layoutIdFragment: Int = R.layout.fragment_similar_details
    override val viewModel: SimilarTvShowViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(true, getString(R.string.similar_tv_shows))
        binding.recyclerMedia.adapter = SimilarTvShowsAdapter(emptyList(), viewModel)
    }
}