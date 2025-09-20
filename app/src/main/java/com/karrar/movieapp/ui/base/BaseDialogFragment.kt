package com.karrar.movieapp.ui.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.*
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karrar.movieapp.BR
import com.karrar.movieapp.R
import androidx.core.graphics.drawable.toDrawable


abstract class BaseDialogFragment<VDB : ViewDataBinding> : BottomSheetDialogFragment() {

    abstract val layoutIdFragment: Int
    abstract val viewModel: ViewModel

    private lateinit var _binding: VDB
    protected val binding: VDB
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutIdFragment, container, false)
        _binding.apply {
            lifecycleOwner = this@BaseDialogFragment
            setVariable(BR.viewModel, viewModel)
            setVariable(BR.listener, viewModel)
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFloatingBottomSheetMargins()
    }
    private fun setupFloatingBottomSheetMargins() {
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isDraggable = false

                val layoutParams = it.layoutParams as ViewGroup.MarginLayoutParams
                val horizontalMargin =
                    resources.getDimensionPixelSize(R.dimen.bottom_sheet_horizontal_margin)

                layoutParams.leftMargin = horizontalMargin
                layoutParams.rightMargin = horizontalMargin
                it.layoutParams = layoutParams

                it.setBackgroundResource(android.R.color.transparent)
               dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            }
        }
    }
}