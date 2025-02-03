package com.example.quiz

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity responsible for displaying the quiz leaderboard.
 *
 * This activity fetches and aggregates quiz results from Firestore,
 * sorts users by their total score, and displays the top-ranked users
 * in a RecyclerView.
 */
class LeaderboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private val db = FirebaseFirestore.getInstance()
    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState Contains the saved state of the activity, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadLeaderboard()
    }
    /**
     * Loads and processes the leaderboard data from Firestore.
     *
     * Fetches all quiz results, aggregates scores per user,
     * sorts the top 10 users, and retrieves their names for display.
     */
    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                // 1. Query all quiz results from Firestore
                val resultsSnapshot = db.collection("quiz_results").get().await()
                val scoreMap = mutableMapOf<String, Int>()

                resultsSnapshot.documents.forEach { document ->
                    val userId = document.getString("userId") ?: return@forEach
                    val score = document.getLong("score")?.toInt() ?: 0
                    scoreMap[userId] = (scoreMap[userId] ?: 0) + score
                }
                Log.d("LeaderboardActivity", "Aggregated scores for ${scoreMap.size} users.")

                // 2. Sort users by total score in descending order and take the top 10
                val sortedEntries = scoreMap.toList().sortedByDescending { it.second }
                val topEntries = sortedEntries.take(10)

                // 3. Fetch user names from Firestore for each top entry
                val leaderboardEntries = topEntries.map { (userId, totalScore) ->
                    async {
                        val userSnapshot = db.collection("users").document(userId).get().await()
                        val userName = userSnapshot.getString("name") ?: "Unknown"
                        LeaderboardEntry(userId, userName, totalScore)
                    }
                }.awaitAll()

                // 4. Set the adapter for the RecyclerView to display the leaderboard
                recyclerView.adapter = LeaderboardAdapter(leaderboardEntries)
            } catch (e: Exception) {
                Log.e("LeaderboardActivity", "Error loading leaderboard: ${e.message}", e)
                Toast.makeText(this@LeaderboardActivity, "Error loading leaderboard", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
