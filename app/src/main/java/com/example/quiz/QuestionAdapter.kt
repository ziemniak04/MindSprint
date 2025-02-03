package com.example.quiz.firebase

import QuestionData
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R

/**
 * Adapter for displaying a list of quiz questions in a RecyclerView.
 * This adapter binds [QuestionData] objects to views and provides click listeners
 * for editing and deleting questions.
 * @property questions List of quiz questions to be displayed.
 * @property listener Callback interface for handling item interactions.
 */
class QuestionAdapter(
    private val questions: List<QuestionData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    /**
     * Creates and returns a ViewHolder for a single quiz question item.
     * @param parent The parent ViewGroup.
     * @param viewType The view type (not used as all rows are identical).
     * @return A new [QuestionViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    /**
     * Binds question data to a ViewHolder.
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.questionText.text = question.questionText
        holder.answerButtonA.text = question.options[0]
        holder.answerButtonB.text = question.options[1]
        holder.answerButtonC.text = question.options[2]
        holder.answerButtonD.text = question.options[3]

        holder.editButton.setOnClickListener {
            listener.onEditClick(question)
        }
        holder.deleteButton.setOnClickListener {
            listener.onDeleteClick(question)
        }
    }
    /**
     * Returns the total number of questions in the dataset.
     * @return The number of questions in the list.
     */
    override fun getItemCount(): Int = questions.size

    /**
     * ViewHolder class that represents a single quiz question item.
     *
     * @property questionText TextView displaying the question text.
     * @property answerButtonA Button for option A.
     * @property answerButtonB Button for option B.
     * @property answerButtonC Button for option C.
     * @property answerButtonD Button for option D.
     * @property editButton Button to edit the question.
     * @property deleteButton Button to delete the question.
     */
    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val answerButtonA: Button = itemView.findViewById(R.id.answerButton1)
        val answerButtonB: Button = itemView.findViewById(R.id.answerButton2)
        val answerButtonC: Button = itemView.findViewById(R.id.answerButton3)
        val answerButtonD: Button = itemView.findViewById(R.id.answerButton4)
        val editButton: Button = itemView.findViewById(R.id.updateButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }
    /**
     * Interface for handling user interactions with quiz questions.
     */
    interface OnItemClickListener {
        /**
         * Called when the edit button is clicked.
         *
         * @param question The question being edited.
         */
        fun onEditClick(question: QuestionData)
        /**
         * Called when the delete button is clicked.
         *
         * @param question The question being deleted.
         */
        fun onDeleteClick(question: QuestionData)
    }
}
