package com.karrar.movieapp.ui.profile.editProfile

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentWebViewBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewFragment : BaseFragment<FragmentWebViewBinding>() {
    override val layoutIdFragment: Int = R.layout.fragment_web_view
    override val viewModel: WebViewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(true, getString(R.string.edit_profile))
        setupWebView()
        collectLast(viewModel.url) {
            loadUrl(it)
        }
    }

    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient() // For progress bar and title updates

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
        }
    }

    private fun loadUrl(url: String) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            binding.webView.loadUrl(url)
        } else {
            binding.webView.loadUrl("https://$url")
        }
    }
}