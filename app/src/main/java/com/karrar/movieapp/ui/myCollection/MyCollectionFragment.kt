package com.karrar.movieapp.ui.myCollection

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentMyListBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.myCollection.myCollectionUIState.MyListUIEvent
import com.karrar.movieapp.utilities.collectLast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyCollectionFragment : BaseFragment<FragmentMyListBinding>() {

    override val layoutIdFragment: Int = R.layout.fragment_my_list
    override val viewModel: MyLCollectionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(true, getString(R.string.my_collections))
        binding.savedList.adapter = CreatedListAdapter(emptyList(), viewModel)
        collectEvent()

        binding.buttonEmpty.setOnClickListener {
            findNavController().navigate(MyCollectionFragmentDirections.actionMyListFragmentToExploringFragment())
        }

    }

    private fun collectEvent() {
        collectLast(viewModel.myListUIEvent) {
            it.getContentIfNotHandled()?.let { onEvent(it) }
        }
    }

    private fun onEvent(event: MyListUIEvent) {
        var action: NavDirections? = null
        when (event) {
            MyListUIEvent.CreateButtonClicked -> {
                action = MyCollectionFragmentDirections.actionMyListFragmentToCreateSavedList()
            }
            MyListUIEvent.StartCollectingButtonClicked -> {
                action = MyCollectionFragmentDirections.actionMyListFragmentToExploringFragment()
            }
            is MyListUIEvent.DisplayError -> {
                Toast.makeText(requireContext(), event.errorMessage, Toast.LENGTH_LONG).show()
            }
            is MyListUIEvent.OnSelectItem -> {
                action = MyCollectionFragmentDirections.actionMyListFragmentToSavedListFragment(
                    event.createdListUIState.listID,
                    event.createdListUIState.name
                )
            }
            else -> {
            }
        }
        action?.let { findNavController().navigate(it) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getData()
    }

}