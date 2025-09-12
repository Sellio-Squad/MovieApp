package com.karrar.movieapp.ui.galleryActor

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.karrar.movieapp.BR
import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener

class GalleryActorAdapter(
    private val items: List<GalleryActorUIState>,
    val listener: GalleryActorInteractionListener
) : BaseAdapter<GalleryActorUIState>(items, listener) {
    override val layoutID: Int = R.layout.item_gallery

    override fun bind(holder: ItemViewHolder, position: Int) {
        super.bind(holder, position)

        val itemGallery = items[position]
        val containerImages = holder.binding.root.findViewById<FrameLayout>(R.id.container_images)

        if (itemGallery.isFlipped) {
            containerImages.scaleX = -1f
        } else {
            containerImages.scaleX = 1f
        }
        containerImages.removeAllViews()


        val childBinding = when (itemGallery.imagesUrl.size) {
            1 -> {
                val binding = DataBindingUtil.inflate<ViewDataBinding>(
                    LayoutInflater.from(containerImages.context),
                    R.layout.single_image_gallery, containerImages, false
                )
                binding.setVariable(BR.imageUrlGallery, itemGallery.imagesUrl[0])
                binding
            }

            2 -> {
                val binding = DataBindingUtil.inflate<ViewDataBinding>(
                    LayoutInflater.from(containerImages.context),
                    R.layout.three_image_gallery, containerImages, false
                )
                binding.setVariable(BR.firstImageUrl, itemGallery.imagesUrl[0])
                binding.setVariable(BR.secondImageUrl, itemGallery.imagesUrl[1])
                binding
            }

            3 -> {
                val binding = DataBindingUtil.inflate<ViewDataBinding>(
                    LayoutInflater.from(containerImages.context),
                    R.layout.three_image_gallery, containerImages, false
                )
                binding.setVariable(BR.firstImageUrl, itemGallery.imagesUrl[0])
                binding.setVariable(BR.secondImageUrl, itemGallery.imagesUrl[1])
                binding.setVariable(BR.thirdImageUrl, itemGallery.imagesUrl[2])
                binding
            }

            else -> return
        }
        childBinding.executePendingBindings()
        containerImages.addView(childBinding.root)

    }

}

interface GalleryActorInteractionListener : BaseInteractionListener