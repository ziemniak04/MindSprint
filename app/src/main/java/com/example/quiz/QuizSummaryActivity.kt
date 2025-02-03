package com.example.quiz

import NotificationHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that displays a summary of a completed quiz.
 * This screen shows the number of correct and incorrect answers,
 * triggers a notification upon quiz completion, and allows navigation
 * to the Top 3 Scores screen.
 */
class QuizSummaryActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created.
     * Initializes the UI components and processes quiz results.
     * @param savedInstanceState Contains the saved state of the activity, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_summary)

        // Retrieve quiz result data from intent
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val category = intent.getStringExtra("CATEGORY").let {
            if (it.isNullOrEmpty()) {
                Log.e("CATEGORY_FLOW", "Missing category in QuizSummary!")
                "Unknown"
            } else {
                it
            }
        }
        val correctTextView: TextView = findViewById(R.id.correctTextView)
        val incorrectTextView: TextView = findViewById(R.id.incorrectTextView)
        val nextButton: Button = findViewById(R.id.nextButton) // Button to navigate to Top 3 Scores

        val incorrectAnswers = totalQuestions - correctAnswers

        // Display quiz results
        correctTextView.text = "✅ Correct Answers: $correctAnswers"
        incorrectTextView.text = "❌ Incorrect Answers: $incorrectAnswers"

        // Show quiz completion notification
        NotificationHelper(this).showQuizCompletionNotification(correctAnswers, totalQuestions)

        /**
         * Handles the navigation to the Top 3 Scores screen when the next button is clicked.
         */
        nextButton.setOnClickListener {
            Log.d("CATEGORY_PASSING", "Passing to Top3: $category")
            val intent = Intent(this, Top3ScoresActivity::class.java).apply {
                putExtra("CATEGORY", category)
            }
            startActivity(intent)
            finish()
        }
    }
}
