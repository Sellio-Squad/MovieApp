package com.karrar.movieapp.utilities.common

import androidx.databinding.BindingAdapter

@BindingAdapter("onSeeAllClick")
fun SectionHeaderView.setOnSeeAllClickBinding(listener: (() -> Unit)?) {
    listener?.let { setSeeAllClickListener(it) }
}

@BindingAdapter("android:text")
fun SectionHeaderView.setText(text: String?) {
    setTitle(text ?: "")
}
