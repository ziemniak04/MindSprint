package com.example.quiz

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Activity responsible for displaying the user's quiz history.
 * Retrieves quiz results from Firestore and presents them in a RecyclerView.
 */
class QuizHistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyStateText: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var quizFirestore: QuizFirestore
    private val auth = FirebaseAuth.getInstance()

    /**
     * Called when the activity is first created.
     * Initializes UI components and loads the quiz history.
     * @param savedInstanceState Contains the saved state of the activity, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views
        recyclerView = findViewById(R.id.quizHistoryRecyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyStateText = findViewById(R.id.emptyStateText)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        // Configure RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        quizFirestore = QuizFirestore()

        // Set up SwipeRefreshLayout to allow manual refreshing
        swipeRefresh.setOnRefreshListener {
            loadQuizHistory()
        }

        // Initial data load
        loadQuizHistory()
    }

    /**
     * Handles the selection of menu items.
     * @param item The selected menu item.
     * @return True if the item was handled, otherwise calls the superclass implementation.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Loads the quiz history from Firestore and updates the RecyclerView.
     * Displays an empty state message if no results are found.
     */
    private fun loadQuizHistory() {
        lifecycleScope.launch {
            try {
                showLoading()
                val userId = auth.currentUser?.uid
                if (userId.isNullOrEmpty()) {
                    Log.e("QuizHistory", "User ID is null – no results to load.")
                    showEmptyState()
                    return@launch
                }
                Log.d("QuizHistory", "Loading quiz history for user: $userId")
                val results = quizFirestore.getUserQuizResults(userId)
                Log.d("QuizHistory", "Received ${'$'}{results.size} results")

                if (results.isEmpty()) {
                    showEmptyState()
                } else {
                    showResults(results)
                }
            } catch (e: Exception) {
                Log.e("QuizHistory", "Error loading quiz history", e)
                Toast.makeText(
                    this@QuizHistoryActivity,
                    "Error loading quiz history: ${'$'}{e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    /**
     * Displays a loading indicator while data is being retrieved.
     */
    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateText.visibility = View.GONE
    }

    /**
     * Displays a message indicating that there is no quiz history available.
     */
    private fun showEmptyState() {
        loadingIndicator.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyStateText.visibility = View.VISIBLE
    }

    /**
     * Updates the RecyclerView with quiz history results.
     * @param results The list of quiz results to be displayed.
     */
    private fun showResults(results: List<QuizResult>) {
        loadingIndicator.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE

        recyclerView.adapter = QuizResultAdapter(results)
    }
}