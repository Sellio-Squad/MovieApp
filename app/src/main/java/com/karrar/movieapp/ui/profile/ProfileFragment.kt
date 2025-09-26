package com.karrar.movieapp.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentProfileBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.profile.settings.language.ChangeLanguageEvents
import com.karrar.movieapp.ui.profile.settings.language.ChangeLanguageViewModel
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    override val layoutIdFragment: Int = R.layout.fragment_profile
    override val viewModel: ProfileViewModel by viewModels()

    private val changeLanguageViewModel: ChangeLanguageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(true, getString(R.string.profile))

        binding.lifecycleOwner = viewLifecycleOwner

        collectLast(viewModel.profileUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }

        collectLast(changeLanguageViewModel.changeLanguageUIEvent) {
            it.getContentIfNotHandled()?.let { event ->
                when (event) {
                    ChangeLanguageEvents.OnCloseDialog -> {
                    }
                    ChangeLanguageEvents.OnLanguageChanged -> {
                        requireActivity().recreate()
                    }
                }
            }
        }

        onClick(uiState = viewModel.profileDetailsUIState.value)
    }

    private fun onClick(uiState: ProfileUIState) {
        binding.switchLanguage.isChecked = uiState.isSwitchChecked
        binding.switchLanguage.setOnCheckedChangeListener { _, checked ->
            viewModel.changeTheme()
        }
    }

    private fun onEvent(event: ProfileUIEvent) {
        val action = when (event) {
            ProfileUIEvent.DialogLogoutEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToLogoutDialog()
            }
            is ProfileUIEvent.LoginEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToLogInDialog(event.userName)
            }
            ProfileUIEvent.RatedMoviesEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToRatedMoviesFragment()
            }
            ProfileUIEvent.WatchHistoryEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToWatchHistoryFragment()
            }
            ProfileUIEvent.DialogPreferencesEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToContentPreferencesDialog()
            }

            ProfileUIEvent.DialogLanguageEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToChangeLanguageDialog()
            }
            ProfileUIEvent.MyCollectionEvent -> {
                ProfileFragmentDirections.actionProfileFragmentToMyListsFragment()
            }
        }
        findNavController().navigate(action)
    }
}
