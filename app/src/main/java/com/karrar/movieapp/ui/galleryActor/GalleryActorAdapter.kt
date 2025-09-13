package com.karrar.movieapp.ui.galleryActor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.ItemGalleryBinding
import com.karrar.movieapp.databinding.ItemGalleryFlippedBinding

class ActorGalleryAdapter(
    private var images: List<List<String>> = emptyList()
) : RecyclerView.Adapter<ActorGalleryAdapter.BaseViewHolder>() {

    fun submitList(newImages: List<String>) {
        images = newImages.chunked(3)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_NORMAL -> {
                val binding = ItemGalleryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ActorGalleryViewHolder(binding)
            }

            TYPE_FLIPPED -> {
                val binding = ItemGalleryFlippedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ActorGalleryFlippedViewHolder(binding)
            }

            else -> throw Exception("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is ActorGalleryViewHolder -> holder.bind(images[position])
            is ActorGalleryFlippedViewHolder -> holder.bind(images[position])
        }
    }

    override fun getItemCount() = images.size

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) TYPE_NORMAL else TYPE_FLIPPED
    }

    open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class ActorGalleryFlippedViewHolder(private val binding: ItemGalleryFlippedBinding) :
        BaseViewHolder(binding.root) {

        fun bind(images: List<String>) {
            if (images.isNotEmpty()) {
                binding.ivMainImage.load(
                    images[0]
                ) {
                    placeholder(R.drawable.image_error_palceholder)
                    error(R.drawable.profile)
                }
            } else {
                binding.ivMainImage.setImageDrawable(null)
            }

            if (images.size > 1) {
                binding.ivSecondImage.load(
                    images[1]
                ) {
                    placeholder(R.drawable.image_error_palceholder)
                    error(R.drawable.profile)
                }
            } else {
                binding.ivSecondImage.setImageDrawable(null)
            }

            if (images.size > 2) {
                binding.ivThirdImage.load(
                    images[2]
                ) {
                    placeholder(R.drawable.image_error_palceholder)
                    error(R.drawable.profile)
                }
            } else {
                binding.ivThirdImage.setImageDrawable(null)
            }
        }
    }

    class ActorGalleryViewHolder(private val binding: ItemGalleryBinding) :
        BaseViewHolder(binding.root) {

        fun bind(images: List<String>) {
            if (images.isNotEmpty()) {
                binding.ivMainImage.load(
                    images[0]
                ) {
                    placeholder(R.drawable.image_error_palceholder)
                    error(R.drawable.profile)
                }
            } else {
                binding.ivMainImage.setImageDrawable(null)
            }

            if (images.size > 1) {
                binding.ivSecondImage.load(
                    images[1]
                ) {
                    placeholder(R.drawable.image_error_palceholder)
                    error(R.drawable.profile)
                }
            } else {
                binding.ivSecondImage.setImageDrawable(null)
            }

            if (images.size > 2) {
                binding.ivThirdImage.load(
                    images[2]
                ) {
                    placeholder(R.drawable.image_error_palceholder)
                    error(R.drawable.profile)
                }
            } else {
                binding.ivThirdImage.setImageDrawable(null)
            }
        }
    }

    companion object {
        private const val TYPE_NORMAL = 0
        private const val TYPE_FLIPPED = 1
    }
}