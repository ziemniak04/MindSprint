package com.example.quiz

import QuestionData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying a list of quiz questions in a RecyclerView.
 *
 * This adapter extends [ListAdapter] to efficiently manage updates using DiffUtil.
 * It binds quiz questions to views and provides an edit button for modifying questions.
 *
 * @property onEditClick A lambda function that handles edit button clicks.
 */
class QuestionListAdapter(
    private val onEditClick: (QuestionData) -> Unit
) : ListAdapter<QuestionData, QuestionListAdapter.ViewHolder>(QuestionDiffCallback()) {
    /**
     * Creates and returns a ViewHolder for a single quiz question item.
     * @param parent The parent ViewGroup.
     * @param viewType The view type (not used as all rows are identical).
     * @return A new [ViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_list, parent, false)
        return ViewHolder(view)
    }
    /**
     * Binds question data to a ViewHolder.
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    /**
     * ViewHolder class that represents a single quiz question item.
     * @property questionText TextView displaying the question text.
     * @property categoryText TextView displaying the question category.
     * @property editButton Button to edit the question.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val questionText: TextView = view.findViewById(R.id.questionText)
        private val categoryText: TextView = view.findViewById(R.id.categoryText)
        private val editButton: Button = view.findViewById(R.id.editButton)

        /**
         * Binds a [QuestionData] object to the ViewHolder.
         * @param question The question to display.
         */
        fun bind(question: QuestionData) {
            questionText.text = question.questionText
            categoryText.text = question.category
            editButton.setOnClickListener { onEditClick(question) }
        }
    }

    /**
     * DiffUtil callback for efficiently updating the RecyclerView.
     */
    class QuestionDiffCallback : DiffUtil.ItemCallback<QuestionData>() {
        /**
         * Checks if two items represent the same data.
         * @param oldItem The old question data.
         * @param newItem The new question data.
         * @return True if the items have the same ID.
         */
        override fun areItemsTheSame(oldItem: QuestionData, newItem: QuestionData): Boolean {
            return oldItem.id == newItem.id
        }
        /**
         * Checks if the contents of two items are the same.
         * @param oldItem The old question data.
         * @param newItem The new question data.
         * @return True if the items are equal.
         */
        override fun areContentsTheSame(oldItem: QuestionData, newItem: QuestionData): Boolean {
            return oldItem == newItem
        }
    }
}
