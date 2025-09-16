package com.karrar.movieapp.ui.match.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karrar.movieapp.databinding.ItemMatchQuestionBinding
import com.karrar.movieapp.ui.match.QuestionType
import com.karrar.movieapp.ui.match.QuestionUiState

class MatchOptionsAdapter(
    private val onOptionClick: (QuestionUiState, QuestionType) -> Unit
) : ListAdapter<QuestionUiState, MatchOptionsAdapter.OptionViewHolder>(OptionDiffCallback()) {

    private var currentQuestionType: QuestionType = QuestionType.MOOD

    fun setQuestionType(questionType: QuestionType) {
        currentQuestionType = questionType
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemMatchQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(getItem(position), currentQuestionType, onOptionClick)
    }

    class OptionViewHolder(
        private val binding: ItemMatchQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            question: QuestionUiState,
            questionType: QuestionType,
            onOptionClick: (QuestionUiState, QuestionType) -> Unit
        ) {
            binding.question = question

            binding.optionName.text = binding.root.context.getString(question.name)

            question.description?.let { descId ->
                binding.optionDescription.text = binding.root.context.getString(descId)
                binding.optionDescription.visibility = android.view.View.VISIBLE
            } ?: run {
                binding.optionDescription.visibility = android.view.View.GONE
            }

            question.iconResource?.let { iconRes ->
                binding.optionIcon.setImageResource(iconRes)
                binding.iconContainer.visibility = android.view.View.VISIBLE
            } ?: run {
                binding.iconContainer.visibility = android.view.View.GONE
            }

            binding.root.isSelected = question.isSelected

            binding.root.setOnClickListener {
                onOptionClick(question, questionType)
            }

            binding.executePendingBindings()
        }
    }

    private class OptionDiffCallback : DiffUtil.ItemCallback<QuestionUiState>() {
        override fun areItemsTheSame(oldItem: QuestionUiState, newItem: QuestionUiState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: QuestionUiState,
            newItem: QuestionUiState
        ): Boolean {
            return oldItem == newItem
        }
    }
}