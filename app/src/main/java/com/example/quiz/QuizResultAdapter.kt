package com.example.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying a list of quiz results in a RecyclerView.
 * This adapter binds a list of [QuizResult] objects to views and formats the date
 * for display in a user-friendly format.
 * @property results List of quiz results to be displayed.
 */
class QuizResultAdapter(private val results: List<QuizResult>) :
    RecyclerView.Adapter<QuizResultAdapter.ViewHolder>() {

    /**
     * ViewHolder class representing a single quiz result item.
     * @property categoryText TextView displaying the quiz category.
     * @property scoreText TextView displaying the user's score.
     * @property dateText TextView displaying the formatted timestamp.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryText: TextView = view.findViewById(R.id.categoryText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
        val dateText: TextView = view.findViewById(R.id.dateText)
    }

    /**
     * Creates and returns a ViewHolder for a single quiz result item.
     * @param parent The parent ViewGroup.
     * @param viewType The view type (not used as all rows are identical).
     * @return A new [ViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_result, parent, false)
        return ViewHolder(view)
    }
    /**
     * Binds quiz result data to a ViewHolder.
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.categoryText.text = result.category
        holder.scoreText.text = "${result.score}/${result.totalQuestions}"
        holder.dateText.text = formatDate(result.timestamp)
    }
    /**
     * Returns the total number of quiz results in the dataset.
     * @return The number of items in the dataset.
     */
    override fun getItemCount() = results.size
    /**
     * Formats the timestamp into a readable date format.
     * @param timestamp The date object to be formatted.
     * @return A string representation of the formatted date.
     */
    private fun formatDate(timestamp: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp)
    }
}
