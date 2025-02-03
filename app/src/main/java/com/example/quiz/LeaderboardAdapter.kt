// LeaderboardAdapter.kt
package com.example.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying leaderboard entries in a RecyclerView.
 *
 * This adapter binds a list of [LeaderboardEntry] objects to a RecyclerView,
 * displaying the ranking, user name, and total score.
 *
 * @property entries List of leaderboard entries to be displayed.
 */
class LeaderboardAdapter(private val entries: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    /**
     * ViewHolder class that represents a single row in the leaderboard list.
     *
     * @property tvRank TextView displaying the rank position.
     * @property tvUserName TextView displaying the user's name.
     * @property tvScore TextView displaying the user's total score.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
    }
    /**
     * Inflates the item layout for leaderboard rows.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type (not used here, as all rows are identical).
     * @return A new [ViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }
    /**
     * Binds leaderboard data to a ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvRank.text = (position + 1).toString()
        holder.tvUserName.text = entry.userName
        holder.tvScore.text = entry.totalScore.toString()
    }
    /**
     * Returns the total number of leaderboard entries.
     *
     * @return The number of items in the dataset.
     */
    override fun getItemCount(): Int = entries.size
}