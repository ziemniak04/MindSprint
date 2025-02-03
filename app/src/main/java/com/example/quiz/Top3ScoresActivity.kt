package com.example.quiz

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Activity to display the top 3 highest quiz scores for a given category.
 */
class Top3ScoresActivity : AppCompatActivity() {
    private lateinit var topScoresTextView: TextView
    private lateinit var topScoresListView: ListView
    private lateinit var backButton: Button
    private val db = FirebaseFirestore.getInstance() // Firestore instance
    private var currentCategory: String = "General"

    /**
     * Called when the activity is first created.
     * Initializes UI components and fetches top 3 quiz results.
     * @param savedInstanceState Contains data supplied if the activity is being reinitialized.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top3_scores)

        // Initialize UI components
        topScoresTextView = findViewById(R.id.topScoresTextView)
        topScoresListView = findViewById(R.id.topScoresListView)
        backButton = findViewById(R.id.backButton)

        // Retrieve category from intent extras
        currentCategory = intent.getStringExtra("CATEGORY").let {
            when {
                it.isNullOrEmpty() -> {
                    Log.e("CATEGORY_FLOW", "Missing category in Top3Scores!")
                    "General"
                }
                it == "Unknown" -> {
                    Log.w("CATEGORY_FLOW", "Received 'Unknown' category")
                    "General"
                }
                else -> it
            }
        }

        Log.d("CATEGORY_FLOW", "Final category: $currentCategory")
        topScoresTextView.text = "$currentCategory"
        getTop3Results(currentCategory)

        // Handle back button click event
        backButton.setOnClickListener {
            finish() // Close the activity and return to the previous screen
        }
    }

    /**
     * Retrieves the top 3 quiz results for a given category from Firestore.
     * @param category The category for which to fetch top scores.
     */
    private fun getTop3Results(category: String) {
        // Validate input
        Log.d("DEBUG_CATEGORY", "Searching for category: '$category'")
        if (category.isBlank()) {
            Toast.makeText(this, "Invalid category filter", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("quiz_results")
            .whereEqualTo("category", category)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("DEBUG_RESULTS", "Found ${documents.size()} documents")
                documents.forEach { doc ->
                    Log.d("DEBUG_DOC", """
                    Category: ${doc.getString("category")}
                    Score: ${doc.getLong("score")}
                    Total: ${doc.getLong("totalQuestions")}
                """.trimIndent())
                }
                val scoresList = mutableListOf<String>()

                // Validate each document
                for (document in documents) {
                    val cat = document.getString("category") ?: "N/A"
                    val score = document.getLong("score") ?: 0L
                    val total = document.getLong("totalQuestions") ?: 0L

                    // Add score to list if category matches exactly
                    if (cat.equals(category, ignoreCase = false)) {
                        scoresList.add("Score: $score/$total")
                    }
                }

                if (scoresList.isEmpty()) {
                    scoresList.add("No results for $category yet!")
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scoresList)
                topScoresListView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
